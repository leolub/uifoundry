package dev.uifoundry.generation;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "generation_files")
public class GenerationFile {
    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "generation_id", nullable = false, updatable = false)
    private Generation generation;

    @Column(nullable = false, length = 255)
    private String path;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected GenerationFile() { }

    public GenerationFile(String path, String content) {
        this.id = UUID.randomUUID();
        this.path = path;
        this.content = content;
    }

    void attachTo(Generation generation) { this.generation = generation; }

    @PrePersist
    void onCreate() { createdAt = Instant.now(); }

    public UUID getId() { return id; }
    public String getPath() { return path; }
    public String getContent() { return content; }
    public Instant getCreatedAt() { return createdAt; }
}
