package com.taller.proxy.exception;

import com.taller.proxy.dto.response.ApiErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * Manejador global de excepciones. Centraliza el manejo de errores de la API.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<ApiErrorResponse> handleRateLimit(RateLimitExceededException ex) {
        log.warn("Rate limit excedido: {}", ex.getMessage());

        HttpHeaders headers = new HttpHeaders();
        headers.set("Retry-After", String.valueOf(ex.getRetryAfterSeconds()));

        ApiErrorResponse body = new ApiErrorResponse(
                HttpStatus.TOO_MANY_REQUESTS.value(),
                "RATE_LIMIT_EXCEEDED",
                ex.getMessage(),
                java.time.LocalDateTime.now(),
                Map.of("retryAfterSeconds", ex.getRetryAfterSeconds())
        );

        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .headers(headers)
                .body(body);
    }

    @ExceptionHandler(QuotaExceededException.class)
    public ResponseEntity<ApiErrorResponse> handleQuotaExceeded(QuotaExceededException ex) {
        log.warn("Cuota agotada: {}", ex.getMessage());

        ApiErrorResponse body = new ApiErrorResponse(
                HttpStatus.PAYMENT_REQUIRED.value(),
                "QUOTA_EXCEEDED",
                ex.getMessage(),
                java.time.LocalDateTime.now(),
                Map.of("currentPlan", ex.getCurrentPlan(), "upgradeUrl", "/api/quota/upgrade")
        );

        return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED).body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage));

        ApiErrorResponse body = new ApiErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "VALIDATION_ERROR",
                "Error de validación en los campos enviados.",
                java.time.LocalDateTime.now(),
                fieldErrors
        );

        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(
                new ApiErrorResponse(HttpStatus.BAD_REQUEST.value(), "BAD_REQUEST", ex.getMessage())
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGeneric(Exception ex) {
        log.error("Error inesperado: {}", ex.getMessage(), ex);
        return ResponseEntity.internalServerError().body(
                new ApiErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "INTERNAL_ERROR",
                        "Ocurrió un error interno. Por favor intenta de nuevo.")
        );
    }
}
