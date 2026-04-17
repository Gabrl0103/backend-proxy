package com.taller.proxy.service;

import com.taller.proxy.dto.request.GenerationRequest;
import com.taller.proxy.dto.response.GenerationResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

/**
 * Implementación real (simulada) del servicio de IA.
 * Simula una latencia de 1200ms y retorna texto aleatorio predefinido.
 *
 * En el Patrón Proxy, este es el "RealSubject" — el objeto que realmente hace el trabajo.
 */
@Service("mockAIGenerationService")
public class MockAIGenerationService implements AIGenerationService {

    private static final Logger log = LoggerFactory.getLogger(MockAIGenerationService.class);
    private static final int SIMULATED_LATENCY_MS = 1200;
    private static final String MODEL_NAME = "mock-gpt-1.0";

    private static final List<String> PREDEFINED_RESPONSES = List.of(
            "La inteligencia artificial es una rama de la ciencia de la computación que busca crear sistemas capaces de realizar tareas que normalmente requieren inteligencia humana.",
            "El aprendizaje automático permite a las computadoras aprender de los datos sin ser explícitamente programadas para cada tarea específica.",
            "Las redes neuronales artificiales están inspiradas en el cerebro humano y son capaces de reconocer patrones complejos en grandes conjuntos de datos.",
            "El procesamiento del lenguaje natural es un campo de la IA que permite a las máquinas entender, interpretar y generar lenguaje humano de forma efectiva.",
            "La visión por computadora es la capacidad de las máquinas para interpretar y entender el mundo visual a través de imágenes y videos digitales.",
            "Los modelos de lenguaje grande (LLMs) han revolucionado la forma en que interactuamos con la tecnología, permitiendo conversaciones naturales y generación de contenido.",
            "El aprendizaje profundo utiliza múltiples capas de redes neuronales para extraer características cada vez más abstractas de los datos de entrenamiento.",
            "La ética en la inteligencia artificial es fundamental para garantizar que los sistemas sean justos, transparentes y respeten los derechos humanos.",
            "Los algoritmos de recomendación utilizan técnicas de aprendizaje automático para predecir qué contenido será más relevante para cada usuario.",
            "La automatización impulsada por IA está transformando industrias enteras, desde la manufactura hasta la atención médica y los servicios financieros."
    );

    private final Random random = new Random();

    @Override
    public GenerationResponse generate(GenerationRequest request) {
        log.debug("Procesando solicitud de generación para usuario: {}", request.userId());

        long startTime = System.currentTimeMillis();
        simulateProcessing();
        long processingTime = System.currentTimeMillis() - startTime;

        String generatedText = selectResponse(request.prompt());
        long tokensUsed = estimateTokens(request.prompt(), generatedText);

        log.debug("Generación completada. Tokens usados: {}, Tiempo: {}ms", tokensUsed, processingTime);

        return new GenerationResponse(generatedText, tokensUsed, processingTime, MODEL_NAME);
    }

    private void simulateProcessing() {
        try {
            Thread.sleep(SIMULATED_LATENCY_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Simulación de procesamiento interrumpida");
        }
    }

    private String selectResponse(String prompt) {
        // Selecciona respuesta basada en hash del prompt para consistencia
        int index = Math.abs(prompt.hashCode()) % PREDEFINED_RESPONSES.size();
        return PREDEFINED_RESPONSES.get(index);
    }

    /**
     * Estima tokens basado en conteo aproximado de palabras (1 token ≈ 0.75 palabras).
     */
    private long estimateTokens(String prompt, String response) {
        int totalWords = countWords(prompt) + countWords(response);
        return Math.max(10, (long) (totalWords / 0.75));
    }

    private int countWords(String text) {
        if (text == null || text.isBlank()) return 0;
        return text.trim().split("\\s+").length;
    }
}
