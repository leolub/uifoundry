package dev.uifoundry.common.exception;

import org.springframework.http.HttpStatus;

public class InvalidSourceImageException extends RuntimeException {
    private final HttpStatus status;
    private final String code;

    public InvalidSourceImageException(HttpStatus status, String code, String message) {
        super(message);
        this.status = status;
        this.code = code;
    }

    public HttpStatus getStatus() { return status; }
    public String getCode() { return code; }
}
