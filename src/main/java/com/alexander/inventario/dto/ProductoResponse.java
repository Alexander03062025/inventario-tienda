package com.alexander.inventario.dto;

import com.alexander.inventario.model.Producto;

import java.time.LocalDateTime;

/**
 * DTO de salida: lo que la API devuelve al frontend por cada producto.
 * Se construye a partir de la entidad con el método estático desde().
 */
public record ProductoResponse(
        Long id,
        String nombre,
        String categoria,
        double precio,
        int stock,
        int stockMinimo,
        boolean stockBajo,
        double valorEnInventario,
        LocalDateTime fechaCreacion
) {
    public static ProductoResponse desde(Producto p) {
        return new ProductoResponse(
                p.getId(), p.getNombre(), p.getCategoria(), p.getPrecio(),
                p.getStock(), p.getStockMinimo(), p.tieneStockBajo(),
                p.valorEnInventario(), p.getFechaCreacion());
    }
}
