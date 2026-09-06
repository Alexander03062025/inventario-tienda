package com.alexander.inventario.model;

/**
 * Roles del sistema.
 *
 * ADMIN    : acceso total (crear/editar/borrar productos, gestionar usuarios).
 * VENDEDOR : ver el inventario y registrar entradas/salidas de stock.
 */
public enum Rol {
    ADMIN,
    VENDEDOR
}
