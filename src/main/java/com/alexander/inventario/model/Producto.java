package com.alexander.inventario.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entidad Producto: se mapea a la tabla PRODUCTO de la base de datos.
 * Cada instancia = una fila.
 */
@Entity
@Table(name = "producto")
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(length = 60)
    private String categoria;

    @Column(nullable = false)
    private double precio;

    /** Unidades actualmente en bodega. Se ajusta con los movimientos. */
    @Column(nullable = false)
    private int stock;

    /** Umbral: si stock <= stockMinimo, el producto aparece en "stock bajo". */
    @Column(nullable = false)
    private int stockMinimo;

    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    /** JPA necesita un constructor vacío. */
    protected Producto() {
    }

    public Producto(String nombre, String categoria, double precio, int stock, int stockMinimo) {
        this.nombre = nombre;
        this.categoria = categoria;
        this.precio = precio;
        this.stock = stock;
        this.stockMinimo = stockMinimo;
    }

    /** Se ejecuta automáticamente justo antes de guardar por primera vez. */
    @PrePersist
    void alCrear() {
        this.fechaCreacion = LocalDateTime.now();
    }

    public boolean tieneStockBajo() {
        return stock <= stockMinimo;
    }

    public double valorEnInventario() {
        return precio * stock;
    }

    // --- getters y setters ---

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public int getStockMinimo() {
        return stockMinimo;
    }

    public void setStockMinimo(int stockMinimo) {
        this.stockMinimo = stockMinimo;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }
}
