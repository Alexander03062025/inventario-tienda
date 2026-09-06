package com.alexander.inventario.controller;

import com.alexander.inventario.dto.MovimientoRequest;
import com.alexander.inventario.dto.MovimientoResponse;
import com.alexander.inventario.service.MovimientoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST de movimientos de inventario. API bajo /api/movimientos.
 */
@RestController
@RequestMapping("/api/movimientos")
public class MovimientoController {

    private final MovimientoService service;

    public MovimientoController(MovimientoService service) {
        this.service = service;
    }

    /** GET /api/movimientos — historial completo, más reciente primero. */
    @GetMapping
    public List<MovimientoResponse> listar() {
        return service.listar().stream().map(MovimientoResponse::desde).toList();
    }

    /** POST /api/movimientos — registra una entrada o salida y ajusta el stock. */
    @PostMapping
    public ResponseEntity<MovimientoResponse> registrar(@Valid @RequestBody MovimientoRequest datos) {
        MovimientoResponse creado = MovimientoResponse.desde(service.registrar(datos));
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }
}
