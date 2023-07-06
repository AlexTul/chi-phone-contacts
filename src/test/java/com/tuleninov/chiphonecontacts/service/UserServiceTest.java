package com.tuleninov.chiphonecontacts.service;

import com.tuleninov.chiphonecontacts.config.security.PasswordEncoderConfig;
import com.tuleninov.chiphonecontacts.model.user.CustomUser;
import com.tuleninov.chiphonecontacts.model.user.KnownAuthority;
import com.tuleninov.chiphonecontacts.model.user.UserStatus;
import com.tuleninov.chiphonecontacts.model.user.response.UserResponse;
import com.tuleninov.chiphonecontacts.repository.AuthorityRepository;
import com.tuleninov.chiphonecontacts.repository.UserRepository;
import com.tuleninov.chiphonecontacts.service.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.OffsetDateTime;
import java.util.EnumSet;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
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
    void testFindById() {
        var absentId = 100L;
        var presentId = 1L;
        var presentEmail = "test@gmail.com";
        var presentNickname = "test";

        var customUser = new CustomUser();
        customUser.setId(presentId);
        customUser.setEmail(presentEmail);
        customUser.setNickname(presentNickname);
        customUser.setStatus(UserStatus.ACTIVE);
        customUser.setCreatedAt(OffsetDateTime.now());
        customUser.getAuthorities().put(KnownAuthority.ROLE_USER, null);

        when(userRepository.findById(absentId)).thenReturn(Optional.empty());
        when(userRepository.findById(presentId)).thenReturn(Optional.of(customUser));

        Optional<UserResponse> absentResponse = userService.findById(absentId);

        assertThat(absentResponse).isEmpty();
        verify(userRepository).findById(absentId);

        Optional<UserResponse> presentResponse = userService.findById(presentId);

        assertThat(presentResponse).hasValueSatisfying(userResponse ->
                assertUserMatchesResponse(customUser, userResponse));
        verify(userRepository).findById(presentId);

        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void testFindByEmail() {
        var absentEmail = "absent@gmail.com";
        var presentId = 1L;
        var presentEmail = "test@gmail.com";
        var presentNickname = "test";

        var customUser = new CustomUser();
        customUser.setId(presentId);
        customUser.setEmail(presentEmail);
        customUser.setNickname(presentNickname);
        customUser.setStatus(UserStatus.ACTIVE);
        customUser.setCreatedAt(OffsetDateTime.now());
        customUser.getAuthorities().put(KnownAuthority.ROLE_USER, null);

        when(userRepository.findByEmail(absentEmail)).thenReturn(Optional.empty());
        when(userRepository.findByEmail(presentEmail)).thenReturn(Optional.of(customUser));

        Optional<UserResponse> absentResponse = userService.findByEmail(absentEmail);

        assertThat(absentResponse).isEmpty();
        verify(userRepository).findByEmail(absentEmail);

        Optional<UserResponse> presentResponse = userService.findByEmail(presentEmail);

        assertThat(presentResponse).hasValueSatisfying(userResponse ->
                assertUserMatchesResponse(customUser, userResponse));
        verify(userRepository).findByEmail(presentEmail);

        verifyNoMoreInteractions(userRepository);
    }

    private static void assertUserMatchesResponse(CustomUser user, UserResponse userResponse) {
        assertThat(userResponse.id()).isEqualTo(user.getId());
        assertThat(userResponse.email()).isEqualTo(user.getEmail());
        assertThat(userResponse.nickname()).isEqualTo(user.getNickname());
        assertThat(userResponse.status()).isEqualTo(user.getStatus());
        assertThat(userResponse.createdAt()).isEqualTo(user.getCreatedAt());
        assertThat(userResponse.authorities()).isEqualTo(EnumSet.copyOf(user.getAuthorities().keySet()));
    }
}
