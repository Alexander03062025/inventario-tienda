package com.alexander.inventario.config;

import com.alexander.inventario.model.*;
import com.alexander.inventario.repository.MovimientoRepository;
import com.alexander.inventario.repository.ProductoRepository;
import com.alexander.inventario.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Siembra datos al arrancar, SOLO si las tablas están vacías.
 *
 * Se hace en Java (no en data.sql) para que funcione igual en H2 y en
 * PostgreSQL, y para poder cifrar la contraseña del admin con BCrypt.
 *
 * El usuario admin inicial se toma de configuración (application.properties
 * o variables de entorno en producción): app.admin.username / app.admin.password.
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final UsuarioRepository usuarioRepo;
    private final ProductoRepository productoRepo;
    private final MovimientoRepository movimientoRepo;
    private final PasswordEncoder encoder;
    private final String adminUsername;
    private final String adminPassword;
    private final String adminNombre;

    public DataSeeder(UsuarioRepository usuarioRepo, ProductoRepository productoRepo,
                      MovimientoRepository movimientoRepo, PasswordEncoder encoder,
                      @Value("${app.admin.username}") String adminUsername,
                      @Value("${app.admin.password}") String adminPassword,
                      @Value("${app.admin.nombre}") String adminNombre) {
        this.usuarioRepo = usuarioRepo;
        this.productoRepo = productoRepo;
        this.movimientoRepo = movimientoRepo;
        this.encoder = encoder;
        this.adminUsername = adminUsername;
        this.adminPassword = adminPassword;
        this.adminNombre = adminNombre;
    }

    @Override
    public void run(String... args) {
        sembrarAdmin();
        sembrarInventario();
    }

    private void sembrarAdmin() {
        if (usuarioRepo.count() > 0) return;
        usuarioRepo.save(new Usuario(
                adminUsername, encoder.encode(adminPassword), adminNombre, Rol.ADMIN));
        // un vendedor de ejemplo para probar los permisos
        usuarioRepo.save(new Usuario(
                "vendedor", encoder.encode("vendedor123"), "Vendedor de ejemplo", Rol.VENDEDOR));
        log.info("Usuarios iniciales creados: '{}' (ADMIN) y 'vendedor' (VENDEDOR)", adminUsername);
    }

    private void sembrarInventario() {
        if (productoRepo.count() > 0) return;

        List<Producto> productos = productoRepo.saveAll(List.of(
                new Producto("Cuaderno universitario 100 hojas", "Papelería", 1.25, 40, 10),
                new Producto("Esferográfico azul", "Papelería", 0.35, 8, 15),
                new Producto("Resma de papel A4", "Papelería", 4.50, 12, 5),
                new Producto("Mouse inalámbrico", "Tecnología", 9.90, 6, 4),
                new Producto("Teclado USB", "Tecnología", 12.00, 3, 4),
                new Producto("Audífonos con micrófono", "Tecnología", 15.75, 20, 6),
                new Producto("Botella de agua 500ml", "Bebidas", 0.75, 50, 20),
                new Producto("Café instantáneo sobre", "Bebidas", 0.50, 2, 12)
        ));

        Producto cuaderno = productos.get(0);
        movimientoRepo.save(new MovimientoInventario(
                cuaderno, TipoMovimiento.ENTRADA, 50, "Compra inicial a proveedor", 50));
        movimientoRepo.save(new MovimientoInventario(
                cuaderno, TipoMovimiento.SALIDA, 10, "Venta mostrador", 40));
        movimientoRepo.save(new MovimientoInventario(
                productos.get(1), TipoMovimiento.SALIDA, 12, "Venta por mayor a colegio", 8));

        log.info("Inventario de ejemplo creado: {} productos", productos.size());
    }
}
