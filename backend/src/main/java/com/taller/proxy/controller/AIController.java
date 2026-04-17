package com.taller.proxy.controller;

import com.taller.proxy.dto.request.GenerationRequest;
import com.taller.proxy.dto.response.GenerationResponse;
import com.taller.proxy.service.AIGenerationService;
import com.taller.proxy.service.proxy.QuotaProxyService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controlador REST para el servicio de generación de IA.
 * Las peticiones pasan por la cadena completa de proxies (RateLimit → Quota → Real).
 */
@RestController
@RequestMapping("/api/ai")
public class AIController {

    private static final Logger log = LoggerFactory.getLogger(AIController.class);

    private final AIGenerationService aiGenerationService;

    public AIController(AIGenerationService aiGenerationService) {
        this.aiGenerationService = aiGenerationService;
    }

    /**
     * Genera texto usando el servicio de IA.
     * La petición pasa por el proxy chain completo antes de llegar al servicio real.
     */
    @PostMapping("/generate")
    public ResponseEntity<GenerationResponse> generate(@Valid @RequestBody GenerationRequest request) {
        log.info("Nueva solicitud de generación de usuario: {}", request.userId());
        GenerationResponse response = aiGenerationService.generate(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Estima los tokens que consumirá un prompt antes de enviarlo.
     */
    @GetMapping("/estimate")
    public ResponseEntity<Map<String, Object>> estimateTokens(@RequestParam String prompt) {
        long estimatedTokens = QuotaProxyService.estimateTokenCost(prompt);
        return ResponseEntity.ok(Map.of(
                "prompt", prompt,
                "estimatedTokens", estimatedTokens,
                "promptLength", prompt.length()
        ));
    }
}
