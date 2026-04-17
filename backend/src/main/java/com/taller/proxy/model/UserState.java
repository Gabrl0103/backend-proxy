package com.taller.proxy.model;

import com.taller.proxy.model.enums.UserPlan;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Representa el estado de un usuario en la plataforma.
 * Almacena información de plan, cuota y rate limit en memoria.
 */
public class UserState {

    private final String userId;
    private volatile UserPlan plan;

    // Rate limit: requests en el minuto actual
    private final AtomicInteger requestsInCurrentMinute;
    private volatile LocalDateTime minuteWindowStart;

    // Cuota mensual
    private final AtomicLong tokensUsedThisMonth;
    private volatile LocalDate monthResetDate;

    // Historial de uso diario (últimos 7 días)
    private final List<DailyUsage> dailyUsageHistory;

    public UserState(String userId, UserPlan plan) {
        this.userId = userId;
        this.plan = plan;
        this.requestsInCurrentMinute = new AtomicInteger(0);
        this.minuteWindowStart = LocalDateTime.now();
        this.tokensUsedThisMonth = new AtomicLong(0);
        this.monthResetDate = LocalDate.now().withDayOfMonth(1).plusMonths(1);
        this.dailyUsageHistory = new ArrayList<>();
        initializeDailyHistory();
    }

    private void initializeDailyHistory() {
        LocalDate today = LocalDate.now();
        for (int i = 6; i >= 0; i--) {
            dailyUsageHistory.add(new DailyUsage(today.minusDays(i), 0L));
        }
    }

    public void incrementDailyUsage(long tokens) {
        LocalDate today = LocalDate.now();
        dailyUsageHistory.stream()
                .filter(d -> d.getDate().equals(today))
                .findFirst()
                .ifPresent(d -> d.addTokens(tokens));
    }

    // Getters y setters
    public String getUserId() { return userId; }
    public UserPlan getPlan() { return plan; }
    public void setPlan(UserPlan plan) { this.plan = plan; }
    public AtomicInteger getRequestsInCurrentMinute() { return requestsInCurrentMinute; }
    public LocalDateTime getMinuteWindowStart() { return minuteWindowStart; }
    public void setMinuteWindowStart(LocalDateTime minuteWindowStart) { this.minuteWindowStart = minuteWindowStart; }
    public AtomicLong getTokensUsedThisMonth() { return tokensUsedThisMonth; }
    public LocalDate getMonthResetDate() { return monthResetDate; }
    public void setMonthResetDate(LocalDate monthResetDate) { this.monthResetDate = monthResetDate; }
    public List<DailyUsage> getDailyUsageHistory() { return dailyUsageHistory; }
}
