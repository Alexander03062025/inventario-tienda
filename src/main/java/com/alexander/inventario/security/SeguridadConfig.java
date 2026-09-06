package com.alexander.inventario.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Configuración central de seguridad.
 *
 * - Sin sesiones (STATELESS): cada petición se autentica con su token JWT.
 * - Rutas públicas: la página de login, los archivos estáticos y /api/auth/**.
 * - Todo lo demás (/api/**) exige token válido.
 * - Permisos por rol: se afinan con @PreAuthorize en cada controlador,
 *   más algunas reglas por método HTTP aquí abajo.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity   // habilita @PreAuthorize
public class SeguridadConfig {

    private final JwtAuthenticationFilter jwtFilter;
    private final PuntoEntradaNoAutenticado puntoEntrada;
    private final ManejadorAccesoDenegado accesoDenegado;

    public SeguridadConfig(JwtAuthenticationFilter jwtFilter, PuntoEntradaNoAutenticado puntoEntrada,
                           ManejadorAccesoDenegado accesoDenegado) {
        this.jwtFilter = jwtFilter;
        this.puntoEntrada = puntoEntrada;
        this.accesoDenegado = accesoDenegado;
    }

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // no aplica: API stateless con token
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // páginas y recursos estáticos
                .requestMatchers(HttpMethod.GET,
                        "/", "/index.html", "/login.html",
                        "/css/**", "/js/**", "/favicon.ico").permitAll()
                // registro y login
                .requestMatchers("/api/auth/**").permitAll()
                // consola H2 solo en desarrollo
                .requestMatchers("/h2-console/**").permitAll()
                // gestión de usuarios: solo ADMIN
                .requestMatchers("/api/usuarios/**").hasRole("ADMIN")
                // resto de la API: cualquier usuario autenticado
                .anyRequest().authenticated())
            .headers(h -> h.frameOptions(f -> f.sameOrigin())) // para la consola H2
            .exceptionHandling(e -> e
                    .authenticationEntryPoint(puntoEntrada)   // 401: sin sesión
                    .accessDeniedHandler(accesoDenegado))     // 403: rol insuficiente
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @SuppressWarnings("deprecation")
    DaoAuthenticationProvider authenticationProvider(UsuarioDetailsService uds, PasswordEncoder encoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(uds);
        provider.setPasswordEncoder(encoder);
        return provider;
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
