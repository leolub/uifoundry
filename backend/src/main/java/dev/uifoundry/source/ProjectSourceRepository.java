package dev.uifoundry.source;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectSourceRepository extends JpaRepository<ProjectSource, UUID> {
    Optional<ProjectSource> findByProjectId(UUID projectId);
}
