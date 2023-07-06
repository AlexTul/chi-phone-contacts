package com.tuleninov.chiphonecontacts.exceptions.email;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class EmailExceptions {

    public EmailExceptions() {
    }

    public static ResponseStatusException duplicateEmail(String email) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email '" + email + "' already exists");
    }
}
