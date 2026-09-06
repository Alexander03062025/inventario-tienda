package com.alexander.inventario.service;

import com.alexander.inventario.dto.MovimientoRequest;
import com.alexander.inventario.exception.RecursoNoEncontradoException;
import com.alexander.inventario.exception.StockInsuficienteException;
import com.alexander.inventario.model.MovimientoInventario;
import com.alexander.inventario.model.Producto;
import com.alexander.inventario.model.TipoMovimiento;
import com.alexander.inventario.repository.MovimientoRepository;
import com.alexander.inventario.repository.ProductoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Lógica de negocio de los movimientos de stock.
 *
 * Regla central: registrar un movimiento y ajustar el stock del producto
 * SIEMPRE van juntos. Por eso el método está marcado @Transactional:
 * si algo falla a mitad, no se guarda nada (todo o nada).
 */
@Service
public class MovimientoService {

    private final MovimientoRepository movimientoRepo;
    private final ProductoRepository productoRepo;

    public MovimientoService(MovimientoRepository movimientoRepo, ProductoRepository productoRepo) {
        this.movimientoRepo = movimientoRepo;
        this.productoRepo = productoRepo;
    }

    @Transactional(readOnly = true)
    public List<MovimientoInventario> listar() {
        return movimientoRepo.findAllByOrderByFechaDesc();
    }

    @Transactional
    public MovimientoInventario registrar(MovimientoRequest datos) {
        Producto producto = productoRepo.findById(datos.productoId())
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe el producto con id " + datos.productoId()));

        int stockNuevo = calcularStockNuevo(producto, datos.tipo(), datos.cantidad());

        producto.setStock(stockNuevo);
        productoRepo.save(producto);

        MovimientoInventario mov = new MovimientoInventario(
                producto, datos.tipo(), datos.cantidad(), datos.motivo(), stockNuevo);
        return movimientoRepo.save(mov);
    }

    private int calcularStockNuevo(Producto producto, TipoMovimiento tipo, int cantidad) {
        if (tipo == TipoMovimiento.ENTRADA) {
            return producto.getStock() + cantidad;
        }
        // SALIDA
        if (cantidad > producto.getStock()) {
            throw new StockInsuficienteException(String.format(
                    "No puedes sacar %d unidades de \"%s\": solo hay %d en stock",
                    cantidad, producto.getNombre(), producto.getStock()));
        }
        return producto.getStock() - cantidad;
    }
}
