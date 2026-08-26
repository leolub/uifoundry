package dev.uifoundry.generation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import dev.uifoundry.common.exception.GenerationApiException;
import dev.uifoundry.generation.ai.GeminiResponseParser;

class GeminiResponseParserTest {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final GeminiResponseParser parser = new GeminiResponseParser(objectMapper);

    @Test
    void mapsGeminiCandidateTextToTypedGeneratedCode() throws Exception {
        var response = objectMapper.readTree("""
                {
                  "candidates": [{
                    "content": {"parts": [{"text": "{\\\"summary\\\":\\\"Dashboard\\\",\\\"files\\\":[{\\\"path\\\":\\\"src/App.tsx\\\",\\\"content\\\":\\\"export default function App() {}\\\"}],\\\"warnings\\\":[]}"}]}
                  }]
                }
                """);

        var result = parser.parse(response);
        assertThat(result.summary()).isEqualTo("Dashboard");
        assertThat(result.files()).singleElement().extracting("path").isEqualTo("src/App.tsx");
    }

    @Test
    void rejectsMalformedGeminiCandidateText() throws Exception {
        var response = objectMapper.readTree(
                "{\"candidates\":[{\"content\":{\"parts\":[{\"text\":\"not json\"}]}}]}");
        assertThatThrownBy(() -> parser.parse(response))
                .isInstanceOf(GenerationApiException.class)
                .extracting("code").isEqualTo("AI_PROVIDER_RESPONSE_INVALID");
    }
}
