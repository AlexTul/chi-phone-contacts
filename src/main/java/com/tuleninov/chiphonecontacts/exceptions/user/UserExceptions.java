package com.tuleninov.chiphonecontacts.exceptions.user;

import com.tuleninov.chiphonecontacts.exceptions.auth.InvalidRefreshTokenException;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public final class UserExceptions {

    private UserExceptions() {
    }

    public static ResponseStatusException authorityNotFound(String value) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "User authority '" + value + "' not defined");
    }

    public static ResponseStatusException userByCodeNotFound(String activationCode) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "User with activation code '" + activationCode + "' not found");
    }

    public static ResponseStatusException userNotFound(String email) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "User with email '" + email + "' not found");
    }

    public static ResponseStatusException userNotFound(long id) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "User with id '" + id + "' not found");
    }

    public static ResponseStatusException duplicateEmail(String email) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email '" + email + "' already taken");
    }

    public static ResponseStatusException duplicateNickname(String nickname) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nickname '" + nickname + "' already taken");
    }

    public static ResponseStatusException invalidRefreshToken(InvalidRefreshTokenException cause) {
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                "Refresh token is invalid! It may have been rotated, invalidated or expired naturally", cause);
    }

    public static ResponseStatusException wrongPassword() {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password is incorrect");
    }

    public static ResponseStatusException lastAdmin(long id) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, "This is the last admin in the database. Id: '" + id + "'.");
    }
}
