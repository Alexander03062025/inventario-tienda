package com.alexander.inventario.exception;

/**
 * Regla de negocio incumplida (p. ej. username duplicado).
 * El manejador global la traduce a un HTTP 409.
 */
public class ValidacionException extends RuntimeException {

    public ValidacionException(String mensaje) {
        super(mensaje);
    }
}
