package dev.uifoundry.project;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import dev.uifoundry.common.security.JwtService;
import dev.uifoundry.user.User;
import dev.uifoundry.user.UserRepository;

@SpringBootTest
@AutoConfigureMockMvc
class ProjectIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    private User owner;
    private User otherUser;
    private String ownerToken;
    private String otherToken;

    @BeforeEach
    void setUp() {
        projectRepository.deleteAll();
        userRepository.deleteAll();
        owner = userRepository.saveAndFlush(new User("owner@example.com", "unused-test-hash"));
        otherUser = userRepository.saveAndFlush(new User("other@example.com", "unused-test-hash"));
        ownerToken = jwtService.createAccessToken(owner);
        otherToken = jwtService.createAccessToken(otherUser);
    }

    @Test
    void authenticatedUserCanCreateOwnedProject() throws Exception {
        mockMvc.perform(post("/api/v1/projects")
                        .header(HttpHeaders.AUTHORIZATION, bearer(ownerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"  Landing Page Recreation  "}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.name").value("Landing Page Recreation"))
                .andExpect(jsonPath("$.createdAt").isNotEmpty())
                .andExpect(jsonPath("$.updatedAt").isNotEmpty())
                .andExpect(jsonPath("$.owner").doesNotExist());

        Project project = projectRepository.findAll().get(0);
        assertThat(project.getOwner().getId()).isEqualTo(owner.getId());
    }

    @Test
    void listContainsOnlyCurrentUsersProjects() throws Exception {
        projectRepository.saveAndFlush(new Project(owner, "Mine"));
        projectRepository.saveAndFlush(new Project(otherUser, "Not mine"));

        mockMvc.perform(get("/api/v1/projects")
                        .header(HttpHeaders.AUTHORIZATION, bearer(ownerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Mine"));
    }

    @Test
    void ownerCanFetchProject() throws Exception {
        Project project = projectRepository.saveAndFlush(new Project(owner, "Mine"));

        mockMvc.perform(get("/api/v1/projects/{id}", project.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(ownerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(project.getId().toString()));
    }

    @Test
    void differentUserCannotFetchProject() throws Exception {
        Project project = projectRepository.saveAndFlush(new Project(owner, "Mine"));

        mockMvc.perform(get("/api/v1/projects/{id}", project.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(otherToken)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PROJECT_NOT_FOUND"));
    }

    @Test
    void ownerCanRenameProject() throws Exception {
        Project project = projectRepository.saveAndFlush(new Project(owner, "Old name"));

        mockMvc.perform(patch("/api/v1/projects/{id}", project.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(ownerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"New name"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New name"));

        assertThat(projectRepository.findById(project.getId()).orElseThrow().getName()).isEqualTo("New name");
    }

    @Test
    void differentUserCannotRenameProject() throws Exception {
        Project project = projectRepository.saveAndFlush(new Project(owner, "Original"));

        mockMvc.perform(patch("/api/v1/projects/{id}", project.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(otherToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Stolen"}
                                """))
                .andExpect(status().isNotFound());

        assertThat(projectRepository.findById(project.getId()).orElseThrow().getName()).isEqualTo("Original");
    }

    @Test
    void ownerCanDeleteProject() throws Exception {
        Project project = projectRepository.saveAndFlush(new Project(owner, "Delete me"));

        mockMvc.perform(delete("/api/v1/projects/{id}", project.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(ownerToken)))
                .andExpect(status().isNoContent());

        assertThat(projectRepository.existsById(project.getId())).isFalse();
    }

    @Test
    void differentUserCannotDeleteProject() throws Exception {
        Project project = projectRepository.saveAndFlush(new Project(owner, "Keep me"));

        mockMvc.perform(delete("/api/v1/projects/{id}", project.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(otherToken)))
                .andExpect(status().isNotFound());

        assertThat(projectRepository.existsById(project.getId())).isTrue();
    }

    @Test
    void rejectsInvalidProjectName() throws Exception {
        mockMvc.perform(post("/api/v1/projects")
                        .header(HttpHeaders.AUTHORIZATION, bearer(ownerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"   "}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.name").isNotEmpty());
    }

    @Test
    void unauthenticatedListIsRejected() throws Exception {
        mockMvc.perform(get("/api/v1/projects"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void unauthenticatedCreateIsRejected() throws Exception {
        mockMvc.perform(post("/api/v1/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"No owner"}
                                """))
                .andExpect(status().isUnauthorized());
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }
}
