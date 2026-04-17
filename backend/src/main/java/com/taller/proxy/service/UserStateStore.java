package com.taller.proxy.service;

import com.taller.proxy.model.UserState;
import com.taller.proxy.model.enums.UserPlan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Almacén en memoria del estado de todos los usuarios.
 * En producción, esto se reemplazaría con Redis o base de datos.
 */
@Component
public class UserStateStore {

    private static final Logger log = LoggerFactory.getLogger(UserStateStore.class);

    private final ConcurrentHashMap<String, UserState> userStates = new ConcurrentHashMap<>();

    /**
     * Obtiene o crea el estado del usuario. Los nuevos usuarios inician con plan FREE.
     */
    public UserState getOrCreate(String userId) {
        return userStates.computeIfAbsent(userId, id -> {
            log.info("Creando nuevo estado para usuario: {}", id);
            return new UserState(id, UserPlan.FREE);
        });
    }

    public Collection<UserState> getAllStates() {
        return userStates.values();
    }
}
