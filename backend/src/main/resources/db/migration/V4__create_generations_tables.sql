CREATE TABLE generations (
    id UUID PRIMARY KEY,
    project_id UUID NOT NULL,
    status VARCHAR(20) NOT NULL,
    provider VARCHAR(40) NOT NULL,
    model VARCHAR(120) NOT NULL,
    instruction VARCHAR(2000),
    summary TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    completed_at TIMESTAMP WITH TIME ZONE,
    error_message VARCHAR(500),
    CONSTRAINT fk_generations_project
        FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE
);

CREATE INDEX idx_generations_project_created
    ON generations(project_id, created_at DESC);

CREATE TABLE generation_files (
    id UUID PRIMARY KEY,
    generation_id UUID NOT NULL,
    path VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_generation_files_generation
        FOREIGN KEY (generation_id) REFERENCES generations(id) ON DELETE CASCADE,
    CONSTRAINT uq_generation_files_path UNIQUE (generation_id, path)
);
