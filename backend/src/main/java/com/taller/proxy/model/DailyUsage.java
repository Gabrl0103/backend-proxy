package com.taller.proxy.model;

import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Representa el uso de tokens en un día específico.
 */
public class DailyUsage {

    private final LocalDate date;
    private final AtomicLong tokensUsed;

    public DailyUsage(LocalDate date, long initialTokens) {
        this.date = date;
        this.tokensUsed = new AtomicLong(initialTokens);
    }

    public void addTokens(long tokens) {
        tokensUsed.addAndGet(tokens);
    }

    public LocalDate getDate() { return date; }
    public long getTokensUsed() { return tokensUsed.get(); }
}
