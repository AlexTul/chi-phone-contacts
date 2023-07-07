package com.tuleninov.chiphonecontacts.service;

import com.tuleninov.chiphonecontacts.config.security.PasswordEncoderConfig;
import com.tuleninov.chiphonecontacts.exceptions.user.UserExceptions;
import com.tuleninov.chiphonecontacts.model.user.CustomUser;
import com.tuleninov.chiphonecontacts.model.user.KnownAuthority;
import com.tuleninov.chiphonecontacts.model.user.UserAuthority;
import com.tuleninov.chiphonecontacts.model.user.UserStatus;
import com.tuleninov.chiphonecontacts.model.user.request.SaveUserRequest;
import com.tuleninov.chiphonecontacts.model.user.response.UserResponse;
import com.tuleninov.chiphonecontacts.repository.AuthorityRepository;
import com.tuleninov.chiphonecontacts.repository.UserRepository;
import com.tuleninov.chiphonecontacts.service.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.*;

public class UserServiceTest {

    private UserRepository userRepository;
    private AuthorityRepository authorityRepository;
    private PasswordEncoder passwordEncoder;
    private UserService userService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        authorityRepository = mock(AuthorityRepository.class);
        passwordEncoder = new PasswordEncoderConfig().passwordEncoder();
        userService = new UserService(userRepository, authorityRepository, passwordEncoder);
    }

    @Test
    void testList() {
        var presentId = 1L;
        var presentEmail = "test@gmail.com";
        var presentNickname = "test";

        var user = new CustomUser();
        user.setId(presentId);
        user.setEmail(presentEmail);
        user.setNickname(presentNickname);
        user.setStatus(UserStatus.ACTIVE);
        user.setCreatedAt(OffsetDateTime.now());
        user.getAuthorities().put(KnownAuthority.ROLE_USER, null);

        List<CustomUser> userList = List.of(user);
        Pageable pageable = PageRequest.of(0, 10, Sort.by("id").descending());
        Page<CustomUser> userPage = new PageImpl<>(userList, pageable, userList.size());

        when(userRepository.findAll(pageable)).thenReturn(userPage);

        Page<UserResponse> presentResponse = userService.list(pageable);

        assertThat(Optional.of(presentResponse.stream().toList().get(0))).hasValueSatisfying(userResponse ->
                assertUserMatchesResponseWithBasicAttributes(user, userResponse));
        verify(userRepository).findAll(pageable);

        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void testFindById() {
        var absentId = 100L;
        var presentId = 1L;
        var presentEmail = "test@gmail.com";
        var presentNickname = "test";

        var user = new CustomUser();
        user.setId(presentId);
        user.setEmail(presentEmail);
        user.setNickname(presentNickname);
        user.setStatus(UserStatus.ACTIVE);
        user.setCreatedAt(OffsetDateTime.now());
        user.getAuthorities().put(KnownAuthority.ROLE_USER, null);

        when(userRepository.findById(absentId)).thenReturn(Optional.empty());
        when(userRepository.findById(presentId)).thenReturn(Optional.of(user));

        Optional<UserResponse> absentResponse = userService.findById(absentId);

        assertThat(absentResponse).isEmpty();
        verify(userRepository).findById(absentId);

        Optional<UserResponse> presentResponse = userService.findById(presentId);

        assertThat(presentResponse).hasValueSatisfying(userResponse ->
                assertUserMatchesResponse(user, userResponse));
        verify(userRepository).findById(presentId);

        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void testFindByEmail() {
        var absentEmail = "absent@gmail.com";
        var presentId = 1L;
        var presentEmail = "test@gmail.com";
        var presentNickname = "test";

        var user = new CustomUser();
        user.setId(presentId);
        user.setEmail(presentEmail);
        user.setNickname(presentNickname);
        user.setStatus(UserStatus.ACTIVE);
        user.setCreatedAt(OffsetDateTime.now());
        user.getAuthorities().put(KnownAuthority.ROLE_USER, null);

        when(userRepository.findByEmail(absentEmail)).thenReturn(Optional.empty());
        when(userRepository.findByEmail(presentEmail)).thenReturn(Optional.of(user));

        Optional<UserResponse> absentResponse = userService.findByEmail(absentEmail);

        assertThat(absentResponse).isEmpty();
        verify(userRepository).findByEmail(absentEmail);

        Optional<UserResponse> presentResponse = userService.findByEmail(presentEmail);

        assertThat(presentResponse).hasValueSatisfying(userResponse ->
                assertUserMatchesResponse(user, userResponse));
        verify(userRepository).findByEmail(presentEmail);

        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void testCreate() {
        var presentId = 1L;
        var presentEmail = "test@gmail.com";
        var presentNickname = "test";
        var password = "fua9eeF1ahphooy5eth9ooth1iebee0AeWahyob4lai4cha3goohaewoh0gicieB";
        var encodePassword = passwordEncoder.encode(password);
        var createdAt = OffsetDateTime.now();
        var code = UUID.randomUUID().toString();
        var userAuthority = new UserAuthority();
        userAuthority.setId(KnownAuthority.ROLE_USER);
        var request = new SaveUserRequest(presentEmail, password, presentNickname);

        var user = new CustomUser();
        user.setId(presentId);
        user.setEmail(presentEmail);
        user.setNickname(presentNickname);
        user.setStatus(UserStatus.SUSPENDED);
        user.setPassword(encodePassword);
        user.setCreatedAt(createdAt);
        user.setActivationCode(code);
        user.getAuthorities().put(KnownAuthority.ROLE_USER, userAuthority);

        when(authorityRepository.findById(KnownAuthority.ROLE_USER)).thenReturn(Optional.of(userAuthority));
        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(userRepository.existsByNickname(request.nickname())).thenReturn(false);
        when(userRepository.save(notNull())).thenAnswer(invocation -> {
            CustomUser entity = invocation.getArgument(0);
            assertThat(entity.getId()).isNull();
            assertThat(entity.getEmail()).isEqualTo(request.email());
            assertThat(entity.getNickname()).isEqualTo(request.nickname());
            assertThat(entity.getStatus()).isEqualTo(UserStatus.SUSPENDED);
            assertThat(entity.getCreatedAt()).isEqualToIgnoringSeconds(createdAt);
            assertThat(entity.getAuthorities()).isEqualTo(user.getAuthorities());
            assertThat(entity.getActivationCode()).isEqualTo(code);
            entity.setId(presentId);
            entity.setCreatedAt(createdAt);
            return entity;
        });

        UserResponse presentResponse = userService.create(request, code);

        assertThat(Optional.of(presentResponse)).hasValueSatisfying(userResponse ->
                assertUserMatchesResponse(user, userResponse));
        verify(authorityRepository).findById(KnownAuthority.ROLE_USER);
        verify(userRepository).save(notNull());
        verify(userRepository).existsByEmail(request.email());
        verify(userRepository).existsByNickname(request.nickname());

        verifyNoMoreInteractions(authorityRepository, userRepository);
    }

    @Test
    void testCreateAdmin() {
        var presentId = 1L;
        var presentEmail = "test@gmail.com";
        var presentNickname = "test";
        var password = "fua9eeF1ahphooy5eth9ooth1iebee0AeWahyob4lai4cha3goohaewoh0gicieB";
        var encodePassword = passwordEncoder.encode(password);
        var createdAt = OffsetDateTime.now();
        var code = UUID.randomUUID().toString();
        var adminAuthority = new UserAuthority();
        adminAuthority.setId(KnownAuthority.ROLE_ADMIN);
        var request = new SaveUserRequest(presentEmail, password, presentNickname);

        var user = new CustomUser();
        user.setId(presentId);
        user.setEmail(presentEmail);
        user.setNickname(presentNickname);
        user.setStatus(UserStatus.SUSPENDED);
        user.setPassword(encodePassword);
        user.setCreatedAt(createdAt);
        user.setActivationCode(code);
        user.getAuthorities().put(KnownAuthority.ROLE_ADMIN, adminAuthority);

        when(authorityRepository.findAllByIdIn(AuthorityRepository.ADMIN_AUTHORITIES)).thenReturn(Stream.of(adminAuthority));
        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(userRepository.existsByNickname(request.nickname())).thenReturn(false);
        when(userRepository.save(notNull())).thenAnswer(invocation -> {
            CustomUser entity = invocation.getArgument(0);
            assertThat(entity.getId()).isNull();
            assertThat(entity.getEmail()).isEqualTo(request.email());
            assertThat(entity.getNickname()).isEqualTo(request.nickname());
            assertThat(entity.getStatus()).isEqualTo(UserStatus.SUSPENDED);
            assertThat(entity.getCreatedAt()).isEqualToIgnoringSeconds(createdAt);
            assertThat(entity.getAuthorities()).isEqualTo(user.getAuthorities());
            assertThat(entity.getActivationCode()).isEqualTo(code);
            entity.setId(presentId);
            entity.setCreatedAt(createdAt);
            return entity;
        });

        UserResponse presentResponse = userService.createAdmin(request, code);

        assertThat(Optional.of(presentResponse)).hasValueSatisfying(userResponse ->
                assertUserMatchesResponse(user, userResponse));
        verify(authorityRepository).findAllByIdIn(AuthorityRepository.ADMIN_AUTHORITIES);
        verify(userRepository).save(notNull());
        verify(userRepository).existsByEmail(request.email());
        verify(userRepository).existsByNickname(request.nickname());

        verifyNoMoreInteractions(authorityRepository, userRepository);
    }

    @Test
    void testActivate() {
        var presentId = 1L;
        var presentEmail = "test@gmail.com";
        var presentNickname = "test";
        var absentCode = UUID.randomUUID().toString();
        var presentCode = UUID.randomUUID().toString();

        var customUser = new CustomUser();
        customUser.setId(presentId);
        customUser.setEmail(presentEmail);
        customUser.setNickname(presentNickname);
        customUser.setStatus(UserStatus.SUSPENDED);
        customUser.setCreatedAt(OffsetDateTime.now());
        customUser.getAuthorities().put(KnownAuthority.ROLE_USER, null);

        when(userRepository.findByActivationCode(absentCode)).thenThrow(UserExceptions.userByCodeNotFound(absentCode));
        when(userRepository.findByActivationCode(presentCode)).thenReturn(Optional.of(customUser));

        assertThrows(ResponseStatusException.class, () -> userService.activate(absentCode));
        verify(userRepository).findByActivationCode(absentCode);

        Optional<UserResponse> presentResponse = Optional.of(userService.activate(presentCode));

        assertThat(presentResponse).hasValueSatisfying(userResponse ->
                assertUserMatchesResponseActive(customUser, userResponse));
        verify(userRepository).findByActivationCode(presentCode);

        verifyNoMoreInteractions(userRepository);
    }

    private static void assertUserMatchesResponseWithBasicAttributes(CustomUser user, UserResponse userResponse) {
        assertThat(userResponse.id()).isEqualTo(user.getId());
        assertThat(userResponse.email()).isEqualTo(user.getEmail());
        assertThat(userResponse.nickname()).isEqualTo(user.getNickname());
        assertThat(userResponse.status()).isEqualTo(user.getStatus());
        assertThat(userResponse.createdAt()).isEqualTo(user.getCreatedAt());
        assertThat(userResponse.authorities()).isEqualTo(null);
    }

    private static void assertUserMatchesResponse(CustomUser user, UserResponse userResponse) {
        assertThat(userResponse.id()).isEqualTo(user.getId());
        assertThat(userResponse.email()).isEqualTo(user.getEmail());
        assertThat(userResponse.nickname()).isEqualTo(user.getNickname());
        assertThat(userResponse.status()).isEqualTo(user.getStatus());
        assertThat(userResponse.createdAt()).isEqualTo(user.getCreatedAt());
        assertThat(userResponse.authorities()).isEqualTo(user.getAuthorities().keySet());
    }

    private static void assertUserMatchesResponseActive(CustomUser user, UserResponse userResponse) {
        assertThat(userResponse.id()).isEqualTo(user.getId());
        assertThat(userResponse.email()).isEqualTo(user.getEmail());
        assertThat(userResponse.nickname()).isEqualTo(user.getNickname());
        assertThat(UserStatus.ACTIVE).isEqualTo(user.getStatus());
        assertThat(userResponse.createdAt()).isEqualTo(user.getCreatedAt());
        assertThat(userResponse.authorities()).isEqualTo(user.getAuthorities().keySet());
    }
}
