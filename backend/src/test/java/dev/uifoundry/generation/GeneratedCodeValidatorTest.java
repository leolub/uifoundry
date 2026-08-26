package dev.uifoundry.generation;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.Test;

import dev.uifoundry.common.exception.GenerationApiException;
import dev.uifoundry.generation.ai.GeneratedCodeResult;
import dev.uifoundry.generation.ai.GeneratedCodeResult.GeneratedFile;
import dev.uifoundry.generation.ai.GeneratedCodeValidator;

class GeneratedCodeValidatorTest {
    private final GeneratedCodeValidator validator = new GeneratedCodeValidator();

    @Test
    void rejectsEmptyOrMalformedProviderOutput() {
        assertInvalid(null);
        assertInvalid(new GeneratedCodeResult("Empty", List.of(), List.of()));
    }

    @Test
    void rejectsUnsafeAndUnsupportedPaths() {
        for (String path : List.of("../../secret.txt", "C:\\whatever.tsx", "/etc/passwd.ts", "src/App.js")) {
            assertInvalid(result(new GeneratedFile(path, "content"), app()));
        }
    }

    @Test
    void rejectsMissingRequiredAppFile() {
        assertInvalid(result(new GeneratedFile("src/components/Header.tsx", "export function Header() {}")));
    }

    @Test
    void rejectsDuplicatePaths() {
        assertInvalid(result(app(), new GeneratedFile("src/App.tsx", "export default function Other() {}")));
    }

    private void assertInvalid(GeneratedCodeResult result) {
        assertThatThrownBy(() -> validator.validate(result))
                .isInstanceOf(GenerationApiException.class)
                .extracting("code").isEqualTo("GENERATED_OUTPUT_INVALID");
    }

    private GeneratedCodeResult result(GeneratedFile... files) {
        return new GeneratedCodeResult("Test", List.of(files), List.of());
    }

    private GeneratedFile app() {
        return new GeneratedFile("src/App.tsx", "export default function App() { return <main /> }");
    }
}
