package com.tuleninov.chiphonecontacts.service.auth;

import com.tuleninov.chiphonecontacts.exceptions.auth.InvalidRefreshTokenException;
import com.tuleninov.chiphonecontacts.model.auth.CustomUserDetails;
import com.tuleninov.chiphonecontacts.model.auth.response.AccessTokenResponse;

public interface AuthOperations {

    AccessTokenResponse getToken(CustomUserDetails userDetails);

    AccessTokenResponse refreshToken(String refreshToken) throws InvalidRefreshTokenException;

    void invalidateToken(String refreshToken, String ownerEmail) throws InvalidRefreshTokenException;

}
