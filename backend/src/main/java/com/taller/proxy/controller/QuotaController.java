package com.taller.proxy.controller;

import com.taller.proxy.dto.request.UpgradeRequest;
import com.taller.proxy.dto.response.DailyUsageResponse;
import com.taller.proxy.dto.response.QuotaStatusResponse;
import com.taller.proxy.service.QuotaService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para gestión de cuotas y planes de usuario.
 */
@RestController
@RequestMapping("/api/quota")
public class QuotaController {

    private static final Logger log = LoggerFactory.getLogger(QuotaController.class);

    private final QuotaService quotaService;

    public QuotaController(QuotaService quotaService) {
        this.quotaService = quotaService;
    }

    /**
     * Retorna el estado actual de cuota del usuario:
     * tokens usados, tokens restantes, resetDate y plan actual.
     */
    @GetMapping("/status")
    public ResponseEntity<QuotaStatusResponse> getStatus(
            @RequestParam(defaultValue = "default-user") String userId) {
        log.debug("Consultando estado de cuota para usuario: {}", userId);
        return ResponseEntity.ok(quotaService.getQuotaStatus(userId));
    }

    /**
     * Retorna el historial de uso de tokens de los últimos 7 días.
     */
    @GetMapping("/history")
    public ResponseEntity<DailyUsageResponse> getHistory(
            @RequestParam(defaultValue = "default-user") String userId) {
        log.debug("Consultando historial de uso para usuario: {}", userId);
        return ResponseEntity.ok(quotaService.getDailyHistory(userId));
    }

    /**
     * Actualiza el plan del usuario (FREE → PRO o FREE/PRO → ENTERPRISE).
     */
    @PostMapping("/upgrade")
    public ResponseEntity<QuotaStatusResponse> upgradePlan(@Valid @RequestBody UpgradeRequest request) {
        log.info("Solicitud de upgrade de plan para usuario: {} → {}", request.userId(), request.targetPlan());
        QuotaStatusResponse updatedStatus = quotaService.upgradePlan(request.userId(), request.targetPlan());
        return ResponseEntity.ok(updatedStatus);
    }
}
