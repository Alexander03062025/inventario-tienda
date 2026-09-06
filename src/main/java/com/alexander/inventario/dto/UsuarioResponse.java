package com.alexander.inventario.dto;

import com.alexander.inventario.model.Usuario;

import java.time.LocalDateTime;

public record UsuarioResponse(
        Long id,
        String username,
        String nombre,
        String rol,
        boolean activo,
        LocalDateTime fechaCreacion
) {
    public static UsuarioResponse desde(Usuario u) {
        return new UsuarioResponse(u.getId(), u.getUsername(), u.getNombre(),
                u.getRol().name(), u.isActivo(), u.getFechaCreacion());
    }
}
