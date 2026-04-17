package com.taller.proxy.service.proxy;

import com.taller.proxy.dto.request.GenerationRequest;
import com.taller.proxy.dto.response.GenerationResponse;
import com.taller.proxy.exception.RateLimitExceededException;
import com.taller.proxy.model.UserState;
import com.taller.proxy.service.AIGenerationService;
import com.taller.proxy.service.UserStateStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * PROXY DE RATE LIMITING — Patrón Proxy (Structural Pattern)
 *
 * Este proxy controla el número de requests por minuto según el plan del usuario.
 * Si el usuario supera el límite, lanza RateLimitExceededException (HTTP 429).
 *
 * Cadena de proxies: RateLimitProxy → QuotaProxy → MockAIGenerationService
 */
public class RateLimitProxyService implements AIGenerationService {

    private static final Logger log = LoggerFactory.getLogger(RateLimitProxyService.class);

    private final AIGenerationService next; // Siguiente en la cadena
    private final UserStateStore userStateStore;

    public RateLimitProxyService(AIGenerationService next, UserStateStore userStateStore) {
        this.next = next;
        this.userStateStore = userStateStore;
    }

    @Override
    public GenerationResponse generate(GenerationRequest request) {
        UserState state = userStateStore.getOrCreate(request.userId());

        resetWindowIfNeeded(state);
        checkAndIncrementRateLimit(state, request.userId());

        log.debug("Rate limit OK para usuario: {} ({}/{} req/min)",
                request.userId(),
                state.getRequestsInCurrentMinute().get(),
                state.getPlan().getRequestsPerMinute());

        // Delega al siguiente en la cadena
        return next.generate(request);
    }

    /**
     * Reinicia la ventana de tiempo si ha pasado más de 1 minuto.
     */
    private synchronized void resetWindowIfNeeded(UserState state) {
        LocalDateTime now = LocalDateTime.now();
        long secondsSinceWindow = ChronoUnit.SECONDS.between(state.getMinuteWindowStart(), now);

        if (secondsSinceWindow >= 60) {
            log.debug("Reiniciando ventana de rate limit para usuario: {}", state.getUserId());
            state.getRequestsInCurrentMinute().set(0);
            state.setMinuteWindowStart(now);
        }
    }

    /**
     * Verifica si el usuario puede hacer otro request, y si puede, incrementa el contador.
     */
    private void checkAndIncrementRateLimit(UserState state, String userId) {
        int limit = state.getPlan().getRequestsPerMinute();

        // ENTERPRISE tiene límite Integer.MAX_VALUE, no puede excederlo en la práctica
        if (limit == Integer.MAX_VALUE) {
            state.getRequestsInCurrentMinute().incrementAndGet();
            return;
        }

        int current = state.getRequestsInCurrentMinute().incrementAndGet();
        if (current > limit) {
            state.getRequestsInCurrentMinute().decrementAndGet(); // Revertir incremento
            int retryAfter = calculateRetryAfter(state);
            log.warn("Rate limit excedido para usuario: {} ({}/{})", userId, current - 1, limit);
            throw new RateLimitExceededException(userId, retryAfter);
        }
    }

    private int calculateRetryAfter(UserState state) {
        long secondsElapsed = ChronoUnit.SECONDS.between(state.getMinuteWindowStart(), LocalDateTime.now());
        return (int) Math.max(1, 60 - secondsElapsed);
    }
}
