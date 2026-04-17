package com.taller.proxy.dto.response;

import java.time.LocalDateTime;

/**
 * Respuesta de error estandarizada para la API.
 */
public record ApiErrorResponse(
        int status,
        String error,
        String message,
        LocalDateTime timestamp,
        Object details
) {
    public ApiErrorResponse(int status, String error, String message) {
        this(status, error, message, LocalDateTime.now(), null);
    }
}
