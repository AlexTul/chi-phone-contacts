package com.tuleninov.chiphonecontacts.controller.user;

import com.tuleninov.chiphonecontacts.Routes;
import com.tuleninov.chiphonecontacts.model.user.request.*;
import com.tuleninov.chiphonecontacts.model.user.response.PasswordResponse;
import com.tuleninov.chiphonecontacts.model.user.response.UserResponse;
import com.tuleninov.chiphonecontacts.service.user.UserOperations;
import io.swagger.v3.oas.annotations.Parameter;
import org.springdoc.core.converters.models.PageableAsQueryParam;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import static com.tuleninov.chiphonecontacts.exceptions.UserExceptions.userNotFound;

/**
 * Controller for user registration, authenticated user API, admin-only API.
 */
@RestController
@RequestMapping(Routes.USERS)
public class UserController {

    private final UserOperations userOperations;

    public UserController(UserOperations userOperations) {
        this.userOperations = userOperations;
    }

    //region user forgot password

    /**
     * Get a new temporary password for the user.
     *
     * @param request the request with user`s parameters
     * @return temporary password
     */
    @PatchMapping(
            value = "/forgot",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public PasswordResponse getTemporaryPassword(@RequestBody @Valid ForgotPasswordRequest request) {
        return userOperations.createTemporaryPassword(request.email());
    }

    //endregion

    //region user registration

    /**
     * Register user as client in the database.
     *
     * @param request request with admin`s credentials
     * @param code    activation code
     * @return user who was registered in response format
     */
    @PostMapping(
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(@RequestBody @Valid SaveUserRequest request, @RequestParam String code) {
        return userOperations.create(request, code);
    }

    /**
     * Activate the user in the database.
     *
     * @param code activation code from user
     * @return user that has been activated in response format
     */
    @GetMapping(
            value = "/{code}/activate",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public UserResponse activate(@PathVariable String code) {
        return userOperations.activate(code);
    }

    //endregion

    //region authenticated user API

    /**
     * Get current user from the database.
     *
     * @param email authentication principal
     * @return current user from the database in response format
     */
    @GetMapping(value = "/me",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public UserResponse getCurrentUser(@AuthenticationPrincipal String email) {
        return userOperations.findByEmail(email).orElseThrow(() -> userNotFound(email));
    }

    /**
     * Merge current user in the database.
     *
     * @param email   authentication principal
     * @param request request with user`s credentials
     * @return user that was merged
     */
    @PatchMapping(
            value = "/me",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public UserResponse mergeCurrentUser(@AuthenticationPrincipal String email,
                                         @RequestBody @Valid MergeUserRequest request) {
        return userOperations.mergeByEmail(email, request);
    }

    /**
     * Change current user`s password in the database.
     *
     * @param email   authentication principal
     * @param request with password and confirm password
     * @return user who changed the password in response format
     */
    @PatchMapping(
            value = "/me/password",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public UserResponse changeCurrentUserPassword(@AuthenticationPrincipal String email,
                                                  @RequestBody @Valid ChangeUserPasswordRequest request) {
        return userOperations.changePasswordByEmail(email, request);
    }

    /**
     * Delete current user in the database.
     *
     * @param email authentication principal
     */
    @DeleteMapping("/me")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCurrentUser(@AuthenticationPrincipal String email) {
        userOperations.deleteByEmail(email);
    }

    /**
     * Get the user by id in the database.
     *
     * @param id user`s id in the database
     * @return current user from the database in response format
     */
    @GetMapping(
            value = "/{id}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public UserResponse getUserById(@PathVariable long id) {
        return userOperations.findById(id).orElseThrow(() -> userNotFound(id));
    }

    //endregion

    //region admin-only API

    /**
     * Get all users from the database with pagination information.
     *
     * @param pageable abstract interface for pagination information
     * @return all users from the database in response format
     */
    @GetMapping(
            value = "/admins",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PageableAsQueryParam
    public Page<UserResponse> listUsers(@Parameter(hidden = true) Pageable pageable) {
        return userOperations.list(pageable);
    }

    /**
     * Register the user as admin in the database.
     *
     * @param request request with admin`s credentials
     * @param code    activation code
     * @return user who was registered in response format
     */
    @PostMapping(
            value = "/admins",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse registerAdmin(@RequestBody @Valid SaveUserRequest request, @RequestParam String code) {
        return userOperations.createAdmin(request, code);
    }

    /**
     * Merge the user by id in the database.
     *
     * @param id      id of user
     * @param request request with user`s credentials
     * @return user that was merged in response format
     */
    @PatchMapping(
            value = "/{id}",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public UserResponse mergeUserById(@PathVariable long id,
                                      @RequestBody @Valid MergeUserRequest request) {
        return userOperations.mergeById(id, request);
    }

    /**
     * Change user`s status by id in the database.
     *
     * @param id      id of user
     * @param request request with user`s status
     * @return user whose status was changed in response format
     */
    @PatchMapping(
            value = "/{id}/status",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public UserResponse changeUserStatusById(@PathVariable long id,
                                             @RequestBody @Valid ChangeUserStatusRequest request) {
        return userOperations.changeStatusById(id, request.status());
    }

    /**
     * Change user`s password by id in the database.
     *
     * @param id      id of user
     * @param request with password
     * @return user whose password was changed in response format
     */
    @PatchMapping(
            value = "/{id}/password",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public UserResponse changeUserPassword(@PathVariable long id,
                                           @RequestBody @Valid OverrideUserPasswordRequest request) {
        return userOperations.changePasswordById(id, request);
    }

    /**
     * Delete the user by id in the database.
     *
     * @param id id of user
     * @return user what was deleted in response format
     */
    @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public UserResponse deleteUserById(@PathVariable long id) {
        return userOperations.deleteById(id)
                .orElseThrow(() -> userNotFound(id));
    }

    //endregion
}
