package com.taller.proxy.exception;

/**
 * Excepción lanzada cuando el usuario agota su cuota mensual de tokens.
 * Corresponde al HTTP 402 Payment Required.
 */
public class QuotaExceededException extends RuntimeException {

    private final String userId;
    private final String currentPlan;

    public QuotaExceededException(String userId, String currentPlan) {
        super(String.format("Cuota mensual agotada para el usuario '%s' con plan %s. Considera hacer upgrade.", userId, currentPlan));
        this.userId = userId;
        this.currentPlan = currentPlan;
    }

    public String getUserId() { return userId; }
    public String getCurrentPlan() { return currentPlan; }
}
