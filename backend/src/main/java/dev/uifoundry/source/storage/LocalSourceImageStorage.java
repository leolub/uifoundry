package dev.uifoundry.source.storage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import dev.uifoundry.common.exception.SourceImageStorageException;

@Component
public class LocalSourceImageStorage implements SourceImageStorage {
    private final Path root;

    public LocalSourceImageStorage(@Value("${app.storage.source-images-directory}") String directory) {
        this.root = Path.of(directory).toAbsolutePath().normalize();
    }

    @Override
    public String store(UUID projectId, byte[] content, String extension) {
        String storageKey = projectId + "/" + UUID.randomUUID() + "." + extension;
        Path destination = resolve(storageKey);
        Path temporary = destination.resolveSibling(destination.getFileName() + ".tmp");
        try {
            Files.createDirectories(destination.getParent());
            Files.write(temporary, content);
            try {
                Files.move(temporary, destination, StandardCopyOption.ATOMIC_MOVE);
            } catch (IOException atomicMoveFailure) {
                Files.move(temporary, destination, StandardCopyOption.REPLACE_EXISTING);
            }
            return storageKey;
        } catch (IOException exception) {
            tryDelete(temporary);
            throw new SourceImageStorageException("Could not store the source image.", exception);
        }
    }

    @Override
    public byte[] read(String storageKey) {
        try {
            return Files.readAllBytes(resolve(storageKey));
        } catch (IOException exception) {
            throw new SourceImageStorageException("Could not read the source image.", exception);
        }
    }

    @Override
    public void delete(String storageKey) {
        Path file = resolve(storageKey);
        try {
            Files.deleteIfExists(file);
            Path parent = file.getParent();
            if (!parent.equals(root)) {
                tryDeleteEmptyDirectory(parent);
            }
        } catch (IOException exception) {
            throw new SourceImageStorageException("Could not delete the source image.", exception);
        }
    }

    private Path resolve(String storageKey) {
        Path resolved = root.resolve(storageKey).normalize();
        if (!resolved.startsWith(root)) {
            throw new SourceImageStorageException("Invalid source image storage key.", null);
        }
        return resolved;
    }

    private void tryDelete(Path path) {
        try { Files.deleteIfExists(path); } catch (IOException ignored) { }
    }

    private void tryDeleteEmptyDirectory(Path path) {
        try { Files.delete(path); } catch (IOException ignored) { }
    }
}
