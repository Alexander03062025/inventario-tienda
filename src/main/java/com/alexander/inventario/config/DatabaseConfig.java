package com.alexander.inventario.config;

import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;
import java.net.URI;

/**
 * Solo se usa en producción (perfil "prod").
 *
 * Render entrega la conexión a PostgreSQL en una sola variable con formato
 * de URI: postgresql://usuario:clave@host:puerto/basededatos
 *
 * Spring necesita eso separado en: URL JDBC + usuario + contraseña.
 * Esta clase hace la traducción.
 */
@Configuration
@Profile("prod")
public class DatabaseConfig {

    @Bean
    public DataSource dataSource() {
        String databaseUrl = System.getenv("DATABASE_URL");
        if (databaseUrl == null || databaseUrl.isBlank()) {
            throw new IllegalStateException("Falta la variable de entorno DATABASE_URL");
        }

        URI uri = URI.create(databaseUrl);
        String[] credenciales = uri.getUserInfo().split(":", 2);
        int puerto = uri.getPort() == -1 ? 5432 : uri.getPort();

        String jdbcUrl = "jdbc:postgresql://" + uri.getHost() + ":" + puerto + uri.getPath()
                + "?sslmode=require";

        return DataSourceBuilder.create()
                .driverClassName("org.postgresql.Driver")
                .url(jdbcUrl)
                .username(credenciales[0])
                .password(credenciales.length > 1 ? credenciales[1] : "")
                .build();
    }
}
