package com.alexander.inventario.exception;

/**
 * Se lanza cuando se pide un recurso (producto, movimiento) que no existe.
 * El manejador global la traduce a un HTTP 404.
 */
public class RecursoNoEncontradoException extends RuntimeException {

    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
