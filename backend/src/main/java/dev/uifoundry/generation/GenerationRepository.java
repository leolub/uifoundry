package dev.uifoundry.generation;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GenerationRepository extends JpaRepository<Generation, UUID> {
    @EntityGraph(attributePaths = "files")
    Optional<Generation> findFirstByProjectIdAndStatusOrderByCreatedAtDesc(UUID projectId, GenerationStatus status);
}
