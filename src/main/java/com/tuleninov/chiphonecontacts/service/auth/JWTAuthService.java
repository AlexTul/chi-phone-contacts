package com.tuleninov.chiphonecontacts.service.auth;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.tuleninov.chiphonecontacts.config.security.SecurityConstants;
import com.tuleninov.chiphonecontacts.config.security.properties.CustomSecurityProperties;
import com.tuleninov.chiphonecontacts.exceptions.auth.InvalidRefreshTokenException;
import com.tuleninov.chiphonecontacts.model.auth.CustomUserDetails;
import com.tuleninov.chiphonecontacts.model.auth.RefreshToken;
import com.tuleninov.chiphonecontacts.model.auth.response.AccessTokenResponse;
import com.tuleninov.chiphonecontacts.model.user.CustomUser;
import com.tuleninov.chiphonecontacts.model.user.KnownAuthority;
import com.tuleninov.chiphonecontacts.model.user.UserStatus;
import com.tuleninov.chiphonecontacts.repository.RefreshTokenRepository;
import com.tuleninov.chiphonecontacts.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * The JWTAuthService for AuthController.
 */
@Service
public class JWTAuthService implements AuthOperations {

    private static final Logger log = LoggerFactory.getLogger(JWTAuthService.class);

    private final RefreshTokenRepository refreshTokenRepository;

    private final UserRepository userRepository;

    private final Duration jwtExpiration;

    private final Duration refreshExpiration;

    private final Algorithm algorithm;

    public JWTAuthService(CustomSecurityProperties securityProperties,
                          RefreshTokenRepository refreshTokenRepository,
                          UserRepository userRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
        var jwtProperties = securityProperties.getJwt();
        this.jwtExpiration = jwtProperties.getAccessExpireIn();
        this.refreshExpiration = jwtProperties.getRefreshExpireIn();
        this.algorithm = Algorithm.HMAC512(new String(jwtProperties.getSecret()).getBytes());
    }

    /**
     * Get response with accessToken, refreshToken, expireIn.
     *
     * @param userDetails custom UserDetails
     * @return response with token, refresh token, expire in
     */
    @Override
    @Transactional
    public AccessTokenResponse getToken(CustomUserDetails userDetails) {
        RefreshToken newToken = issueRefreshToken(userDetails.getSource());
        return response(userDetails.getUsername(), userDetails.getAuthorities(), newToken);
    }

    /**
     * Get response with new accessToken, refreshToken, new expireIn.
     *
     * @param refreshToken refresh token
     * @return response with token, refresh token, expire in
     */
    @Override
    @Transactional
    public AccessTokenResponse refreshToken(String refreshToken) throws InvalidRefreshTokenException {
        RefreshToken storedToken = refreshTokenRepository.findIfValid(
                verifyRefreshToken(refreshToken),
                OffsetDateTime.now(),
                UserStatus.ACTIVE
        ).orElseThrow(InvalidRefreshTokenException::new);

        checkIfRotated(storedToken);

        CustomUser user = storedToken.getUser();

        var nextToken = issueRefreshToken(user);

        refreshTokenRepository.updateChain(storedToken, nextToken);

        // call prune_refresh_tokens() after refreshing
        refreshTokenRepository.pruneRefreshTokens();

        return response(user.getEmail(), user.getAuthorities().keySet(), nextToken);
    }

    /**
     * Token destruction (such as logout).
     *
     * @param refreshToken refresh token
     * @param ownerEmail owner email
     * */
    @Override
    @Transactional
    public void invalidateToken(String refreshToken, String ownerEmail) throws InvalidRefreshTokenException {
        RefreshToken storedToken = refreshTokenRepository.findById(verifyRefreshToken(refreshToken))
                .orElseThrow(InvalidRefreshTokenException::new);
        checkOwner(storedToken, ownerEmail);
        checkIfRotated(storedToken);
        refreshTokenRepository.deleteChain(storedToken);
    }

