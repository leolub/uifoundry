package dev.uifoundry.generation.ai;

import java.util.List;

public record GeneratedCodeResult(String summary, List<GeneratedFile> files, List<String> warnings) {
    public record GeneratedFile(String path, String content) { }
}
