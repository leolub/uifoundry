package dev.uifoundry.project.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateProjectRequest(
        @NotBlank(message = "Project name is required.")
        @Size(max = 120, message = "Project name must be at most 120 characters.")
        String name) {

    public CreateProjectRequest {
        name = name == null ? null : name.trim();
    }
}
