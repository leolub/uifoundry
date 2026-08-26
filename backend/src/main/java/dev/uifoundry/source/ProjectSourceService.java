package dev.uifoundry.source;

import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import dev.uifoundry.common.exception.InvalidSourceImageException;
import dev.uifoundry.common.exception.ProjectNotFoundException;
import dev.uifoundry.common.exception.SourceImageNotFoundException;
import dev.uifoundry.project.Project;
import dev.uifoundry.project.ProjectRepository;
import dev.uifoundry.source.dto.ProjectSourceResponse;
import dev.uifoundry.source.storage.SourceImageStorage;

@Service
public class ProjectSourceService {
    private static final Map<String, String> EXTENSIONS = Map.of(
            "image/png", "png", "image/jpeg", "jpg", "image/webp", "webp");

    private final ProjectRepository projectRepository;
    private final ProjectSourceRepository sourceRepository;
    private final SourceImageStorage storage;
    private final long maxSizeBytes;

    public ProjectSourceService(ProjectRepository projectRepository, ProjectSourceRepository sourceRepository,
            SourceImageStorage storage,
            @Value("${app.storage.max-source-image-size-bytes:10485760}") long maxSizeBytes) {
        this.projectRepository = projectRepository;
        this.sourceRepository = sourceRepository;
        this.storage = storage;
        this.maxSizeBytes = maxSizeBytes;
    }

    @Transactional
    public ProjectSourceResponse upload(UUID ownerId, UUID projectId, MultipartFile file) {
        Project project = findOwnedProject(ownerId, projectId);
        ValidatedImage image = validate(file);
        ProjectSource existing = sourceRepository.findByProjectId(projectId).orElse(null);
        String oldStorageKey = existing == null ? null : existing.getStorageKey();
        String newStorageKey = storage.store(projectId, image.content(), image.extension());
        try {
            ProjectSource source = existing == null
                    ? new ProjectSource(project, newStorageKey, image.filename(), image.contentType(), image.content().length)
                    : existing;
            if (existing != null) {
                source.replace(newStorageKey, image.filename(), image.contentType(), image.content().length);
            }
            ProjectSource saved = sourceRepository.saveAndFlush(source);
            if (oldStorageKey != null) {
                storage.delete(oldStorageKey);
            }
            return ProjectSourceResponse.from(saved);
        } catch (RuntimeException exception) {
            storage.delete(newStorageKey);
            throw exception;
        }
    }

    @Transactional(readOnly = true)
    public ProjectSourceResponse get(UUID ownerId, UUID projectId) {
        findOwnedProject(ownerId, projectId);
        return ProjectSourceResponse.from(findSource(projectId));
    }

    @Transactional(readOnly = true)
    public SourceImageContent getContent(UUID ownerId, UUID projectId) {
        findOwnedProject(ownerId, projectId);
        ProjectSource source = findSource(projectId);
        return new SourceImageContent(storage.read(source.getStorageKey()), source.getContentType());
    }

    @Transactional
    public void delete(UUID ownerId, UUID projectId) {
        findOwnedProject(ownerId, projectId);
        deleteForProject(projectId);
    }

    @Transactional
    public void deleteForProject(UUID projectId) {
        sourceRepository.findByProjectId(projectId).ifPresent(source -> {
            sourceRepository.delete(source);
            sourceRepository.flush();
            storage.delete(source.getStorageKey());
        });
    }

    private Project findOwnedProject(UUID ownerId, UUID projectId) {
        return projectRepository.findByIdAndOwnerId(projectId, ownerId)
                .orElseThrow(ProjectNotFoundException::new);
    }

    private ProjectSource findSource(UUID projectId) {
        return sourceRepository.findByProjectId(projectId).orElseThrow(SourceImageNotFoundException::new);
    }

    private ValidatedImage validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw invalid(HttpStatus.BAD_REQUEST, "SOURCE_IMAGE_EMPTY", "Choose a non-empty image file.");
        }
        if (file.getSize() > maxSizeBytes) {
            throw invalid(HttpStatus.PAYLOAD_TOO_LARGE, "SOURCE_IMAGE_TOO_LARGE",
                    "The source image exceeds the configured upload limit.");
        }
        String contentType = file.getContentType() == null ? "" : file.getContentType().toLowerCase(Locale.ROOT);
        String extension = EXTENSIONS.get(contentType);
        if (extension == null) {
            throw invalid(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "SOURCE_IMAGE_TYPE_UNSUPPORTED",
                    "Only PNG, JPEG, and WebP images are supported.");
        }
        try {
            byte[] content = file.getBytes();
            if (!hasExpectedSignature(content, contentType)) {
                throw invalid(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "SOURCE_IMAGE_CONTENT_INVALID",
                        "The uploaded file content does not match a supported image type.");
            }
            return new ValidatedImage(content, safeFilename(file.getOriginalFilename()), contentType, extension);
        } catch (java.io.IOException exception) {
            throw invalid(HttpStatus.BAD_REQUEST, "SOURCE_IMAGE_UNREADABLE", "The uploaded image could not be read.");
        }
    }

    private boolean hasExpectedSignature(byte[] b, String type) {
        return switch (type) {
            case "image/png" -> b.length >= 8 && b[0] == (byte) 0x89 && b[1] == 0x50 && b[2] == 0x4e
                    && b[3] == 0x47 && b[4] == 0x0d && b[5] == 0x0a && b[6] == 0x1a && b[7] == 0x0a;
            case "image/jpeg" -> b.length >= 3 && b[0] == (byte) 0xff && b[1] == (byte) 0xd8 && b[2] == (byte) 0xff;
            case "image/webp" -> b.length >= 12 && ascii(b, 0, "RIFF") && ascii(b, 8, "WEBP");
            default -> false;
        };
    }

    private boolean ascii(byte[] bytes, int offset, String expected) {
        for (int i = 0; i < expected.length(); i++) {
            if (bytes[offset + i] != (byte) expected.charAt(i)) return false;
        }
        return true;
    }

    private String safeFilename(String original) {
        String filename = original == null ? "image" : original.replace('\\', '/');
        filename = filename.substring(filename.lastIndexOf('/') + 1).replaceAll("[\\p{Cntrl}]", "").trim();
        if (filename.isBlank()) filename = "image";
        return filename.length() <= 255 ? filename : filename.substring(filename.length() - 255);
    }

    private InvalidSourceImageException invalid(HttpStatus status, String code, String message) {
        return new InvalidSourceImageException(status, code, message);
    }

    private record ValidatedImage(byte[] content, String filename, String contentType, String extension) { }
    public record SourceImageContent(byte[] bytes, String contentType) { }
}
