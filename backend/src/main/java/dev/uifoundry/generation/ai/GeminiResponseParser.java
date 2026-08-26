package dev.uifoundry.generation.ai;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import dev.uifoundry.common.exception.GenerationApiException;

@Component
public class GeminiResponseParser {
    private final ObjectMapper objectMapper;

    public GeminiResponseParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public GeneratedCodeResult parse(JsonNode response) {
        JsonNode parts = response == null ? null : response.path("candidates").path(0).path("content").path("parts");
        if (parts == null || !parts.isArray()) throw malformed();
        List<String> texts = new ArrayList<>();
        parts.forEach(part -> {
            if (part.hasNonNull("text")) texts.add(part.get("text").asText());
        });
        String json = String.join("", texts);
        if (json.isBlank()) throw malformed();
        try {
            return objectMapper.readValue(json, GeneratedCodeResult.class);
        } catch (JsonProcessingException exception) {
            throw malformed();
        }
    }

    private GenerationApiException malformed() {
        return new GenerationApiException(HttpStatus.BAD_GATEWAY, "AI_PROVIDER_RESPONSE_INVALID",
                "The AI provider returned an invalid structured response.");
    }
}
