package com.alexander.inventario.dto;

/**
 * Respuesta del login: el token que el frontend debe guardar y enviar
 * en cada petición, más datos del usuario para mostrar en pantalla.
 */
public record AuthResponse(
        String token,
        String username,
        String nombre,
        String rol,
        long expiraEnMs
) {
}
