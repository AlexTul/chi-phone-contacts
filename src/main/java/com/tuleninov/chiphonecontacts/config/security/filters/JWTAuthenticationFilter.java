package com.tuleninov.chiphonecontacts.config.security.filters;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tuleninov.chiphonecontacts.model.auth.request.SignInRequest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UncheckedIOException;

/**
 * JWTAuthenticationFilter бере JSON з інформацією про логін користувача, читає її як SignInRequest.
 * Дістає юзера з БД, валідує його юзернейм та пароль.
 */
public class JWTAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final ObjectMapper objectMapper;

    public JWTAuthenticationFilter(AuthenticationManager authenticationManager, ObjectMapper objectMapper) {
        setAuthenticationManager(authenticationManager);
        setUsernameParameter("login");
        this.objectMapper = objectMapper;
    }

    /**
     * Returning a fully populated Authentication object (including granted authorities) if successful.
     * Designed for simple presentation of a username and password.
     * The principal and credentials are set using with an Object.
     *
     * @param req  an object that is passed as an argument to the servlet's utility methods (doGet, doPost, etc.)
     * @param resp an object that is passed as an argument to the servlet's utility methods (doGet, doPost, etc.)
     * @return a fully authenticated object including credentials
     */
    @Override
    public Authentication attemptAuthentication(HttpServletRequest req,
                                                HttpServletResponse resp) throws AuthenticationException {
        SignInRequest credentials;
        try {
            credentials = objectMapper.readValue(req.getInputStream(), SignInRequest.class);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        var authToken = new UsernamePasswordAuthenticationToken(
                credentials.login(),
                credentials.password()
        );
        return getAuthenticationManager().authenticate(authToken);
    }

    /**
     * Changes the currently authenticated principal, or removes the authentication information.
     *
     * @param req   an object that is passed as an argument to the servlet's utility methods (doGet, doPost, etc.)
     * @param resp  an object that is passed as an argument to the servlet's utility methods (doGet, doPost, etc.)
     * @param chain an object provided by the servlet container to the developer giving a view into the invocation chain of a filtered request for a resource
     * @param auth  the new Authentication token, or null if no further authentication information should be stored
     */
    @Override
    protected void successfulAuthentication(HttpServletRequest req,
                                            HttpServletResponse resp,
                                            FilterChain chain,
                                            Authentication auth) throws IOException, ServletException {

        SecurityContextHolder.getContext().setAuthentication(auth);

        chain.doFilter(req, resp);
    }
}
