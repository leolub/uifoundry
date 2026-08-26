package dev.uifoundry.generation.ai;

public interface CodeGenerationProvider {
    String providerName();
    String modelName();
    GeneratedCodeResult generate(byte[] imageBytes, String contentType, String instruction);
}
