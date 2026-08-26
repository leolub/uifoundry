package dev.uifoundry.generation.ai;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import dev.uifoundry.common.exception.GenerationApiException;
import dev.uifoundry.generation.ai.GeneratedCodeResult.GeneratedFile;

@Component
public class GeneratedCodeValidator {
    static final int MAX_FILES = 10;
    static final int MAX_TOTAL_CHARACTERS = 500_000;
    private static final Pattern SAFE_PATH = Pattern.compile(
            "^src/(?:[A-Za-z0-9_-]+/)*[A-Za-z0-9_.-]+\\.(?:ts|tsx|css)$");

    public GeneratedCodeResult validate(GeneratedCodeResult result) {
        if (result == null || result.files() == null || result.files().isEmpty()) {
            throw invalid("The AI provider returned no generated files.");
        }
        if (result.files().size() > MAX_FILES) {
            throw invalid("The AI provider returned too many generated files.");
        }

        Set<String> paths = new HashSet<>();
        int totalCharacters = 0;
        for (GeneratedFile file : result.files()) {
            if (file == null || file.path() == null || !SAFE_PATH.matcher(file.path()).matches()
                    || file.path().contains("..") || file.path().contains("\\")) {
                throw invalid("The AI provider returned an unsafe generated file path.");
            }
            if (!paths.add(file.path())) {
                throw invalid("The AI provider returned duplicate generated file paths.");
            }
            if (file.content() == null || file.content().isBlank()) {
                throw invalid("The AI provider returned an empty generated file.");
            }
            totalCharacters += file.content().length();
            if (totalCharacters > MAX_TOTAL_CHARACTERS) {
                throw invalid("The generated source exceeds the allowed size.");
            }
        }
        if (!paths.contains("src/App.tsx")) {
            throw invalid("The AI provider response is missing src/App.tsx.");
        }

        String summary = result.summary() == null || result.summary().isBlank()
                ? "Generated React interface" : result.summary().trim();
        List<String> warnings = result.warnings() == null ? List.of() : result.warnings();
        return new GeneratedCodeResult(summary, List.copyOf(result.files()), List.copyOf(warnings));
    }

    private GenerationApiException invalid(String message) {
        return new GenerationApiException(HttpStatus.UNPROCESSABLE_ENTITY, "GENERATED_OUTPUT_INVALID", message);
    }
}
