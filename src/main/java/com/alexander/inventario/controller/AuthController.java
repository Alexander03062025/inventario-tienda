package com.alexander.inventario.controller;

import com.alexander.inventario.dto.AuthResponse;
import com.alexander.inventario.dto.LoginRequest;
import com.alexander.inventario.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * Endpoints de autenticación (rutas públicas, no requieren token).
 *
 * El REGISTRO de usuarios NO está aquí: crear usuarios es una acción de ADMIN
 * y vive en /api/usuarios (UsuarioController). El primer admin se crea solo
 * al arrancar la app (ver DataSeeder).
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /** POST /api/auth/login  -> { token, username, nombre, rol, expiraEnMs } */
    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest datos) {
        return authService.login(datos);
    }
}
