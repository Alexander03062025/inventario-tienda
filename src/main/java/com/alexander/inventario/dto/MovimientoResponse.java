package com.alexander.inventario.dto;

import com.alexander.inventario.model.MovimientoInventario;
import com.alexander.inventario.model.TipoMovimiento;

import java.time.LocalDateTime;

public record MovimientoResponse(
        Long id,
        Long productoId,
        String productoNombre,
        TipoMovimiento tipo,
        int cantidad,
        String motivo,
        int stockResultante,
        LocalDateTime fecha
) {
    public static MovimientoResponse desde(MovimientoInventario m) {
        return new MovimientoResponse(
                m.getId(),
                m.getProducto().getId(),
                m.getProducto().getNombre(),
                m.getTipo(),
                m.getCantidad(),
                m.getMotivo(),
                m.getStockResultante(),
                m.getFecha());
    }
}
