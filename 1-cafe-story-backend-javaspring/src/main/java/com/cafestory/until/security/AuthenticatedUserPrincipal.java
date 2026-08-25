package com.cafestory.until.security;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

public record AuthenticatedUserPrincipal(UUID userId, String username, List<String> roles) implements Principal {

    /**
     * Trả userId để STOMP user destination (`/user/{name}/queue/**`) khớp với
     * {@code convertAndSendToUser(userId.toString(), ...)}. Nếu không implement Principal,
     * {@code AbstractAuthenticationToken.getName()} fallback về {@code toString()} của record
     * và message bị drop âm thầm.
     */
    @Override
    public String getName() {
        return userId == null ? null : userId.toString();
    }
}
