package com.taller.proxy.dto.response;

import java.time.LocalDate;

/**
 * Estado actual de la cuota del usuario.
 */
public record QuotaStatusResponse(
        String userId,
        String plan,
        long tokensUsed,
        long tokensRemaining,
        long totalQuota,
        int requestsInCurrentMinute,
        int requestsPerMinuteLimit,
        LocalDate resetDate,
        double usagePercentage
) {}
