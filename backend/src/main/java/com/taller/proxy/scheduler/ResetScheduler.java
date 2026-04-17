package com.taller.proxy.scheduler;

import com.taller.proxy.model.DailyUsage;
import com.taller.proxy.model.UserState;
import com.taller.proxy.service.UserStateStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Tareas programadas para reseteo automático de rate limits y cuotas.
 */
@Component
public class ResetScheduler {

    private static final Logger log = LoggerFactory.getLogger(ResetScheduler.class);

    private final UserStateStore userStateStore;

    public ResetScheduler(UserStateStore userStateStore) {
        this.userStateStore = userStateStore;
    }

    /**
     * Resetea el contador de rate limit de todos los usuarios cada minuto.
     * El reset se hace evaluando la ventana de tiempo, no forzando.
     * Este scheduled job es un respaldo para usuarios inactivos.
     */
    @Scheduled(fixedRate = 60_000)
    public void resetRateLimitWindows() {
        LocalDateTime now = LocalDateTime.now();
        int resetCount = 0;

        for (UserState state : userStateStore.getAllStates()) {
            long secondsSinceWindow = java.time.temporal.ChronoUnit.SECONDS
                    .between(state.getMinuteWindowStart(), now);

            if (secondsSinceWindow >= 60) {
                state.getRequestsInCurrentMinute().set(0);
                state.setMinuteWindowStart(now);
                resetCount++;
            }
        }

        if (resetCount > 0) {
            log.debug("Rate limit reseteado para {} usuarios", resetCount);
        }
    }

    /**
     * Verifica diariamente si se debe resetear la cuota mensual (primer día del mes)
     * y rota el historial de uso diario.
     */
    @Scheduled(cron = "0 0 0 * * *") // Todos los días a medianoche
    public void resetMonthlyQuotaAndRotateHistory() {
        LocalDate today = LocalDate.now();

        for (UserState state : userStateStore.getAllStates()) {

            // Reset de cuota mensual el primer día del mes
            if (today.getDayOfMonth() == 1) {
                long previousUsage = state.getTokensUsedThisMonth().getAndSet(0);
                state.setMonthResetDate(today.plusMonths(1));
                log.info("Cuota mensual reseteada para usuario: {} (uso anterior: {} tokens)",
                        state.getUserId(), previousUsage);
            }

            // Rotar historial diario: eliminar el día más antiguo y agregar hoy
            rotateHistory(state, today);
        }
    }

    /**
     * Desplaza el historial 1 día: elimina el más antiguo y agrega el día de hoy con 0 tokens.
     */
    private void rotateHistory(UserState state, LocalDate today) {
        var history = state.getDailyUsageHistory();

        boolean todayExists = history.stream().anyMatch(d -> d.getDate().equals(today));
        if (!todayExists) {
            if (history.size() >= 7) {
                history.remove(0);
            }
            history.add(new DailyUsage(today, 0L));
        }
    }
}
