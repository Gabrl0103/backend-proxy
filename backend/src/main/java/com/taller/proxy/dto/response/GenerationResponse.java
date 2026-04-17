package com.taller.proxy.dto.response;

/**
 * Respuesta de la generación de texto por IA.
 */
public record GenerationResponse(
        String generatedText,
        long tokensUsed,
        long processingTimeMs,
        String model
) {}
