package com.alexander.inventario.controller;

import com.alexander.inventario.dto.ProductoRequest;
import com.alexander.inventario.dto.ProductoResponse;
import com.alexander.inventario.service.ProductoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST de productos. Expone la API bajo /api/productos.
 *
 * Lectura (GET): cualquier usuario autenticado (ADMIN o VENDEDOR).
 * Escritura (POST/PUT/DELETE): solo ADMIN, con @PreAuthorize.
 */
@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    private final ProductoService service;

    public ProductoController(ProductoService service) {
        this.service = service;
    }

    /** GET /api/productos?buscar=texto&categoria=xxx */
    @GetMapping
    public List<ProductoResponse> listar(@RequestParam(required = false) String buscar,
                                         @RequestParam(required = false) String categoria) {
        return service.listar(buscar, categoria).stream()
                .map(ProductoResponse::desde)
                .toList();
    }

    /** GET /api/productos/stock-bajo */
    @GetMapping("/stock-bajo")
    public List<ProductoResponse> stockBajo() {
        return service.stockBajo().stream().map(ProductoResponse::desde).toList();
    }

    /** GET /api/productos/{id} */
    @GetMapping("/{id}")
    public ProductoResponse obtener(@PathVariable Long id) {
        return ProductoResponse.desde(service.obtener(id));
    }

    /** POST /api/productos */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductoResponse> crear(@Valid @RequestBody ProductoRequest datos) {
        ProductoResponse creado = ProductoResponse.desde(service.crear(datos));
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    /** PUT /api/productos/{id} */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ProductoResponse actualizar(@PathVariable Long id,
                                       @Valid @RequestBody ProductoRequest datos) {
        return ProductoResponse.desde(service.actualizar(id, datos));
    }

    /** DELETE /api/productos/{id} */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        service.eliminar(id);
    }
}
