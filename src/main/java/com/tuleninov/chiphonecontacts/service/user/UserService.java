package com.tuleninov.chiphonecontacts.service.user;

import com.tuleninov.chiphonecontacts.exceptions.UserExceptions;
import com.tuleninov.chiphonecontacts.model.auth.CustomUserDetails;
import com.tuleninov.chiphonecontacts.model.user.CustomUser;
import com.tuleninov.chiphonecontacts.model.user.KnownAuthority;
import com.tuleninov.chiphonecontacts.model.user.UserAuthority;
import com.tuleninov.chiphonecontacts.model.user.UserStatus;
import com.tuleninov.chiphonecontacts.model.user.request.ChangeUserPasswordRequest;
import com.tuleninov.chiphonecontacts.model.user.request.MergeUserRequest;
import com.tuleninov.chiphonecontacts.model.user.request.OverrideUserPasswordRequest;
import com.tuleninov.chiphonecontacts.model.user.request.SaveUserRequest;
import com.tuleninov.chiphonecontacts.model.user.response.PasswordResponse;
import com.tuleninov.chiphonecontacts.model.user.response.UserResponse;
import com.tuleninov.chiphonecontacts.repository.AuthorityRepository;
import com.tuleninov.chiphonecontacts.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * The UserService for UserController.
 */
@Service
public class UserService implements UserDetailsService, UserOperations {

    private final UserRepository userRepository;

    private final AuthorityRepository authorityRepository;

    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       AuthorityRepository authorityRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.authorityRepository = authorityRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Get a new temporary password for the user.
     *
     * @param email user`s email
     * @return index page
     */
    @Override
    @Transactional
    public PasswordResponse createTemporaryPassword(String email) {
        CustomUser user = getUser(email);
        return new PasswordResponse(createTemporaryPassword(user));
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        CustomUser user = userRepository.findByEmailOrNickname(username, username)
                .orElseThrow(() -> new UsernameNotFoundException("User " + username + " not found"));

        return new CustomUserDetails(user);
    }

    /**
     * Find all users in the database with pagination information.
     *
     * @param pageable abstract interface for pagination information
     * @return all users from the database
     */
    @Override
    @Transactional(readOnly = true)
    public Page<UserResponse> list(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(UserResponse::fromUserWithBasicAttributes);
    }

    /**
     * Change user`s status to activate in the database.
     *
     * @param code activation code from user
     * @return user that has been activated in response format
     */
    @Override
    @Transactional
    public UserResponse activate(String code) {
        return UserResponse.fromUser(setUserStatusActive(code));
    }

    /**
     * Find the user by id in the database.
     *
     * @param id user`s id in database
     * @return current user from the database in response format
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<UserResponse> findById(long id) {
        return userRepository.findById(id)
                .map(UserResponse::fromUser);
    }

    /**
     * Find current user in the database.
     *
     * @param email authentication principal
     * @return current user from the database in response format
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<UserResponse> findByEmail(String email) {
        return userRepository.findByEmail(email).map(UserResponse::fromUser);
    }

    /**
     * Merge the user by id in the database.
     *
     * @param id      id of user
     * @param request request with user`s credentials
     * @return the user that was merged in response format
     */
    @Override
    @Transactional
    public UserResponse mergeById(long id, MergeUserRequest request) {
        CustomUser user = getUser(id);
        return UserResponse.fromUser(merge(user, request));
    }

    /**
     * Merge current user by email in the database.
     *
     * @param email   authentication principal
     * @param request request with user`s credentials
     * @return user that was merged
     */
    @Override
    @Transactional
    public UserResponse mergeByEmail(String email, MergeUserRequest request) {
        CustomUser user = getUser(email);
        return UserResponse.fromUser(merge(user, request));
    }

    /**
     * Create user as client in the database.
     *
     * @param request request with user`s fields
     * @param code    activation code
     * @return user who was created in response format
     */
    @Override
    @Transactional
    public UserResponse create(SaveUserRequest request, String code) {
        validateUniqueFields(request);
        return UserResponse.fromUser(save(request, getRegularUserAuthorities(), code));
    }

    /**
     * Register user as admin in the database.
     *
     * @param request request with admin`s credentials
     * @param code    activation code
     * @return user who was registered as admin in the database in response format
     */
    @Override
    @Transactional
    public UserResponse createAdmin(SaveUserRequest request, String code) {
        validateUniqueFields(request);
        return UserResponse.fromUser(save(request, getAdminAuthorities(), code));
    }

