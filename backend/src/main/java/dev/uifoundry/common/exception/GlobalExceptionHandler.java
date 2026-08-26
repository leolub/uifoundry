package dev.uifoundry.common.exception;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(GenerationApiException.class)
    ResponseEntity<ApiError> handleGenerationApi(
            GenerationApiException exception, HttpServletRequest request) {
        return ResponseEntity.status(exception.getStatus()).body(new ApiError(
                exception.getCode(), exception.getMessage(), request.getRequestId(), null));
    }

    @ExceptionHandler(SourceImageNotFoundException.class)
    ResponseEntity<ApiError> handleSourceImageNotFound(
            SourceImageNotFoundException exception, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiError(
                "SOURCE_IMAGE_NOT_FOUND", exception.getMessage(), request.getRequestId(), null));
    }

    @ExceptionHandler(InvalidSourceImageException.class)
    ResponseEntity<ApiError> handleInvalidSourceImage(
            InvalidSourceImageException exception, HttpServletRequest request) {
        return ResponseEntity.status(exception.getStatus()).body(new ApiError(
                exception.getCode(), exception.getMessage(), request.getRequestId(), null));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    ResponseEntity<ApiError> handleMaxUploadSize(HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(new ApiError(
                "SOURCE_IMAGE_TOO_LARGE", "The source image exceeds the configured upload limit.",
                request.getRequestId(), null));
    }

    @ExceptionHandler(SourceImageStorageException.class)
    ResponseEntity<ApiError> handleSourceImageStorage(
            SourceImageStorageException exception, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiError(
                "SOURCE_IMAGE_STORAGE_ERROR", exception.getMessage(), request.getRequestId(), null));
    }

    @ExceptionHandler(ProjectNotFoundException.class)
    ResponseEntity<ApiError> handleProjectNotFound(
            ProjectNotFoundException exception, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiError(
                "PROJECT_NOT_FOUND",
                exception.getMessage(),
                request.getRequestId(),
                null));
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    ResponseEntity<ApiError> handleInvalidCredentials(
            InvalidCredentialsException exception, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiError(
                "INVALID_CREDENTIALS",
                exception.getMessage(),
                request.getRequestId(),
                null));
    }

    @ExceptionHandler(DuplicateEmailException.class)
    ResponseEntity<ApiError> handleDuplicateEmail(
            DuplicateEmailException exception, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ApiError(
                "EMAIL_ALREADY_EXISTS",
                exception.getMessage(),
                request.getRequestId(),
                null));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiError> handleValidation(
            MethodArgumentNotValidException exception, HttpServletRequest request) {
        Map<String, String> details = new LinkedHashMap<>();
        exception.getBindingResult().getFieldErrors().forEach(error ->
                details.putIfAbsent(error.getField(), error.getDefaultMessage()));
        return ResponseEntity.badRequest().body(new ApiError(
                "VALIDATION_ERROR",
                "The request contains invalid fields.",
                request.getRequestId(),
                details));
    }
}
