package dev.uifoundry.common.exception;

import java.util.Map;

public record ApiError(String code, String message, String requestId, Map<String, String> details) {
}
