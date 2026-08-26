package dev.uifoundry.source;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.UUID;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import org.springframework.mock.web.MockMultipartFile;

import dev.uifoundry.common.security.JwtService;
import dev.uifoundry.project.Project;
import dev.uifoundry.project.ProjectRepository;
import dev.uifoundry.user.User;
import dev.uifoundry.user.UserRepository;

@SpringBootTest
@AutoConfigureMockMvc
class ProjectSourceIntegrationTest {
    private static final Path STORAGE_DIRECTORY = createStorageDirectory();
    private static final byte[] PNG = {(byte) 0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a, 1};
    private static final byte[] JPEG = {(byte) 0xff, (byte) 0xd8, (byte) 0xff, 1};
    private static final byte[] WEBP = {'R', 'I', 'F', 'F', 1, 2, 3, 4, 'W', 'E', 'B', 'P'};

    @DynamicPropertySource
    static void storageProperties(DynamicPropertyRegistry registry) {
        registry.add("app.storage.source-images-directory", () -> STORAGE_DIRECTORY.toString());
        registry.add("app.storage.max-source-image-size-bytes", () -> 32);
    }

    @Autowired MockMvc mockMvc;
    @Autowired ProjectSourceRepository sourceRepository;
    @Autowired ProjectRepository projectRepository;
    @Autowired UserRepository userRepository;
    @Autowired JwtService jwtService;

    private User owner;
    private User other;
    private Project project;
    private String ownerToken;
    private String otherToken;

    @BeforeEach
    void setUp() throws IOException {
        sourceRepository.deleteAll();
        projectRepository.deleteAll();
        userRepository.deleteAll();
        clearStorageContents();
        owner = userRepository.saveAndFlush(new User("source-owner@example.com", "unused-test-hash"));
        other = userRepository.saveAndFlush(new User("source-other@example.com", "unused-test-hash"));
        project = projectRepository.saveAndFlush(new Project(owner, "Screenshot project"));
        ownerToken = jwtService.createAccessToken(owner);
        otherToken = jwtService.createAccessToken(other);
    }

    @AfterAll
    static void removeTestStorage() throws IOException {
        deleteTree(STORAGE_DIRECTORY);
    }

    @Test
    void ownerCanUploadPngAndMetadataIsSafelyPersisted() throws Exception {
        upload(ownerToken, project.getId(), "../../landing-page.png", "image/png", PNG)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sourceType").value("IMAGE_UPLOAD"))
                .andExpect(jsonPath("$.originalFilename").value("landing-page.png"))
                .andExpect(jsonPath("$.storageKey").doesNotExist());

        ProjectSource source = sourceRepository.findByProjectId(project.getId()).orElseThrow();
        assertThat(source.getStorageKey()).doesNotContain("landing-page").doesNotContain("..");
        assertThat(STORAGE_DIRECTORY.resolve(source.getStorageKey())).exists();
    }

    @Test
    void ownerCanUploadJpeg() throws Exception {
        upload(ownerToken, project.getId(), "photo.jpg", "image/jpeg", JPEG)
                .andExpect(status().isOk()).andExpect(jsonPath("$.contentType").value("image/jpeg"));
    }

    @Test
    void ownerCanUploadWebp() throws Exception {
        upload(ownerToken, project.getId(), "screen.webp", "image/webp", WEBP)
                .andExpect(status().isOk()).andExpect(jsonPath("$.contentType").value("image/webp"));
    }

