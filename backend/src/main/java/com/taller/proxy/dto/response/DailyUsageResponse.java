package com.taller.proxy.dto.response;

import java.time.LocalDate;
import java.util.List;

/**
 * Historial de uso de tokens por día.
 */
public record DailyUsageResponse(
        String userId,
        List<DayRecord> history
) {
    public record DayRecord(
            LocalDate date,
            String dayLabel,
            long tokensUsed
    ) {}
}
