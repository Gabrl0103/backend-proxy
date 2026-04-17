package com.taller.proxy.service;

import com.taller.proxy.dto.response.DailyUsageResponse;
import com.taller.proxy.dto.response.QuotaStatusResponse;
import com.taller.proxy.model.DailyUsage;
import com.taller.proxy.model.UserState;
import com.taller.proxy.model.enums.UserPlan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

/**
 * Servicio de gestión de cuotas de usuario.
 * Proporciona estado de cuota, historial y lógica de upgrade de plan.
 */
@Service
public class QuotaService {

    private static final Logger log = LoggerFactory.getLogger(QuotaService.class);

    private final UserStateStore userStateStore;

    public QuotaService(UserStateStore userStateStore) {
        this.userStateStore = userStateStore;
    }

    /**
     * Retorna el estado actual de cuota del usuario.
     */
    public QuotaStatusResponse getQuotaStatus(String userId) {
        UserState state = userStateStore.getOrCreate(userId);
        UserPlan plan = state.getPlan();

        long tokensUsed = state.getTokensUsedThisMonth().get();
        long totalQuota = plan.getMonthlyTokenQuota();
        long tokensRemaining = totalQuota == Long.MAX_VALUE
                ? Long.MAX_VALUE
                : Math.max(0, totalQuota - tokensUsed);

        double usagePercentage = totalQuota == Long.MAX_VALUE
                ? 0.0
                : Math.min(100.0, (tokensUsed * 100.0) / totalQuota);

        return new QuotaStatusResponse(
                userId,
                plan.getDisplayName(),
                tokensUsed,
                tokensRemaining,
                totalQuota,
                state.getRequestsInCurrentMinute().get(),
                plan.getRequestsPerMinute(),
                state.getMonthResetDate(),
                Math.round(usagePercentage * 100.0) / 100.0
        );
    }

    /**
     * Retorna el historial de uso de tokens de los últimos 7 días.
     */
    public DailyUsageResponse getDailyHistory(String userId) {
        UserState state = userStateStore.getOrCreate(userId);

        List<DailyUsageResponse.DayRecord> records = state.getDailyUsageHistory().stream()
                .map(this::toDayRecord)
                .toList();

        return new DailyUsageResponse(userId, records);
    }

    /**
     * Actualiza el plan del usuario. Solo permite upgrades (FREE → PRO → ENTERPRISE).
     */
    public QuotaStatusResponse upgradePlan(String userId, UserPlan targetPlan) {
        UserState state = userStateStore.getOrCreate(userId);
        UserPlan currentPlan = state.getPlan();

        validateUpgrade(currentPlan, targetPlan, userId);

        state.setPlan(targetPlan);
        log.info("Plan actualizado para usuario {}: {} → {}", userId, currentPlan, targetPlan);

        return getQuotaStatus(userId);
    }

    private void validateUpgrade(UserPlan current, UserPlan target, String userId) {
        if (target.ordinal() <= current.ordinal()) {
            throw new IllegalArgumentException(
                    String.format("No se puede cambiar de %s a %s. Solo se permiten upgrades.",
                            current.getDisplayName(), target.getDisplayName())
            );
        }
    }

    private DailyUsageResponse.DayRecord toDayRecord(DailyUsage usage) {
        String dayLabel = usage.getDate().getDayOfWeek()
                .getDisplayName(TextStyle.SHORT, new Locale("es", "CO"));
        return new DailyUsageResponse.DayRecord(
                usage.getDate(),
                dayLabel,
                usage.getTokensUsed()
        );
    }
}
