package com.tuleninov.chiphonecontacts.exceptions.phone;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class PhoneExceptions {

    public PhoneExceptions() {
    }

    public static ResponseStatusException duplicatePhone(String phone) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, "Phone '" + phone + "' already exists");
    }
}
