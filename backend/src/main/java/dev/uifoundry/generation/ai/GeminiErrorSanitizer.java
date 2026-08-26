package dev.uifoundry.generation.ai;

import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class GeminiErrorSanitizer {
    private static final int MAX_LENGTH = 400;
    private static final Pattern GOOGLE_KEY = Pattern.compile("AIza[0-9A-Za-z_-]{20,}");
    private static final Pattern CONTROL = Pattern.compile("[\\p{Cntrl}&&[^\\r\\n\\t]]");
    private final ObjectMapper objectMapper;

    public GeminiErrorSanitizer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String sanitize(String responseBody) {
        if (responseBody == null || responseBody.isBlank()) return "No upstream error detail";
        String detail = extractMessage(responseBody);
        detail = GOOGLE_KEY.matcher(detail).replaceAll("[REDACTED_API_KEY]");
        detail = CONTROL.matcher(detail).replaceAll("").replaceAll("\\s+", " ").trim();
        return detail.length() <= MAX_LENGTH ? detail : detail.substring(0, MAX_LENGTH) + "...";
    }

    private String extractMessage(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode message = root.path("error").path("message");
            return message.isTextual() ? message.asText() : "Unstructured upstream error";
        } catch (Exception ignored) {
            return "Unstructured upstream error";
        }
    }
}
