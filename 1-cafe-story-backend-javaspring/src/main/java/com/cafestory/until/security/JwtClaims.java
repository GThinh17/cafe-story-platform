package com.cafestory.security;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record JwtClaims(UUID userId, String username, List<String> roles, Instant issuedAt, Instant expiresAt) {
}
