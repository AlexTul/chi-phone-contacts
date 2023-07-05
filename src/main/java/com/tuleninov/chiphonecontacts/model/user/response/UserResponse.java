package com.tuleninov.chiphonecontacts.model.user.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.tuleninov.chiphonecontacts.Routes;
import com.tuleninov.chiphonecontacts.model.ResourceResponse;
import com.tuleninov.chiphonecontacts.model.user.CustomUser;
import com.tuleninov.chiphonecontacts.model.user.KnownAuthority;
import com.tuleninov.chiphonecontacts.model.user.UserStatus;

import java.time.OffsetDateTime;
import java.util.EnumSet;
import java.util.Set;

public record UserResponse(long id,
                           String email,
                           String nickname,
                           UserStatus status,
                           @JsonFormat(shape = JsonFormat.Shape.STRING)
                           OffsetDateTime createdAt,
                           @JsonInclude(JsonInclude.Include.NON_NULL)
                           Set<KnownAuthority> authorities) implements ResourceResponse {

    public static UserResponse fromUser(CustomUser user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getStatus(),
                user.getCreatedAt(),
                EnumSet.copyOf(user.getAuthorities().keySet()));
    }

    // only the attributes that don't require extra fetching
    public static UserResponse fromUserWithBasicAttributes(CustomUser user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getStatus(),
                user.getCreatedAt(),
                null);
    }

    @Override
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public String path() {
        return Routes.user(id);
    }
}
