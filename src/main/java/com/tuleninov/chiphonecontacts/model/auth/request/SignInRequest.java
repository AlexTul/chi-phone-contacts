package com.tuleninov.chiphonecontacts.model.auth.request;

import com.fasterxml.jackson.annotation.JsonAlias;

public record SignInRequest(

        @JsonAlias({"username", "email"})
        String login,
        String password

) {
}
