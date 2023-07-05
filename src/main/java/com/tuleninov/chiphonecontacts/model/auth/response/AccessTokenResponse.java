package com.tuleninov.chiphonecontacts.model.auth.response;

public record AccessTokenResponse(String accessToken,
                                  String refreshToken,
                                  long expireIn,
                                  String authorities) {
}
