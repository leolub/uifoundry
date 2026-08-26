package dev.uifoundry.generation.dto;

import dev.uifoundry.generation.GenerationFile;

public record GeneratedFileResponse(String path, String content) {
    public static GeneratedFileResponse from(GenerationFile file) {
        return new GeneratedFileResponse(file.getPath(), file.getContent());
    }
}
