package com.alexander.inventario.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entidad MovimientoInventario: registra cada entrada o salida de stock.
 * Sirve de historial: nunca se borra, así queda la trazabilidad.
 */
@Entity
@Table(name = "movimiento_inventario")
public class MovimientoInventario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Muchos movimientos pertenecen a un producto (relación N:1). */
    @ManyToOne(optional = false)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TipoMovimiento tipo;

    @Column(nullable = false)
    private int cantidad;

    @Column(length = 200)
    private String motivo;

    /** Stock del producto DESPUÉS de aplicar este movimiento. */
    @Column(nullable = false)
    private int stockResultante;

    @Column(nullable = false, updatable = false)
    private LocalDateTime fecha;

    protected MovimientoInventario() {
    }

    public MovimientoInventario(Producto producto, TipoMovimiento tipo, int cantidad,
                                String motivo, int stockResultante) {
        this(producto, tipo, cantidad, motivo, stockResultante, null);
    }

    /**
     * @param fecha si es null se usa el momento actual. Permite registrar
     *              movimientos históricos (por ejemplo, importar un cuaderno).
     */
    public MovimientoInventario(Producto producto, TipoMovimiento tipo, int cantidad,
                                String motivo, int stockResultante, LocalDateTime fecha) {
        this.producto = producto;
        this.tipo = tipo;
        this.cantidad = cantidad;
        this.motivo = motivo;
        this.stockResultante = stockResultante;
        this.fecha = fecha;
    }

    @PrePersist
    void alCrear() {
        if (this.fecha == null) {
            this.fecha = LocalDateTime.now();
        }
    }

    // --- getters ---

    public Long getId() {
        return id;
    }

    public Producto getProducto() {
        return producto;
    }

    public TipoMovimiento getTipo() {
        return tipo;
    }

    public int getCantidad() {
        return cantidad;
    }

    public String getMotivo() {
        return motivo;
    }

    public int getStockResultante() {
        return stockResultante;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }
}