    @Test
    void ownerCanFetchMetadataAndContent() throws Exception {
        upload(ownerToken, project.getId(), "screen.png", "image/png", PNG).andExpect(status().isOk());

        mockMvc.perform(get(path(project.getId())).header(HttpHeaders.AUTHORIZATION, bearer(ownerToken)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.projectId").value(project.getId().toString()));
        mockMvc.perform(get(path(project.getId()) + "/content").header(HttpHeaders.AUTHORIZATION, bearer(ownerToken)))
                .andExpect(status().isOk()).andExpect(content().contentType("image/png"))
                .andExpect(content().bytes(PNG));
    }

    @Test
    void replacingImageUpdatesMetadataAndRemovesOldFile() throws Exception {
        upload(ownerToken, project.getId(), "old.png", "image/png", PNG).andExpect(status().isOk());
        ProjectSource old = sourceRepository.findByProjectId(project.getId()).orElseThrow();
        Path oldFile = STORAGE_DIRECTORY.resolve(old.getStorageKey());

        upload(ownerToken, project.getId(), "new.jpg", "image/jpeg", JPEG)
                .andExpect(status().isOk()).andExpect(jsonPath("$.originalFilename").value("new.jpg"));

        ProjectSource replacement = sourceRepository.findByProjectId(project.getId()).orElseThrow();
        assertThat(replacement.getId()).isEqualTo(old.getId());
        assertThat(replacement.getStorageKey()).isNotEqualTo(old.getStorageKey());
        assertThat(oldFile).doesNotExist();
        assertThat(STORAGE_DIRECTORY.resolve(replacement.getStorageKey())).exists();
    }

    @Test
    void ownerCanDeleteMetadataAndPhysicalFile() throws Exception {
        upload(ownerToken, project.getId(), "screen.png", "image/png", PNG).andExpect(status().isOk());
        Path file = storedFile();

        mockMvc.perform(delete(path(project.getId())).header(HttpHeaders.AUTHORIZATION, bearer(ownerToken)))
                .andExpect(status().isNoContent());

        assertThat(sourceRepository.findByProjectId(project.getId())).isEmpty();
        assertThat(file).doesNotExist();
    }

    @Test
    void rejectsUnsupportedTypeAndMismatchedContent() throws Exception {
        upload(ownerToken, project.getId(), "vector.svg", "image/svg+xml", "<svg/>".getBytes())
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.code").value("SOURCE_IMAGE_TYPE_UNSUPPORTED"));
        upload(ownerToken, project.getId(), "fake.png", "image/png", "not png".getBytes())
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.code").value("SOURCE_IMAGE_CONTENT_INVALID"));
    }

    @Test
    void rejectsEmptyFile() throws Exception {
        upload(ownerToken, project.getId(), "empty.png", "image/png", new byte[0])
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("SOURCE_IMAGE_EMPTY"));
    }

    @Test
    void rejectsOversizedFile() throws Exception {
        byte[] oversized = new byte[33];
        System.arraycopy(PNG, 0, oversized, 0, PNG.length);
        upload(ownerToken, project.getId(), "large.png", "image/png", oversized)
                .andExpect(status().isPayloadTooLarge())
                .andExpect(jsonPath("$.code").value("SOURCE_IMAGE_TOO_LARGE"));
    }

    @Test
    void anotherUserCannotAccessOrMutateSource() throws Exception {
        upload(ownerToken, project.getId(), "screen.png", "image/png", PNG).andExpect(status().isOk());

        mockMvc.perform(get(path(project.getId())).header(HttpHeaders.AUTHORIZATION, bearer(otherToken)))
                .andExpect(status().isNotFound());
        mockMvc.perform(get(path(project.getId()) + "/content").header(HttpHeaders.AUTHORIZATION, bearer(otherToken)))
                .andExpect(status().isNotFound());
        upload(otherToken, project.getId(), "other.jpg", "image/jpeg", JPEG).andExpect(status().isNotFound());
        mockMvc.perform(delete(path(project.getId())).header(HttpHeaders.AUTHORIZATION, bearer(otherToken)))
                .andExpect(status().isNotFound());
    }

    @Test
    void unauthenticatedSourceEndpointsReturnUnauthorized() throws Exception {
        mockMvc.perform(get(path(project.getId()))).andExpect(status().isUnauthorized());
        mockMvc.perform(get(path(project.getId()) + "/content")).andExpect(status().isUnauthorized());
        upload(null, project.getId(), "screen.png", "image/png", PNG).andExpect(status().isUnauthorized());
        mockMvc.perform(delete(path(project.getId()))).andExpect(status().isUnauthorized());
    }

    @Test
    void deletingProjectDeletesSourceMetadataAndPhysicalFile() throws Exception {
        upload(ownerToken, project.getId(), "screen.png", "image/png", PNG).andExpect(status().isOk());
        Path file = storedFile();

        mockMvc.perform(delete("/api/v1/projects/{id}", project.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(ownerToken)))
                .andExpect(status().isNoContent());

        assertThat(sourceRepository.findByProjectId(project.getId())).isEmpty();
        assertThat(file).doesNotExist();
    }

    @Test
    void missingSourceReturnsNotFound() throws Exception {
        mockMvc.perform(get(path(project.getId())).header(HttpHeaders.AUTHORIZATION, bearer(ownerToken)))
                .andExpect(status().isNotFound()).andExpect(jsonPath("$.code").value("SOURCE_IMAGE_NOT_FOUND"));
    }

    private org.springframework.test.web.servlet.ResultActions upload(
            String token, UUID projectId, String filename, String contentType, byte[] bytes) throws Exception {
        MockMultipartHttpServletRequestBuilder request = multipart(path(projectId))
                .file(new MockMultipartFile("file", filename, contentType, bytes));
        request.with(servletRequest -> { servletRequest.setMethod("PUT"); return servletRequest; });
        if (token != null) request.header(HttpHeaders.AUTHORIZATION, bearer(token));
        return mockMvc.perform(request);
    }

    private Path storedFile() {
        return STORAGE_DIRECTORY.resolve(sourceRepository.findByProjectId(project.getId()).orElseThrow().getStorageKey());
    }

    private String path(UUID projectId) { return "/api/v1/projects/" + projectId + "/source-image"; }
    private String bearer(String token) { return "Bearer " + token; }

    private static Path createStorageDirectory() {
        try { return Files.createTempDirectory("uifoundry-source-test-"); }
        catch (IOException exception) { throw new ExceptionInInitializerError(exception); }
    }

    private static void clearStorageContents() throws IOException {
        if (!Files.exists(STORAGE_DIRECTORY)) return;
        try (var paths = Files.walk(STORAGE_DIRECTORY)) {
            paths.filter(path -> !path.equals(STORAGE_DIRECTORY)).sorted(Comparator.reverseOrder()).forEach(path -> {
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
