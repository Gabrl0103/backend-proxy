package com.taller.proxy.dto.request;

import com.taller.proxy.model.enums.UserPlan;
import jakarta.validation.constraints.NotNull;

/**
 * Request para actualizar el plan del usuario.
 */
public record UpgradeRequest(

        String userId,

        @NotNull(message = "El plan de destino es requerido")
        UserPlan targetPlan
) {
    public UpgradeRequest {
        if (userId == null || userId.isBlank()) {
            userId = "default-user";
        }
    }
}
