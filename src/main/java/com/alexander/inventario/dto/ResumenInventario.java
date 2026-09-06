package com.alexander.inventario.dto;

/**
 * DTO de salida para el panel de resumen (dashboard).
 */
public record ResumenInventario(
        long totalProductos,
        long productosStockBajo,
        int unidadesTotales,
        double valorTotalInventario
) {
}
