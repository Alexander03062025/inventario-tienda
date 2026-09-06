package com.alexander.inventario.repository;

import com.alexander.inventario.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * Repositorio de Producto.
 *
 * Al extender JpaRepository ya tienes gratis: save(), findById(), findAll(),
 * deleteById(), count()... Spring genera la implementación en tiempo de ejecución.
 *
 * Además, Spring crea la consulta a partir del NOMBRE del método:
 * findByNombreContainingIgnoreCase -> "WHERE UPPER(nombre) LIKE UPPER('%texto%')".
 */
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    List<Producto> findByNombreContainingIgnoreCaseOrderByNombreAsc(String texto);

    List<Producto> findByCategoriaIgnoreCaseOrderByNombreAsc(String categoria);

    /** Productos cuyo stock cayó al mínimo o por debajo. */
    @Query("SELECT p FROM Producto p WHERE p.stock <= p.stockMinimo ORDER BY p.stock ASC")
    List<Producto> findConStockBajo();

    @Query("SELECT COUNT(p) FROM Producto p WHERE p.stock <= p.stockMinimo")
    long contarConStockBajo();

    @Query("SELECT COALESCE(SUM(p.stock), 0) FROM Producto p")
    int sumarUnidades();

    @Query("SELECT COALESCE(SUM(p.precio * p.stock), 0) FROM Producto p")
    double sumarValorInventario();
}
