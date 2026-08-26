package dev.uifoundry.auth.dto;

import java.time.Instant;
import java.util.UUID;

import dev.uifoundry.user.User;

public record UserResponse(UUID id, String email, Instant createdAt) {

    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getEmail(), user.getCreatedAt());
    }
}
