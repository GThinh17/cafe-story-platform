package com.cafestory.until.security;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public final class AuthenticationPrincipalUtils {

    private AuthenticationPrincipalUtils() {
    }

    public static UUID requireUserId(AuthenticatedUserPrincipal principal) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication is required");
        }
        return principal.userId();
    }
}
