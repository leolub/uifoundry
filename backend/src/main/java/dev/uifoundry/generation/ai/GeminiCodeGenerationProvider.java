package dev.uifoundry.generation.ai;

import java.time.Duration;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import com.fasterxml.jackson.databind.JsonNode;

import dev.uifoundry.common.exception.GenerationApiException;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class GeminiCodeGenerationProvider implements CodeGenerationProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(GeminiCodeGenerationProvider.class);
    private static final Pattern SAFE_MODEL = Pattern.compile("[A-Za-z0-9._-]{1,120}");
    static final String API_VERSION = "v1beta";

    private final String apiKey;
    private final String model;
    private final RestClient restClient;
    private final ScreenshotGenerationPrompt prompt;
    private final GeminiResponseParser responseParser;
    private final GeminiErrorSanitizer errorSanitizer;

    public GeminiCodeGenerationProvider(
            @Value("${app.ai.gemini-api-key:}") String apiKey,
            @Value("${app.ai.model:gemini-3.6-flash}") String model,
            @Value("${app.ai.base-url:https://generativelanguage.googleapis.com}") String baseUrl,
            @Value("${app.ai.timeout-seconds:90}") int timeoutSeconds,
            ScreenshotGenerationPrompt prompt,
            GeminiResponseParser responseParser,
            GeminiErrorSanitizer errorSanitizer) {
        this.apiKey = apiKey;
        this.model = model;
        this.prompt = prompt;
        this.responseParser = responseParser;
        this.errorSanitizer = errorSanitizer;
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(Math.min(timeoutSeconds, 20)));
        requestFactory.setReadTimeout(Duration.ofSeconds(timeoutSeconds));
        this.restClient = RestClient.builder().baseUrl(baseUrl).requestFactory(requestFactory).build();
    }

    @PostConstruct
    void logConfigurationState() {
        LOGGER.info("Gemini provider configuration: key configured: {}, model: {}, API version: {}",
                apiKey != null && !apiKey.isBlank(), model, API_VERSION);
    }

    @Override public String providerName() { return "GEMINI"; }
    @Override public String modelName() { return model; }

    @Override
    public GeneratedCodeResult generate(byte[] imageBytes, String contentType, String instruction) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new GenerationApiException(HttpStatus.SERVICE_UNAVAILABLE, "AI_PROVIDER_NOT_CONFIGURED",
                    "AI provider is not configured.");
        }
        if (!SAFE_MODEL.matcher(model).matches()) {
            throw new GenerationApiException(HttpStatus.SERVICE_UNAVAILABLE, "AI_PROVIDER_NOT_CONFIGURED",
                    "AI provider model configuration is invalid.");
        }

        try {
            JsonNode response = restClient.post()
                    .uri("/" + API_VERSION + "/models/{model}:generateContent", model)
                    .header("x-goog-api-key", apiKey)
                    .body(requestBody(imageBytes, contentType, instruction))
                    .retrieve()
                    .body(JsonNode.class);
            return responseParser.parse(response);
        } catch (GenerationApiException exception) {
            throw exception;
        } catch (RestClientResponseException exception) {
            String safeDetail = errorSanitizer.sanitize(exception.getResponseBodyAsString());
            LOGGER.warn("Gemini generateContent failed: HTTP {}, model: {}, detail: {}",
                    exception.getStatusCode().value(), model, safeDetail);
            throw upstreamError(exception.getStatusCode().value(), safeDetail);
        } catch (RestClientException exception) {
            throw new GenerationApiException(HttpStatus.BAD_GATEWAY, "AI_PROVIDER_UNAVAILABLE",
                    "The AI provider is temporarily unavailable.", exception);
        }
    }

    private Map<String, Object> requestBody(byte[] bytes, String contentType, String instruction) {
        Map<String, Object> image = Map.of("inlineData", Map.of(
                "mimeType", contentType, "data", Base64.getEncoder().encodeToString(bytes)));
        Map<String, Object> text = Map.of("text", prompt.build(instruction));
        return Map.of(
                "contents", List.of(Map.of("role", "user", "parts", List.of(image, text))),
                "generationConfig", Map.of(
                        "temperature", 0.2,
                        "responseMimeType", "application/json",
                        "responseSchema", responseSchema()));
    }

    private Map<String, Object> responseSchema() {
        Map<String, Object> file = Map.of(
                "type", "OBJECT",
                "properties", Map.of(
                        "path", Map.of("type", "STRING"),
                        "content", Map.of("type", "STRING")),
                "required", List.of("path", "content"));
        return Map.of(
                "type", "OBJECT",
                "properties", Map.of(
                        "summary", Map.of("type", "STRING"),
                        "files", Map.of("type", "ARRAY", "items", file),
                        "warnings", Map.of("type", "ARRAY", "items", Map.of("type", "STRING"))),
                "required", List.of("summary", "files", "warnings"));
    }

    private GenerationApiException upstreamError(int status, String safeDetail) {
        if (status == 400) {
            return new GenerationApiException(HttpStatus.BAD_GATEWAY, "AI_REQUEST_REJECTED",
                    "Gemini rejected the generation request. Check the configured model and request contract.");
        }
        if (status == 401 || status == 403) {
            return new GenerationApiException(HttpStatus.BAD_GATEWAY, "AI_PROVIDER_AUTHENTICATION_FAILED",
                    "The AI provider rejected its configured credentials.");
        }
        if (status == 404) {
            return new GenerationApiException(HttpStatus.BAD_GATEWAY, "AI_MODEL_NOT_AVAILABLE",
                    "The configured Gemini model is not available for this API key or API version.");
        }
        if (status == 429) {
            return new GenerationApiException(HttpStatus.TOO_MANY_REQUESTS, "AI_PROVIDER_RATE_LIMITED",
                    "The Gemini free-tier quota or rate limit was reached. Try again later.");
        }
        if (status >= 500) {
            return new GenerationApiException(HttpStatus.BAD_GATEWAY, "AI_PROVIDER_UNAVAILABLE",
                    "Gemini is temporarily unavailable. Try again later.");
        }
        return new GenerationApiException(HttpStatus.BAD_GATEWAY, "AI_PROVIDER_UNAVAILABLE",
                "Gemini could not complete the generation request (HTTP " + status + ").");
    }
}
