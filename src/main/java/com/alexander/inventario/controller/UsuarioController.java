package com.alexander.inventario.controller;

import com.alexander.inventario.dto.RegistroRequest;
import com.alexander.inventario.dto.UsuarioResponse;
import com.alexander.inventario.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Gestión de usuarios. TODO este controlador es solo para ADMIN
 * (además de la regla por URL en SeguridadConfig, se refuerza con @PreAuthorize).
 */
@RestController
@RequestMapping("/api/usuarios")
@PreAuthorize("hasRole('ADMIN')")
public class UsuarioController {

    private final UsuarioService service;

    public UsuarioController(UsuarioService service) {
        this.service = service;
    }

    @GetMapping
    public List<UsuarioResponse> listar() {
        return service.listar().stream().map(UsuarioResponse::desde).toList();
    }

    @PostMapping
    public ResponseEntity<UsuarioResponse> crear(@Valid @RequestBody RegistroRequest datos) {
        UsuarioResponse creado = UsuarioResponse.desde(service.crear(datos));
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    /** PATCH /api/usuarios/{id}/estado?activo=false  -> desactiva (baja lógica) */
    @PatchMapping("/{id}/estado")
    public UsuarioResponse cambiarEstado(@PathVariable Long id, @RequestParam boolean activo) {
        return UsuarioResponse.desde(service.cambiarEstado(id, activo));
    }
}
