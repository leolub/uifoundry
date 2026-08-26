package dev.uifoundry.generation.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import dev.uifoundry.generation.Generation;
import dev.uifoundry.generation.GenerationStatus;

public record GenerationResponse(UUID id, GenerationStatus status, String provider, String model,
        String summary, Instant createdAt, Instant completedAt, List<GeneratedFileResponse> files) {
    public static GenerationResponse from(Generation generation) {
        return new GenerationResponse(generation.getId(), generation.getStatus(), generation.getProvider(),
                generation.getModel(), generation.getSummary(), generation.getCreatedAt(),
                generation.getCompletedAt(), generation.getFiles().stream().map(GeneratedFileResponse::from).toList());
    }
}
