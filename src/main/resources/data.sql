-- Datos de ejemplo. Se cargan al arrancar.
-- MERGE = "inserta o actualiza según el id", así re-arrancar no duplica filas.

MERGE INTO producto (id, nombre, categoria, precio, stock, stock_minimo, fecha_creacion) KEY(id) VALUES
 (1, 'Cuaderno universitario 100 hojas', 'Papelería',      1.25, 40,  10, CURRENT_TIMESTAMP),
 (2, 'Esferográfico azul',               'Papelería',      0.35,  8,  15, CURRENT_TIMESTAMP),
 (3, 'Resma de papel A4',                'Papelería',      4.50, 12,   5, CURRENT_TIMESTAMP),
 (4, 'Mouse inalámbrico',                'Tecnología',    9.90,  6,   4, CURRENT_TIMESTAMP),
 (5, 'Teclado USB',                      'Tecnología',   12.00,  3,   4, CURRENT_TIMESTAMP),
 (6, 'Audífonos con micrófono',          'Tecnología',   15.75, 20,   6, CURRENT_TIMESTAMP),
 (7, 'Botella de agua 500ml',            'Bebidas',        0.75, 50,  20, CURRENT_TIMESTAMP),
 (8, 'Café instantáneo sobre',           'Bebidas',        0.50,  2,  12, CURRENT_TIMESTAMP);

MERGE INTO movimiento_inventario (id, producto_id, tipo, cantidad, motivo, stock_resultante, fecha) KEY(id) VALUES
 (1, 1, 'ENTRADA', 50, 'Compra inicial a proveedor',       50, CURRENT_TIMESTAMP),
 (2, 1, 'SALIDA',  10, 'Venta mostrador',                  40, CURRENT_TIMESTAMP),
 (3, 2, 'SALIDA',  12, 'Venta por mayor a colegio',         8, CURRENT_TIMESTAMP),
 (4, 5, 'SALIDA',   7, 'Venta mostrador',                   3, CURRENT_TIMESTAMP),
 (5, 8, 'SALIDA',  10, 'Consumo interno oficina',           2, CURRENT_TIMESTAMP);

-- Mantiene el contador de IDs por encima de los datos sembrados
ALTER TABLE producto ALTER COLUMN id RESTART WITH 9;
ALTER TABLE movimiento_inventario ALTER COLUMN id RESTART WITH 6;
