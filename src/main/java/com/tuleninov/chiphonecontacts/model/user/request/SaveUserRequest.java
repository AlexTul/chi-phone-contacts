package com.tuleninov.chiphonecontacts.model.user.request;

import javax.validation.constraints.*;

public record SaveUserRequest(

        @Email(message = "the email must be a valid email string")
        @NotNull(message = "the email must not be null")
        String email,

        @NotBlank(message = "the password must not be blank")
        @Size(min = 8, message = "the password's length must be at least 8")
        @Pattern(regexp = "^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=\\S+$).{8,}$", message = "the password must contain at " +
                "least one number, at least one lowercase letter, at least one uppercase letter, must not contain spaces")
        String password,

        @NotBlank(message = "the nickname must not be blank")
        @Size(min = 2, message = "nickname's length must be at least 2")
        @Pattern(regexp = "^[A-Za-zА-ЩЬЮЯҐІЇЄа-щьюяґіїє'\\- ]{2,}", message = "the nickname must be only characters " +
                "of the Latin and Ukrainian alphabets, apostrophes, hyphens and spaces")
        String nickname

) {
}
