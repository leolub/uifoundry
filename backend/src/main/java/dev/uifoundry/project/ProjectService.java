package dev.uifoundry.project;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.uifoundry.common.exception.ProjectNotFoundException;
import dev.uifoundry.project.dto.CreateProjectRequest;
import dev.uifoundry.project.dto.ProjectResponse;
import dev.uifoundry.project.dto.RenameProjectRequest;
import dev.uifoundry.user.UserRepository;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    public ProjectService(ProjectRepository projectRepository, UserRepository userRepository) {
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public ProjectResponse create(UUID ownerId, CreateProjectRequest request) {
        Project project = new Project(userRepository.getReferenceById(ownerId), request.name());
        return ProjectResponse.from(projectRepository.saveAndFlush(project));
    }

    @Transactional(readOnly = true)
    public List<ProjectResponse> list(UUID ownerId) {
        return projectRepository.findAllByOwnerIdOrderByUpdatedAtDesc(ownerId).stream()
                .map(ProjectResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProjectResponse get(UUID ownerId, UUID projectId) {
        return ProjectResponse.from(findOwnedProject(ownerId, projectId));
    }

    @Transactional
    public ProjectResponse rename(UUID ownerId, UUID projectId, RenameProjectRequest request) {
        Project project = findOwnedProject(ownerId, projectId);
        project.rename(request.name());
        return ProjectResponse.from(projectRepository.saveAndFlush(project));
    }

    @Transactional
    public void delete(UUID ownerId, UUID projectId) {
        Project project = findOwnedProject(ownerId, projectId);
        projectRepository.delete(project);
    }

    private Project findOwnedProject(UUID ownerId, UUID projectId) {
        return projectRepository.findByIdAndOwnerId(projectId, ownerId)
                .orElseThrow(ProjectNotFoundException::new);
    }
}
