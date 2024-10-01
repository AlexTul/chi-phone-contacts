package com.tuleninov.chiphonecontacts.model.contact.request;

import javax.validation.Valid;
import javax.validation.constraints.*;
import java.util.List;

/**
 * Record for the Contact request.
 *
 * @author Oleksandr Tuleninov
 * @version 01
 */
public record SaveContactRequest(

        @NotBlank(message = "the name must not be blank")
        String name,

        @Valid
        @NotNull(message = "the email`s list must not be null")
        List<
                @NotBlank(message = "the email must not be blank") @Email(message = "the emails must be a valid email string")
                        String> emails,

        @Valid
        @NotNull(message = "the phone`s list must not be null")
        List<
                @NotBlank(message = "the phone must not be blank")
                @Size(min = 13, max = 13, message = "phone's length must be at 13")
                @Pattern(regexp = "^\\+380\\d{9}$", message = "the phones must be in the format +380XXXXXXXXX")
                        String> phones

) {
}
