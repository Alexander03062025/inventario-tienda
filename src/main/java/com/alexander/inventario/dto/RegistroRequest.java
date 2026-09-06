package com.alexander.inventario.dto;

import com.alexander.inventario.model.Rol;
import jakarta.validation.constraints.*;

/**
 * DTO para crear un usuario. Solo un ADMIN puede llamar a este endpoint.
 */
public record RegistroRequest(
        @NotBlank @Size(min = 3, max = 40) String username,
        @NotBlank @Size(min = 6, max = 72, message = "La contraseña debe tener entre 6 y 72 caracteres") String password,
        @NotBlank @Size(max = 80) String nombre,
        @NotNull Rol rol
) {
}
