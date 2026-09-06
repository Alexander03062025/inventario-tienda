package com.alexander.inventario.repository;

import com.alexander.inventario.model.MovimientoInventario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MovimientoRepository extends JpaRepository<MovimientoInventario, Long> {

    /** Historial completo, del más reciente al más antiguo. */
    List<MovimientoInventario> findAllByOrderByFechaDesc();

    List<MovimientoInventario> findByProductoIdOrderByFechaDesc(Long productoId);

    /** Borra el historial de un producto (se usa al eliminar el producto). */
    void deleteByProductoId(Long productoId);
}
