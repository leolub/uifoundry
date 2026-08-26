package dev.uifoundry.common.security;

import java.time.Instant;
import java.util.UUID;

import dev.uifoundry.user.User;

public record AuthenticatedUser(UUID id, String email, Instant createdAt) {

    public static AuthenticatedUser from(User user) {
        return new AuthenticatedUser(user.getId(), user.getEmail(), user.getCreatedAt());
    }
}
