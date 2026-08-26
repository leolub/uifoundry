package dev.uifoundry.generation.ai;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

@Component
public class ScreenshotGenerationPrompt {
    public static final String VERSION = "screenshot-to-react-v1";
    private final String template;

    public ScreenshotGenerationPrompt() {
        try {
            template = new ClassPathResource("prompts/screenshot-to-react-v1.txt")
                    .getContentAsString(StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new IllegalStateException("Could not load the generation prompt.", exception);
        }
    }

    public String build(String instruction) {
        if (instruction == null) return template + "\n\nInput type: IMAGE_UPLOAD.";
        return template + "\n\nInput type: IMAGE_UPLOAD.\nSupplementary user instruction:\n" + instruction;
    }
}
