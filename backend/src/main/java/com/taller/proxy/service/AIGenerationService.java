package com.taller.proxy.service;

import com.taller.proxy.dto.request.GenerationRequest;
import com.taller.proxy.dto.response.GenerationResponse;

/**
 * Interfaz del servicio de generación de texto con IA.
 * Es el contrato base del Patrón Proxy:
 * tanto el servicio real como los proxies implementan esta interfaz.
 */
public interface AIGenerationService {

    /**
     * Genera texto a partir de un prompt.
     *
     * @param request datos del prompt y usuario
     * @return respuesta con el texto generado y metadata
     */
    GenerationResponse generate(GenerationRequest request);
}
