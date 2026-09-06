package com.alexander.inventario.dto;

import jakarta.validation.constraints.*;

/**
 * DTO de entrada para crear o actualizar un producto.
 * Las anotaciones de validación rechazan datos inválidos ANTES de llegar
 * al servicio (nombre vacío, precio negativo, etc.).
 *
 * Es un 'record': clase inmutable con getters automáticos (nombre(), precio()...).
 */
public record ProductoRequest(

        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 120, message = "El nombre no puede pasar de 120 caracteres")
        String nombre,

        @Size(max = 60, message = "La categoría no puede pasar de 60 caracteres")
        String categoria,

        @PositiveOrZero(message = "El precio no puede ser negativo")
        double precio,

        @PositiveOrZero(message = "El stock no puede ser negativo")
        int stock,

        @PositiveOrZero(message = "El stock mínimo no puede ser negativo")
        int stockMinimo
) {
}
