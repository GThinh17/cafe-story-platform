package com.cafestory.security;

import java.util.List;
import java.util.UUID;

public record AuthenticatedUserPrincipal(UUID userId, String username, List<String> roles) {
}
