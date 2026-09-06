package com.alexander.inventario.exception;

/**
 * Se lanza al intentar una SALIDA de más unidades de las que hay en stock.
 * El manejador global la traduce a un HTTP 409 (conflicto).
 */
public class StockInsuficienteException extends RuntimeException {

    public StockInsuficienteException(String mensaje) {
        super(mensaje);
    }
}
