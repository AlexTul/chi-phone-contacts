package com.tuleninov.chiphonecontacts.model.user.request;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;

public record ForgotPasswordRequest(

        @NotNull(message = "the email must not be null")
        @Email(message = "the email must be a valid email string")
        String email

) {
}
