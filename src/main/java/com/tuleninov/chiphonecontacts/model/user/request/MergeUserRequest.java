package com.tuleninov.chiphonecontacts.model.user.request;

import com.tuleninov.chiphonecontacts.model.constraints.NullableNotBlank;

import javax.validation.constraints.Email;

public record MergeUserRequest(

        @Email(message = "the email must be a valid email string")
        String email,

        @NullableNotBlank(message = "the nickname must not be blank")
        String nickname

) {
}
