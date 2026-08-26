package dev.uifoundry.source;

import java.util.UUID;

import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import dev.uifoundry.common.security.AuthenticatedUser;
import dev.uifoundry.source.dto.ProjectSourceResponse;

@RestController
@RequestMapping("/api/v1/projects/{projectId}/source-image")
public class ProjectSourceController {
    private final ProjectSourceService sourceService;

    public ProjectSourceController(ProjectSourceService sourceService) {
        this.sourceService = sourceService;
    }

    @PutMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ProjectSourceResponse upload(@AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable UUID projectId, @RequestParam("file") MultipartFile file) {
        return sourceService.upload(user.id(), projectId, file);
    }

    @GetMapping
    public ProjectSourceResponse get(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable UUID projectId) {
        return sourceService.get(user.id(), projectId);
    }

    @GetMapping("/content")
    public ResponseEntity<byte[]> content(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable UUID projectId) {
        ProjectSourceService.SourceImageContent content = sourceService.getContent(user.id(), projectId);
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .contentType(MediaType.parseMediaType(content.contentType()))
                .body(content.bytes());
    }

    @DeleteMapping
    public ResponseEntity<Void> delete(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable UUID projectId) {
        sourceService.delete(user.id(), projectId);
        return ResponseEntity.noContent().build();
    }
}
