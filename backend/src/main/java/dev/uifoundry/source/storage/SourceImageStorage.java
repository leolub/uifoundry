package dev.uifoundry.source.storage;

import java.util.UUID;

public interface SourceImageStorage {
    String store(UUID projectId, byte[] content, String extension);
    byte[] read(String storageKey);
    void delete(String storageKey);
}
