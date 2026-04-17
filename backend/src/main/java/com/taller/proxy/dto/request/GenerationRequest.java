package com.taller.proxy.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request para generación de texto con IA.
 */
public record GenerationRequest(

        @NotBlank(message = "El prompt no puede estar vacío")
        @Size(min = 3, max = 2000, message = "El prompt debe tener entre 3 y 2000 caracteres")
        String prompt,

        String userId
) {
    public GenerationRequest {
        if (userId == null || userId.isBlank()) {
            userId = "default-user";
        }
    }
}
