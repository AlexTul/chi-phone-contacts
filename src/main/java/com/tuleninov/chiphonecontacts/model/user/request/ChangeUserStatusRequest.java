package com.tuleninov.chiphonecontacts.model.user.request;

import com.tuleninov.chiphonecontacts.model.user.UserStatus;

import javax.validation.constraints.NotNull;

public record ChangeUserStatusRequest(

        @NotNull UserStatus status

) {
}
