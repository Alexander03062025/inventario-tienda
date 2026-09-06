package com.alexander.inventario.service;

import com.alexander.inventario.dto.AuthResponse;
import com.alexander.inventario.dto.LoginRequest;
import com.alexander.inventario.model.Usuario;
import com.alexander.inventario.repository.UsuarioRepository;
import com.alexander.inventario.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

/**
 * Lógica del login: comprueba usuario + contraseña y devuelve un token JWT.
 */
@Service
public class AuthService {

    private final AuthenticationManager authManager;
    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepo;

    public AuthService(AuthenticationManager authManager, JwtService jwtService,
                       UsuarioRepository usuarioRepo) {
        this.authManager = authManager;
        this.jwtService = jwtService;
        this.usuarioRepo = usuarioRepo;
    }

    public AuthResponse login(LoginRequest datos) {
        // Lanza BadCredentialsException si el usuario o la contraseña no coinciden.
        var authentication = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(datos.username(), datos.password()));

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Usuario usuario = usuarioRepo.findByUsername(datos.username()).orElseThrow();

        String token = jwtService.generarToken(
                userDetails, usuario.getNombre(), usuario.getRol().name());

        return new AuthResponse(
                token,
                usuario.getUsername(),
                usuario.getNombre(),
                usuario.getRol().name(),
                jwtService.getExpiracionMs());
    }
}
