package dev.uifoundry.generation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import dev.uifoundry.common.exception.GenerationApiException;
import dev.uifoundry.common.security.JwtService;
import dev.uifoundry.generation.ai.CodeGenerationProvider;
import dev.uifoundry.generation.ai.GeneratedCodeResult;
import dev.uifoundry.generation.ai.GeneratedCodeResult.GeneratedFile;
import dev.uifoundry.project.Project;
import dev.uifoundry.project.ProjectRepository;
import dev.uifoundry.source.ProjectSource;
import dev.uifoundry.source.ProjectSourceRepository;
import dev.uifoundry.source.storage.SourceImageStorage;
import dev.uifoundry.user.User;
import dev.uifoundry.user.UserRepository;

@SpringBootTest
@AutoConfigureMockMvc
class GenerationIntegrationTest {
    private static final Path STORAGE = createTempDirectory();
    private static final byte[] PNG = {(byte) 0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a, 1};

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("app.storage.source-images-directory", () -> STORAGE.toString());
    }

    @Autowired MockMvc mockMvc;
    @Autowired GenerationRepository generationRepository;
    @Autowired GenerationFileRepository fileRepository;
    @Autowired ProjectSourceRepository sourceRepository;
    @Autowired ProjectRepository projectRepository;
    @Autowired UserRepository userRepository;
    @Autowired SourceImageStorage storage;
    @Autowired JwtService jwtService;

    @MockitoBean CodeGenerationProvider provider;

    private User owner;
    private User other;
    private Project project;
    private String ownerToken;
    private String otherToken;

    @BeforeEach
    void setUp() throws IOException {
        fileRepository.deleteAll();
        generationRepository.deleteAll();
        sourceRepository.deleteAll();
        projectRepository.deleteAll();
        userRepository.deleteAll();
        clearStorage();
        owner = userRepository.saveAndFlush(new User("generation-owner@example.com", "unused-test-hash"));
        other = userRepository.saveAndFlush(new User("generation-other@example.com", "unused-test-hash"));
        project = projectRepository.saveAndFlush(new Project(owner, "Generate me"));
        ownerToken = jwtService.createAccessToken(owner);
        otherToken = jwtService.createAccessToken(other);
        when(provider.providerName()).thenReturn("GEMINI");
        when(provider.modelName()).thenReturn("gemini-test-model");
        when(provider.generate(any(), eq("image/png"), nullable(String.class))).thenReturn(validResult());
        addSource(project);
    }

    @AfterAll
    static void cleanup() throws IOException { deleteTree(STORAGE); }

    @Test
    void ownerCanGenerateAndResultAndFilesArePersisted() throws Exception {
        mockMvc.perform(post(path()).header(HttpHeaders.AUTHORIZATION, bearer(ownerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"instruction\":\"  Keep the navigation dark.  \"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("SUCCEEDED"))
                .andExpect(jsonPath("$.provider").value("GEMINI"))
                .andExpect(jsonPath("$.files.length()").value(2))
                .andExpect(jsonPath("$.files[0].path").exists());

        Generation generation = generationRepository.findAll().get(0);
        assertThat(generation.getInstruction()).isEqualTo("Keep the navigation dark.");
        assertThat(generation.getStatus()).isEqualTo(GenerationStatus.SUCCEEDED);
        assertThat(fileRepository.count()).isEqualTo(2);
        verify(provider).generate(any(), eq("image/png"), eq("Keep the navigation dark."));
    }

    @Test
    void latestSuccessfulGenerationCanBeReloaded() throws Exception {
        generateSuccessfully();
        mockMvc.perform(get(path() + "/latest").header(HttpHeaders.AUTHORIZATION, bearer(ownerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.summary").value("A generated test interface"))
                .andExpect(jsonPath("$.files.length()").value(2));
    }

    @Test
    void otherUserCannotGenerateOrReadGeneration() throws Exception {
        mockMvc.perform(post(path()).header(HttpHeaders.AUTHORIZATION, bearer(otherToken))
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isNotFound());
        generateSuccessfully();
        mockMvc.perform(get(path() + "/latest").header(HttpHeaders.AUTHORIZATION, bearer(otherToken)))
                .andExpect(status().isNotFound());
    }

    @Test
    void unauthenticatedGenerationEndpointsReturnUnauthorized() throws Exception {
        mockMvc.perform(post(path()).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get(path() + "/latest")).andExpect(status().isUnauthorized());
    }

    @Test
    void generationWithoutSourceFailsCleanly() throws Exception {
        sourceRepository.deleteAll();
        mockMvc.perform(post(path()).header(HttpHeaders.AUTHORIZATION, bearer(ownerToken))
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("SOURCE_IMAGE_REQUIRED"));
        assertThat(generationRepository.count()).isZero();
    }

    @Test
    void invalidProviderOutputIsRejectedAndFailureIsRecorded() throws Exception {
        when(provider.generate(any(), any(), nullable(String.class))).thenReturn(new GeneratedCodeResult(
                "Unsafe", List.of(new GeneratedFile("../../secret.txt", "bad")), List.of()));

        mockMvc.perform(post(path()).header(HttpHeaders.AUTHORIZATION, bearer(ownerToken))
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("GENERATED_OUTPUT_INVALID"));

        Generation failed = generationRepository.findAll().get(0);
        assertThat(failed.getStatus()).isEqualTo(GenerationStatus.FAILED);
        assertThat(failed.getErrorMessage()).doesNotContain("secret.txt");
        assertThat(fileRepository.count()).isZero();
    }

    @Test
    void providerFailureIsRecordedWithoutLeakingInternalDetails() throws Exception {
        when(provider.generate(any(), any(), nullable(String.class))).thenThrow(new GenerationApiException(
                org.springframework.http.HttpStatus.BAD_GATEWAY, "AI_PROVIDER_UNAVAILABLE",
                "The AI provider is temporarily unavailable."));

        mockMvc.perform(post(path()).header(HttpHeaders.AUTHORIZATION, bearer(ownerToken))
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.code").value("AI_PROVIDER_UNAVAILABLE"));
        assertThat(generationRepository.findAll().get(0).getStatus()).isEqualTo(GenerationStatus.FAILED);
    }

    @Test
    void latestReturnsNotFoundBeforeSuccessfulGeneration() throws Exception {
        mockMvc.perform(get(path() + "/latest").header(HttpHeaders.AUTHORIZATION, bearer(ownerToken)))
                .andExpect(status().isNotFound()).andExpect(jsonPath("$.code").value("GENERATION_NOT_FOUND"));
    }

    private void generateSuccessfully() throws Exception {
        mockMvc.perform(post(path()).header(HttpHeaders.AUTHORIZATION, bearer(ownerToken))
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isCreated());
    }

    private void addSource(Project target) {
        String key = storage.store(target.getId(), PNG, "png");
        sourceRepository.saveAndFlush(new ProjectSource(target, key, "source.png", "image/png", PNG.length));
    }

    private GeneratedCodeResult validResult() {
        return new GeneratedCodeResult("A generated test interface", List.of(
                new GeneratedFile("src/App.tsx", "export default function App() { return <main /> }"),
                new GeneratedFile("src/index.css", "@tailwind base;")), List.of());
    }

    private String path() { return "/api/v1/projects/" + project.getId() + "/generations"; }
    private String bearer(String token) { return "Bearer " + token; }

    private static Path createTempDirectory() {
        try { return Files.createTempDirectory("uifoundry-generation-test-"); }
        catch (IOException exception) { throw new ExceptionInInitializerError(exception); }
    }

    private static void clearStorage() throws IOException {
        if (!Files.exists(STORAGE)) return;
        try (var paths = Files.walk(STORAGE)) {
            paths.filter(path -> !path.equals(STORAGE)).sorted(Comparator.reverseOrder()).forEach(path -> {
                try { Files.deleteIfExists(path); } catch (IOException exception) { throw new RuntimeException(exception); }
            });
        }
    }

    private static void deleteTree(Path root) throws IOException {
        if (!Files.exists(root)) return;
        try (var paths = Files.walk(root)) {
            paths.sorted(Comparator.reverseOrder()).forEach(path -> {
                try { Files.deleteIfExists(path); } catch (IOException exception) { throw new RuntimeException(exception); }
            });
        }
    }
}
