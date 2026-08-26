package dev.uifoundry.generation;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface GenerationFileRepository extends JpaRepository<GenerationFile, UUID> { }