    /**
     * Change user`s status by id in the database.
     *
     * @param id     id of user
     * @param status user`s status
     * @return user whose status was changed in response format
     */
    @Override
    @Transactional
    public UserResponse changeStatusById(long id, UserStatus status) {
        CustomUser user = getUser(id);
        if (user.getStatus() != status) {
            user.setStatus(status);
        }
        return UserResponse.fromUser(user);
    }

    /**
     * Change user`s password by id in the database.
     *
     * @param id      id of user
     * @param request with password
     * @return user whose password was changed in response format
     */
    @Override
    @Transactional
    public UserResponse changePasswordById(long id, OverrideUserPasswordRequest request) {
        CustomUser user = getUser(id);
        user.setPassword(passwordEncoder.encode(request.password()));
        return UserResponse.fromUser(user);
    }

    /**
     * Change current user`s password by email in the database.
     *
     * @param email   authentication principal
     * @param request with password and confirm password
     * @return user who changed the password in response format
     */
    @Override
    @Transactional
    public UserResponse changePasswordByEmail(String email, ChangeUserPasswordRequest request) {
        CustomUser user = getUser(email);
        changePassword(user, request.oldPassword(), request.newPassword());
        return UserResponse.fromUser(user);
    }

    /**
     * Delete the user by id in the database.
     *
     * @param id id of user
     * @return the user what was deleted in response format
     */
    @Override
    @Transactional
    public Optional<UserResponse> deleteById(long id) {
        if (!userRepository.existsById(id)) throw UserExceptions.userNotFound(id);

        Optional<CustomUser> user = userRepository.findById(id);

        if (checkIsLastAdmin(user)) throw UserExceptions.lastAdmin(id);

        user.ifPresent(userRepository::delete);
        return user.map(UserResponse::fromUser);
    }

    /**
     * Delete current user by email in the database.
     *
     * @param email authentication principal
     */
    @Override
    @Transactional
    public void deleteByEmail(String email) {
        if (!userRepository.existsByEmail(email)) throw UserExceptions.userNotFound(email);
        userRepository.deleteByEmail(email);
    }

    /**
     * Merge admins in the database.
     *
     * @param requests requests with admins
     */
    @Transactional
    public void mergeAdmins(List<SaveUserRequest> requests) {
        if (requests.isEmpty()) return;
        Map<KnownAuthority, UserAuthority> authorities = getAdminAuthorities();
        for (SaveUserRequest request : requests) {
            String email = request.email();
            String nickname = request.nickname();
            CustomUser user = userRepository.findByEmail(email).orElseGet(() -> {
                var newUser = new CustomUser();
                newUser.setCreatedAt(OffsetDateTime.now());
                newUser.setEmail(email);
                return newUser;
            });
            if (!nickname.equals(user.getNickname())) {
                if (userRepository.existsByNickname(nickname)) throw UserExceptions.duplicateNickname(nickname);
                user.setNickname(nickname);
            }
            user.setPassword(passwordEncoder.encode(request.password()));
            user.getAuthorities().putAll(authorities);
            userRepository.save(user);
        }
    }

    /**
     * Get user by activation code in the database.
     *
     * @param code the activation code
     * @return user
     */
    private CustomUser getUserByCode(String code) {
        return userRepository.findByActivationCode(code)
                .orElseThrow(() -> UserExceptions.userByCodeNotFound(code));
    }

