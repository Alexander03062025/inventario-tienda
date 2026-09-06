package com.alexander.inventario.dto;

import com.alexander.inventario.model.TipoMovimiento;
import jakarta.validation.constraints.*;

/**
 * DTO de entrada para registrar un movimiento de stock (entrada o salida).
 */
public record MovimientoRequest(

        @NotNull(message = "Indica el producto")
        Long productoId,

        @NotNull(message = "Indica el tipo: ENTRADA o SALIDA")
        TipoMovimiento tipo,

        @Positive(message = "La cantidad debe ser mayor que cero")
        int cantidad,

        @Size(max = 200, message = "El motivo no puede pasar de 200 caracteres")
        String motivo
) {
}
