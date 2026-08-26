package dev.uifoundry.generation;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import dev.uifoundry.generation.ai.GeminiErrorSanitizer;

class GeminiErrorSanitizerTest {
    private final GeminiErrorSanitizer sanitizer = new GeminiErrorSanitizer(new ObjectMapper());

    @Test
    void extractsGoogleErrorMessageWithoutOtherResponseMetadata() {
        String detail = sanitizer.sanitize("""
                {"error":{"code":400,"message":"Invalid JSON payload received.","status":"INVALID_ARGUMENT"}}
                """);
        assertThat(detail).isEqualTo("Invalid JSON payload received.");
    }

    @Test
    void redactsGoogleApiKeysAndLimitsDetailLength() {
        String detail = sanitizer.sanitize("""
                {"error":{"message":"Rejected key AIza123456789012345678901234567890 and request."}}
                """);
        assertThat(detail).contains("[REDACTED_API_KEY]").doesNotContain("AIza123");
    }

    @Test
    void doesNotLogRawUnstructuredBodies() {
        assertThat(sanitizer.sanitize("gateway html or proxy response"))
                .isEqualTo("Unstructured upstream error");
    }
}
