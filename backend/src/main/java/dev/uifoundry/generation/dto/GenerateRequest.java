package dev.uifoundry.generation.dto;

import jakarta.validation.constraints.Size;

public record GenerateRequest(
        @Size(max = 2000, message = "Instruction must be 2,000 characters or fewer.") String instruction) {
    public GenerateRequest {
        instruction = instruction == null || instruction.isBlank() ? null : instruction.trim();
    }
}
