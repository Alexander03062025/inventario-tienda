package com.alexander.inventario.controller;

import com.alexander.inventario.dto.ResumenInventario;
import com.alexander.inventario.service.ProductoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Expone las cifras del panel principal (dashboard).
 */
@RestController
@RequestMapping("/api/resumen")
public class ResumenController {

    private final ProductoService service;

    public ResumenController(ProductoService service) {
        this.service = service;
    }

    /** GET /api/resumen */
    @GetMapping
    public ResumenInventario resumen() {
        return service.resumen();
    }
}
