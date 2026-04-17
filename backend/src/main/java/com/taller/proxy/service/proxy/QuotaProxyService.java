package com.taller.proxy.service.proxy;

import com.taller.proxy.dto.request.GenerationRequest;
import com.taller.proxy.dto.response.GenerationResponse;
import com.taller.proxy.exception.QuotaExceededException;
import com.taller.proxy.model.UserState;
import com.taller.proxy.service.AIGenerationService;
import com.taller.proxy.service.UserStateStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * PROXY DE CUOTA MENSUAL — Patrón Proxy (Structural Pattern)
 *
 * Este proxy verifica que el usuario no haya agotado su cuota mensual de tokens.
 * Si la cuota está agotada, lanza QuotaExceededException (HTTP 402).
 * Después de una generación exitosa, descuenta los tokens consumidos.
 *
 * Cadena de proxies: RateLimitProxy → QuotaProxy → MockAIGenerationService
 */
public class QuotaProxyService implements AIGenerationService {

    private static final Logger log = LoggerFactory.getLogger(QuotaProxyService.class);

    // Estimación de tokens antes de generar: ~4 caracteres por token
    private static final int CHARS_PER_TOKEN = 4;
    private static final int MIN_TOKEN_RESERVE = 50;

    private final AIGenerationService next; // Siguiente en la cadena (servicio real)
    private final UserStateStore userStateStore;

    public QuotaProxyService(AIGenerationService next, UserStateStore userStateStore) {
        this.next = next;
        this.userStateStore = userStateStore;
    }

    @Override
    public GenerationResponse generate(GenerationRequest request) {
        UserState state = userStateStore.getOrCreate(request.userId());

        // Verificar cuota antes de procesar
        checkQuotaAvailability(state, request);

        // Delegar al servicio real
        GenerationResponse response = next.generate(request);

        // Descontar tokens consumidos del estado del usuario
        deductTokens(state, response.tokensUsed());

        log.debug("Tokens descontados para usuario {}: {} tokens usados, {} restantes",
                request.userId(),
                response.tokensUsed(),
                getRemainingTokens(state));

        return response;
    }

    /**
     * Verifica que el usuario tenga cuota suficiente para procesar el request.
     * Para ENTERPRISE, la cuota es ilimitada (Long.MAX_VALUE).
     */
    private void checkQuotaAvailability(UserState state, GenerationRequest request) {
        long quota = state.getPlan().getMonthlyTokenQuota();

        // ENTERPRISE: sin límite de tokens
        if (quota == Long.MAX_VALUE) {
            return;
        }

        long tokensUsed = state.getTokensUsedThisMonth().get();
        long estimatedCost = estimateTokenCost(request.prompt());

        if (tokensUsed + estimatedCost > quota) {
            log.warn("Cuota mensual agotada para usuario: {} (usado: {}, cuota: {}, estimado: {})",
                    state.getUserId(), tokensUsed, quota, estimatedCost);
            throw new QuotaExceededException(state.getUserId(), state.getPlan().getDisplayName());
        }
    }

    /**
     * Descuenta los tokens consumidos y registra el uso diario.
     */
    private void deductTokens(UserState state, long tokensUsed) {
        state.getTokensUsedThisMonth().addAndGet(tokensUsed);
        state.incrementDailyUsage(tokensUsed);
    }

    private long getRemainingTokens(UserState state) {
        long quota = state.getPlan().getMonthlyTokenQuota();
        if (quota == Long.MAX_VALUE) return Long.MAX_VALUE;
        return Math.max(0, quota - state.getTokensUsedThisMonth().get());
    }

    /**
     * Estimación rápida de costo en tokens antes de llamar al servicio real.
     * Fórmula: (caracteres / 4) + reserva mínima para la respuesta.
     */
    public static long estimateTokenCost(String prompt) {
        if (prompt == null || prompt.isBlank()) return MIN_TOKEN_RESERVE;
        long promptTokens = (long) Math.ceil((double) prompt.length() / CHARS_PER_TOKEN);
        return promptTokens + MIN_TOKEN_RESERVE;
    }
}
