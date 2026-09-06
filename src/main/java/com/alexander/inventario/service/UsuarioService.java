package com.alexander.inventario.service;

import com.alexander.inventario.dto.RegistroRequest;
import com.alexander.inventario.exception.RecursoNoEncontradoException;
import com.alexander.inventario.exception.ValidacionException;
import com.alexander.inventario.model.Usuario;
import com.alexander.inventario.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Gestión de usuarios (alta, baja lógica, listado). Solo para ADMIN.
 */
@Service
public class UsuarioService {

    private final UsuarioRepository repo;
    private final PasswordEncoder encoder;

    public UsuarioService(UsuarioRepository repo, PasswordEncoder encoder) {
        this.repo = repo;
        this.encoder = encoder;
    }

    @Transactional(readOnly = true)
    public List<Usuario> listar() {
        return repo.findAllByOrderByUsernameAsc();
    }

    @Transactional
    public Usuario crear(RegistroRequest datos) {
        if (repo.existsByUsername(datos.username())) {
            throw new ValidacionException("Ya existe un usuario con el username \"" + datos.username() + "\"");
        }
        Usuario u = new Usuario(
                datos.username(),
                encoder.encode(datos.password()), // se guarda el hash BCrypt
                datos.nombre(),
                datos.rol());
        return repo.save(u);
    }

    @Transactional
    public Usuario cambiarEstado(Long id, boolean activo) {
        Usuario u = repo.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el usuario con id " + id));
        u.setActivo(activo);
        return repo.save(u);
    }
}
