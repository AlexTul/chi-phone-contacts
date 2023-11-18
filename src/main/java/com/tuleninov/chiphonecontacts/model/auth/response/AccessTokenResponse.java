package com.tuleninov.chiphonecontacts.model.auth.response;

import com.tuleninov.chiphonecontacts.model.user.KnownAuthority;

import java.util.Set;

public record AccessTokenResponse(String accessToken,
                                  String refreshToken,
                                  long expireIn,
                                  Set<KnownAuthority> authorities) {
}
