package dev.uifoundry.common.exception;

public class SourceImageNotFoundException extends RuntimeException {
    public SourceImageNotFoundException() {
        super("The project does not have a source image.");
    }
}
