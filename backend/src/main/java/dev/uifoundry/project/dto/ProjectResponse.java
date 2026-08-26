package dev.uifoundry.project.dto;

import java.time.Instant;
import java.util.UUID;

import dev.uifoundry.project.Project;

public record ProjectResponse(UUID id, String name, Instant createdAt, Instant updatedAt) {

    public static ProjectResponse from(Project project) {
        return new ProjectResponse(
                project.getId(),
                project.getName(),
                project.getCreatedAt(),
                project.getUpdatedAt());
    }
}
