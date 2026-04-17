package com.taller.proxy.config;

import com.taller.proxy.service.AIGenerationService;
import com.taller.proxy.service.MockAIGenerationService;
import com.taller.proxy.service.UserStateStore;
import com.taller.proxy.service.proxy.QuotaProxyService;
import com.taller.proxy.service.proxy.RateLimitProxyService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Configuración del Patrón Proxy encadenado.
 *
 * La cadena de responsabilidad es:
 *   RateLimitProxyService → QuotaProxyService → MockAIGenerationService
 *
 * Cada proxy verifica su condición y delega al siguiente si todo está bien.
 */
@Configuration
public class ProxyChainConfig {

    /**
     * Construye la cadena de proxies y la expone como el bean principal de AIGenerationService.
     *
     * @param realService  el servicio real (MockAIGenerationService)
     * @param userStateStore almacén de estado de usuarios
     * @return proxy de rate limit que encadena al proxy de cuota y al servicio real
     */
    @Bean
    @Primary
    public AIGenerationService aiGenerationServiceProxy(
            MockAIGenerationService realService,
            UserStateStore userStateStore) {

        // Construir desde el final de la cadena hacia el inicio
        AIGenerationService quotaProxy = new QuotaProxyService(realService, userStateStore);
        return new RateLimitProxyService(quotaProxy, userStateStore);
    }
}
