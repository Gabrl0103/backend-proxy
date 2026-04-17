package com.taller.proxy.exception;

/**
 * Excepción lanzada cuando el usuario supera el rate limit.
 * Corresponde al HTTP 429 Too Many Requests.
 */
public class RateLimitExceededException extends RuntimeException {

    private final int retryAfterSeconds;

    public RateLimitExceededException(String userId, int retryAfterSeconds) {
        super(String.format("Rate limit excedido para el usuario '%s'. Reintenta en %d segundos.", userId, retryAfterSeconds));
        this.retryAfterSeconds = retryAfterSeconds;
    }

    public int getRetryAfterSeconds() {
        return retryAfterSeconds;
    }
}
