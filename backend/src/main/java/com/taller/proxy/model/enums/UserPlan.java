package com.taller.proxy.model.enums;

/**
 * Planes disponibles para los usuarios de la plataforma.
 * Cada plan define límites de rate limit y cuota mensual de tokens.
 */
public enum UserPlan {

    FREE(10, 50_000L, "Free"),
    PRO(60, 500_000L, "Pro"),
    ENTERPRISE(Integer.MAX_VALUE, Long.MAX_VALUE, "Enterprise");

    private final int requestsPerMinute;
    private final long monthlyTokenQuota;
    private final String displayName;

    UserPlan(int requestsPerMinute, long monthlyTokenQuota, String displayName) {
        this.requestsPerMinute = requestsPerMinute;
        this.monthlyTokenQuota = monthlyTokenQuota;
        this.displayName = displayName;
    }

    public int getRequestsPerMinute() {
        return requestsPerMinute;
    }

    public long getMonthlyTokenQuota() {
        return monthlyTokenQuota;
    }

    public String getDisplayName() {
        return displayName;
    }
}