    /**
     * Get the user by id in the database.
     *
     * @param id database user id
     * @return the entity with the given id
     */
    private CustomUser getUser(long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> UserExceptions.userNotFound(id));
    }

    /**
     * Get current user in the database.
     *
     * @param email authentication principal
     * @return current user
     */
    private CustomUser getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> UserExceptions.userNotFound(email));
    }

    /**
     * Create temporary password for user in the database.
     *
     * @param user the user from the database
     * @return the encode password
     */
    private String createTemporaryPassword(CustomUser user) {
        var temporaryPassword = generateRandomPassword(64);
        String encodePassword = passwordEncoder.encode(temporaryPassword);
        user.setPassword(encodePassword);
        return temporaryPassword;
    }

    /**
     * Generate random password.
     *
     * @param length password length
     * @return encode password
     */
    public static String generateRandomPassword(int length) {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()";
        var random = new SecureRandom();
        StringBuilder password = new StringBuilder();

        for (int i = 0; i < length; i++) {
            int randomIndex = random.nextInt(characters.length());
            char randomChar = characters.charAt(randomIndex);
            password.append(randomChar);
        }

        return password.toString();
    }

    /**
     * Merge the user in the database.
     *
     * @param user    the user from the database
     * @param request request with user`s credentials
     * @return the user that was merged
     */
    private CustomUser merge(CustomUser user, MergeUserRequest request) {
        String email = request.email();
        if (email != null && !email.equals(user.getEmail())) {
            if (userRepository.existsByEmail(email)) throw UserExceptions.duplicateEmail(email);
            user.setEmail(email);
        }
        String nickname = request.nickname();
        if (nickname != null && !nickname.equals(user.getNickname())) {
            if (userRepository.existsByNickname(nickname)) throw UserExceptions.duplicateNickname(nickname);
            user.setNickname(nickname);
        }
        return user;
    }

    /**
     * Save user in the database.
     *
     * @param request     request with user`s fields
     * @param authorities user`s authorities
     * @param code        activation code from user
     * @return the saved entity
     */
    private CustomUser save(SaveUserRequest request, Map<KnownAuthority, UserAuthority> authorities, String code) {
        var user = new CustomUser();
        user.setEmail(request.email());
        user.setNickname(request.nickname());
        user.setStatus(UserStatus.SUSPENDED);
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setCreatedAt(OffsetDateTime.now());
        user.getAuthorities().putAll(authorities);
        user.setActivationCode(code);
        userRepository.save(user);
        return user;
    }

    /**
     * Set user`s status is active in the database.
     *
     * @param code activation code from user
     * @return user that has been activated in response format
     */
    private CustomUser setUserStatusActive(String code) {
        CustomUser user = getUserByCode(code);
        user.setStatus(UserStatus.ACTIVE);
        return user;
    }

    /**
     * Change current user`s password in the database.
     *
     * @param user        current user
     * @param oldPassword old password
     * @param newPassword new password
     */
    private void changePassword(CustomUser user, String oldPassword, String newPassword) {
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw UserExceptions.wrongPassword();
        }
        user.setPassword(passwordEncoder.encode(newPassword));
    }

    /**
     * Get admin`s authorities.
     *
     * @return admin`s authorities
     */
    private Map<KnownAuthority, UserAuthority> getAdminAuthorities() {
        return authorityRepository.findAllByIdIn(AuthorityRepository.ADMIN_AUTHORITIES)
                .collect(Collectors.toMap(
                        UserAuthority::getId,
                        Function.identity(),
                        (e1, e2) -> e2,
                        () -> new EnumMap<>(KnownAuthority.class)));
    }

    /**
     * Get user`s authorities.
     *
     * @return user`s authorities
     */
    private Map<KnownAuthority, UserAuthority> getRegularUserAuthorities() {
        UserAuthority authority = authorityRepository
                .findById(KnownAuthority.ROLE_USER)
                .orElseThrow(() -> UserExceptions.authorityNotFound(KnownAuthority.ROLE_USER.name()));
        Map<KnownAuthority, UserAuthority> authorities = new EnumMap<>(KnownAuthority.class);
        authorities.put(KnownAuthority.ROLE_USER, authority);
        return authorities;
    }

    /**
     * Validate user`s fields.
     *
     * @param request request with user`s fields
     */
    private void validateUniqueFields(SaveUserRequest request) {
        String email = request.email();
        if (userRepository.existsByEmail(email)) {
            throw UserExceptions.duplicateEmail(email);
        }
        String nickname = request.nickname();
        if (userRepository.existsByNickname(nickname)) {
            throw UserExceptions.duplicateNickname(nickname);
        }
    }

    /**
     * Check if is the last admin in the database.
     *
     * @param user the user who wants to be removed from the database
     * @return true - if is the last admin in the database
     */
    private boolean checkIsLastAdmin(Optional<CustomUser> user) {
        AtomicBoolean isLastAdmin = new AtomicBoolean(false);
        user.ifPresent(customUser -> {
            if (customUser.getAuthorities().containsKey(KnownAuthority.ROLE_ADMIN)) {
                UserAuthority authority = authorityRepository
                        .findById(KnownAuthority.ROLE_ADMIN)
                        .orElseThrow(() -> UserExceptions.authorityNotFound(KnownAuthority.ROLE_ADMIN.name()));
                Set<CustomUser> admins = authority.getUsers();
                if (admins.size() == 1) {
                    isLastAdmin.set(true);
                }
            }
        });
        return isLastAdmin.get();
    }
}
