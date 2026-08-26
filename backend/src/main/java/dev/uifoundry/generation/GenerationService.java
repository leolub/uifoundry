package dev.uifoundry.generation;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import dev.uifoundry.common.exception.GenerationApiException;
import dev.uifoundry.common.exception.ProjectNotFoundException;
import dev.uifoundry.common.exception.SourceImageNotFoundException;
import dev.uifoundry.generation.ai.CodeGenerationProvider;
import dev.uifoundry.generation.ai.GeneratedCodeResult;
import dev.uifoundry.generation.ai.GeneratedCodeValidator;
import dev.uifoundry.generation.dto.GenerateRequest;
import dev.uifoundry.generation.dto.GenerationResponse;
import dev.uifoundry.project.Project;
import dev.uifoundry.project.ProjectRepository;
import dev.uifoundry.source.ProjectSourceService;

@Service
public class GenerationService {
    private final ProjectRepository projectRepository;
    private final GenerationRepository generationRepository;
    private final ProjectSourceService sourceService;
    private final CodeGenerationProvider provider;
    private final GeneratedCodeValidator validator;

    public GenerationService(ProjectRepository projectRepository, GenerationRepository generationRepository,
            ProjectSourceService sourceService, CodeGenerationProvider provider, GeneratedCodeValidator validator) {
        this.projectRepository = projectRepository;
        this.generationRepository = generationRepository;
        this.sourceService = sourceService;
        this.provider = provider;
        this.validator = validator;
    }

    public GenerationResponse generate(UUID ownerId, UUID projectId, GenerateRequest request) {
        Project project = findOwnedProject(ownerId, projectId);
        ProjectSourceService.SourceImageContent source;
        try {
            source = sourceService.getContent(ownerId, projectId);
        } catch (SourceImageNotFoundException exception) {
            throw new GenerationApiException(HttpStatus.CONFLICT, "SOURCE_IMAGE_REQUIRED",
                    "Upload a source image before generating an interface.");
        }

        String instruction = request == null ? null : request.instruction();
        Generation generation = generationRepository.saveAndFlush(
                new Generation(project, provider.providerName(), provider.modelName(), instruction));
        try {
            GeneratedCodeResult result = validator.validate(
                    provider.generate(source.bytes(), source.contentType(), instruction));
            List<GenerationFile> files = result.files().stream()
                    .map(file -> new GenerationFile(file.path(), file.content()))
                    .toList();
            generation.succeed(result.summary(), files);
            return GenerationResponse.from(generationRepository.saveAndFlush(generation));
        } catch (GenerationApiException exception) {
            recordFailure(generation, exception.getMessage());
            throw exception;
        } catch (RuntimeException exception) {
            recordFailure(generation, "Generation failed before valid source files were produced.");
            throw new GenerationApiException(HttpStatus.BAD_GATEWAY, "GENERATION_FAILED",
                    "Generation could not be completed. Try again later.", exception);
        }
    }

    public GenerationResponse latest(UUID ownerId, UUID projectId) {
        findOwnedProject(ownerId, projectId);
        Generation generation = generationRepository
                .findFirstByProjectIdAndStatusOrderByCreatedAtDesc(projectId, GenerationStatus.SUCCEEDED)
                .orElseThrow(() -> new GenerationApiException(HttpStatus.NOT_FOUND, "GENERATION_NOT_FOUND",
                        "This project does not have a successful generation yet."));
        return GenerationResponse.from(generation);
    }

    private void recordFailure(Generation generation, String safeMessage) {
        generation.fail(safeMessage);
        generationRepository.saveAndFlush(generation);
    }

    private Project findOwnedProject(UUID ownerId, UUID projectId) {
        return projectRepository.findByIdAndOwnerId(projectId, ownerId)
                .orElseThrow(ProjectNotFoundException::new);
    }
}
