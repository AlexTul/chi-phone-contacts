package com.tuleninov.chiphonecontacts.model.auth.response;

import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public record AccessTokenResponse(String accessToken,
                                  String refreshToken,
                                  long expireIn,
                                  Collection<? extends GrantedAuthority> authorities) {
}
