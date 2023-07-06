package com.tuleninov.chiphonecontacts.exceptions.contact;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class ContactExceptions {

    public ContactExceptions() {
    }

    public static ResponseStatusException duplicateName(String name) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, "Name '" + name + "' already exists");
    }

    public static ResponseStatusException contactNotFound(String name) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Contact with name '" + name + "' not found");
    }
}
