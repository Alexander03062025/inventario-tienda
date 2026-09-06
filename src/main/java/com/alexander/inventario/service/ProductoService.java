package com.alexander.inventario.service;

import com.alexander.inventario.dto.ProductoRequest;
import com.alexander.inventario.dto.ResumenInventario;
import com.alexander.inventario.exception.RecursoNoEncontradoException;
import com.alexander.inventario.model.Producto;
import com.alexander.inventario.repository.MovimientoRepository;
import com.alexander.inventario.repository.ProductoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Capa de SERVICIO: contiene la lógica de negocio de productos.
 * El controlador nunca habla directo con el repositorio; pasa por aquí.
 */
@Service
public class ProductoService {

    private final ProductoRepository repo;
    private final MovimientoRepository movimientoRepo;

    public ProductoService(ProductoRepository repo, MovimientoRepository movimientoRepo) {
        this.repo = repo;
        this.movimientoRepo = movimientoRepo;
    }

    @Transactional(readOnly = true)
    public List<Producto> listar(String buscar, String categoria) {
        if (buscar != null && !buscar.isBlank()) {
            return repo.findByNombreContainingIgnoreCaseOrderByNombreAsc(buscar.trim());
        }
        if (categoria != null && !categoria.isBlank()) {
            return repo.findByCategoriaIgnoreCaseOrderByNombreAsc(categoria.trim());
        }
        return repo.findAll();
    }

    @Transactional(readOnly = true)
    public Producto obtener(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el producto con id " + id));
    }

    @Transactional(readOnly = true)
    public List<Producto> stockBajo() {
        return repo.findConStockBajo();
    }

    @Transactional
    public Producto crear(ProductoRequest datos) {
        Producto p = new Producto(datos.nombre(), datos.categoria(),
                datos.precio(), datos.stock(), datos.stockMinimo());
        return repo.save(p);
    }

    @Transactional
    public Producto actualizar(Long id, ProductoRequest datos) {
        Producto p = obtener(id);
        p.setNombre(datos.nombre());
        p.setCategoria(datos.categoria());
        p.setPrecio(datos.precio());
        p.setStock(datos.stock());
        p.setStockMinimo(datos.stockMinimo());
        return repo.save(p);
    }

    @Transactional
    public void eliminar(Long id) {
        if (!repo.existsById(id)) {
            throw new RecursoNoEncontradoException("No existe el producto con id " + id);
        }
        movimientoRepo.deleteByProductoId(id); // primero el historial, si no la BD lo impide
        repo.deleteById(id);
    }

    @Transactional(readOnly = true)
    public ResumenInventario resumen() {
        return new ResumenInventario(
                repo.count(),
                repo.contarConStockBajo(),
                repo.sumarUnidades(),
                repo.sumarValorInventario()
        );
    }
}
