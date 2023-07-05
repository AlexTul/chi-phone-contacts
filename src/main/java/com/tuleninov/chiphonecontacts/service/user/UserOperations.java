package com.tuleninov.chiphonecontacts.service.user;

import com.tuleninov.chiphonecontacts.model.user.UserStatus;
import com.tuleninov.chiphonecontacts.model.user.request.ChangeUserPasswordRequest;
import com.tuleninov.chiphonecontacts.model.user.request.MergeUserRequest;
import com.tuleninov.chiphonecontacts.model.user.request.OverrideUserPasswordRequest;
import com.tuleninov.chiphonecontacts.model.user.request.SaveUserRequest;
import com.tuleninov.chiphonecontacts.model.user.response.PasswordResponse;
import com.tuleninov.chiphonecontacts.model.user.response.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface UserOperations {

    PasswordResponse createTemporaryPassword(String email);

    Page<UserResponse> list(Pageable pageable);

    UserResponse activate(String code);

    Optional<UserResponse> findById(long id);

    Optional<UserResponse> findByEmail(String email);

    UserResponse mergeById(long id, MergeUserRequest request);

    UserResponse mergeByEmail(String email, MergeUserRequest request);

    UserResponse create(SaveUserRequest request, String activationCode);

    UserResponse createAdmin(SaveUserRequest request, String activationCode);

    UserResponse changeStatusById(long id, UserStatus status);

    UserResponse changePasswordById(long id, OverrideUserPasswordRequest request);

    UserResponse changePasswordByEmail(String email, ChangeUserPasswordRequest request);

    Optional<UserResponse> deleteById(long id);

    void deleteByEmail(String email);

}
