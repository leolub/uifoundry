package dev.uifoundry.source.dto;

import java.time.Instant;
import java.util.UUID;

import dev.uifoundry.source.ProjectSource;
import dev.uifoundry.source.SourceType;

public record ProjectSourceResponse(
        UUID id,
        UUID projectId,
        SourceType sourceType,
        String originalFilename,
        String contentType,
        long sizeBytes,
        Instant createdAt,
        Instant updatedAt) {

    public static ProjectSourceResponse from(ProjectSource source) {
        return new ProjectSourceResponse(source.getId(), source.getProject().getId(), source.getSourceType(),
                source.getOriginalFilename(), source.getContentType(), source.getSizeBytes(),
                source.getCreatedAt(), source.getUpdatedAt());
    }
}