    /**
     * Check that this email in this token is the same, if not, it means that
     * access token of one user, and refresh token of another user.
     *
     * @param storedToken stored token
     * @param email owner email
     * */
    private void checkOwner(RefreshToken storedToken, String email) throws InvalidRefreshTokenException {
        CustomUser user = storedToken.getUser();
        if (!user.getEmail().equals(email)) {
            // suspend the nasty-ass token pilferer
            String message = "!! INVESTIGATE ASAP !! User {} engaged in a suspicious activity, " +
                    "trying to use a refresh token issued to another user. " +
                    "Blocking the suspicious actor's account pending investigation!";
            log.error(message, email);
            userRepository.changeStatusByEmail(email, UserStatus.SUSPENDED);
            // invalidate token
            refreshTokenRepository.deleteChain(storedToken);
            throw new InvalidRefreshTokenException();
        }
    }

    /**
     * Check that the token is new, etc. there were no tokens generated after it.
     * This is done to check for the possibility of loss (theft) of the token.
     * If the token has "storedToken.getNext()", then we log an error and delete the entire token chain.
     *
     * @param storedToken stored token
     */
    private void checkIfRotated(RefreshToken storedToken) throws InvalidRefreshTokenException {
        // if an old token is used - we still want to invalidate whole chain in case the new one was stolen
        if (storedToken.getNext() != null) {
            String message = "!! INVESTIGATE ASAP !! An old refresh token used for user {}, " +
                    "signifying possible token theft! Invalidating the entire token chain.";
            log.error(message, storedToken.getUser().getEmail());
            refreshTokenRepository.deleteChain(storedToken.getNext());
            throw new InvalidRefreshTokenException();
        }
    }

    /**
     * Issue a new RefreshToken, which, unlike the JWT token,
     * has a unique id and will be stored in the database.
     *
     * @param user user
     * @return refresh token
     */
    private RefreshToken issueRefreshToken(CustomUser user) {
        var refreshToken = new RefreshToken();
        var now = OffsetDateTime.now();
        refreshToken.setIssuedAt(now);
        refreshToken.setExpireAt(now.plus(refreshExpiration));
        refreshToken.setUser(user);
        return refreshTokenRepository.save(refreshToken);
    }

    /**
     * Get response with accessToken, refreshToken, expireIn.
     *
     * @param subject      specific Subject ("sub") claim to the Payload
     * @param authorities  user`s authorities
     * @param refreshToken refresh token
     * @return response with accessToken, refreshToken, expireIn
     */
    private AccessTokenResponse response(String subject,
                                         Collection<? extends GrantedAuthority> authorities,
                                         RefreshToken refreshToken) {
        String accessToken = issueJWT(subject, authorities);
        return new AccessTokenResponse(
                accessToken,
                signRefreshToken(refreshToken),
                jwtExpiration.toSeconds(),
                authorities.stream()
                        .map(KnownAuthority.class::cast)
                        .collect(Collectors.toSet())
        );
    }

    /**
     * Validate refresh token.
     *
     * @param refreshJWT refresh JWT
     * @return UUID
     * */
    private UUID verifyRefreshToken(String refreshJWT) throws InvalidRefreshTokenException {
        try {
            String id = JWT.require(algorithm)
                    .build()
                    .verify(refreshJWT)
                    .getId();
            Objects.requireNonNull(id, "jti must be present in refresh token");
            return UUID.fromString(id);
        } catch (Exception e) {
            throw new InvalidRefreshTokenException(e);
        }
    }

    /**
     * Sign refresh token.
     * It has an id but no authorities.
     *
     * @param token refresh token
     * @return response with accessToken, refreshToken, expireIn
     */
    private String signRefreshToken(RefreshToken token) {
        return JWT.create()
                .withSubject(token.getUser().getEmail())
                .withJWTId(token.getValue().toString())
                .withIssuedAt(Date.from(token.getIssuedAt().toInstant()))
                .withExpiresAt(Date.from(token.getExpireAt().toInstant()))
                .sign(algorithm);
    }

    /**
     * Issue JWT token.
     *
     * @param subject     specific Subject ("sub") claim to the Payload
     * @param authorities user`s authorities
     * @return JWT tokens
     */
    private String issueJWT(String subject, Collection<? extends GrantedAuthority> authorities) {
        long issuedAt = System.currentTimeMillis();
        return JWT.create()
                .withSubject(subject)
                .withIssuedAt(new Date(issuedAt))
                .withExpiresAt(new Date(issuedAt + jwtExpiration.toMillis()))
                .withArrayClaim(SecurityConstants.AUTHORITIES_CLAIM, authorities.stream()
                        .map(GrantedAuthority::getAuthority)
                        .toArray(String[]::new))
                .sign(algorithm);
    }
}
