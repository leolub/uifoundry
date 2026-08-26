package dev.uifoundry.generation;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import dev.uifoundry.project.Project;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "generations")
public class Generation {
    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false, updatable = false)
    private Project project;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private GenerationStatus status;

    @Column(nullable = false, length = 40)
    private String provider;

    @Column(nullable = false, length = 120)
    private String model;

    @Column(length = 2000)
    private String instruction;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "error_message", length = 500)
    private String errorMessage;

    @OneToMany(mappedBy = "generation", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("path ASC")
    private List<GenerationFile> files = new ArrayList<>();

    protected Generation() { }

    public Generation(Project project, String provider, String model, String instruction) {
        this.id = UUID.randomUUID();
        this.project = project;
        this.status = GenerationStatus.RUNNING;
        this.provider = provider;
        this.model = model;
        this.instruction = instruction;
    }

    @PrePersist
    void onCreate() {
        if (createdAt == null) createdAt = Instant.now();
    }

    public void succeed(String summary, List<GenerationFile> generatedFiles) {
        this.status = GenerationStatus.SUCCEEDED;
        this.summary = summary;
        this.completedAt = Instant.now();
        this.errorMessage = null;
        this.files.clear();
        generatedFiles.forEach(this::addFile);
    }

    public void fail(String safeMessage) {
        this.status = GenerationStatus.FAILED;
        this.completedAt = Instant.now();
        this.errorMessage = safeMessage.length() <= 500 ? safeMessage : safeMessage.substring(0, 500);
    }

    private void addFile(GenerationFile file) {
        file.attachTo(this);
        files.add(file);
    }

    public UUID getId() { return id; }
    public Project getProject() { return project; }
    public GenerationStatus getStatus() { return status; }
    public String getProvider() { return provider; }
    public String getModel() { return model; }
    public String getInstruction() { return instruction; }
    public String getSummary() { return summary; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getCompletedAt() { return completedAt; }
    public String getErrorMessage() { return errorMessage; }
    public List<GenerationFile> getFiles() { return List.copyOf(files); }
}
