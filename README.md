# 📦 Inventario de tienda — aplicación web full-stack

Sistema web para gestionar el inventario de una tienda pequeña: productos con
stock, precio y categoría; registro de entradas y salidas; alertas de stock
bajo y un panel con las cifras del negocio.

**Backend:** Java 21 + Spring Boot 3 (API REST, arquitectura por capas)
**Frontend:** HTML + CSS + JavaScript puro (sin frameworks), consumo de la API con `fetch`
**Base de datos:** H2 en archivo (no hay que instalar nada)

---

## ¿Qué demuestra este proyecto?

- **API REST** con Spring Boot: `@RestController`, verbos HTTP, códigos de estado correctos (201, 204, 400, 404, 409)
- **Arquitectura por capas**: controller → service → repository, cada una con una responsabilidad
- **Persistencia con JPA/Hibernate**: entidades, relaciones `@ManyToOne`, consultas derivadas y `@Query`
- **DTOs + validación** de la entrada con Bean Validation (`@NotBlank`, `@Positive`…)
- **Transacciones**: registrar un movimiento y ajustar el stock es atómico (`@Transactional`)
- **Manejo global de errores** con `@RestControllerAdvice` y `ProblemDetail`
- **Frontend sin frameworks**: `fetch`, `<dialog>` para los modales, delegación de eventos, diseño responsive

Detalle en [`docs/arquitectura.md`](docs/arquitectura.md).

---

## Cómo ejecutarlo

Requisitos: **JDK 17 o superior** (`java -version`). No hace falta instalar
Maven: el proyecto incluye el *Maven wrapper* (`mvnw`).

```bash
git clone https://github.com/Alexander03062025/inventario-tienda.git
cd inventario-tienda

# Windows
mvnw.cmd spring-boot:run

# Linux / macOS
./mvnw spring-boot:run
```

Abre **http://localhost:8080** en el navegador.
La base de datos se crea sola en `./data/` con 8 productos de ejemplo.

Consola de la base de datos: http://localhost:8080/h2-console
(JDBC URL `jdbc:h2:file:./data/inventario`, usuario `sa`, sin contraseña).

---

## Capturas

_Pendiente: ejecutar el proyecto y agregar en `docs/` una captura del panel
principal y otra del historial de movimientos (`Win + Shift + S` en Windows),
luego enlazarlas aquí._

---

## API REST

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/api/productos` | Lista productos (`?buscar=` `?categoria=`) |
| GET | `/api/productos/{id}` | Un producto |
| GET | `/api/productos/stock-bajo` | Productos en el mínimo o por debajo |
| POST | `/api/productos` | Crea un producto |
| PUT | `/api/productos/{id}` | Actualiza un producto |
| DELETE | `/api/productos/{id}` | Elimina un producto y su historial |
| GET | `/api/movimientos` | Historial de entradas/salidas |
| POST | `/api/movimientos` | Registra un movimiento y ajusta el stock |
| GET | `/api/resumen` | Totales, valor del inventario y alertas |

Ejemplo:

```bash
# Crear un producto
curl -X POST http://localhost:8080/api/productos \
  -H "Content-Type: application/json" \
  -d '{"nombre":"Cargador USB-C","categoria":"Tecnologia","precio":6.50,"stock":10,"stockMinimo":4}'

# Registrar una salida (venta de 3 unidades del producto 1)
curl -X POST http://localhost:8080/api/movimientos \
  -H "Content-Type: application/json" \
  -d '{"productoId":1,"tipo":"SALIDA","cantidad":3,"motivo":"Venta mostrador"}'
```

---

## Estructura

```
src/main/
├── java/com/alexander/inventario/
│   ├── InventarioApplication.java     punto de entrada
│   ├── model/          entidades JPA (Producto, MovimientoInventario, TipoMovimiento)
│   ├── dto/            objetos de entrada/salida de la API
│   ├── repository/     acceso a datos (Spring Data JPA)
│   ├── service/        lógica de negocio + transacciones
│   ├── controller/     endpoints REST
│   └── exception/      excepciones propias + manejador global
└── resources/
    ├── application.properties
    ├── data.sql        datos de ejemplo
    └── static/         frontend (index.html, css, js)
```

---

## Posibles mejoras (roadmap)

- [ ] Autenticación con login y roles (admin / vendedor)
- [ ] Paginación y ordenamiento en el listado
- [ ] Pruebas con JUnit + MockMvc
- [ ] Migrar de H2 a PostgreSQL y desplegar en Render
- [ ] Reporte de movimientos por rango de fechas (export a CSV)

---

Hecho por **Alexander** — proyecto de práctica full-stack con Java y Spring Boot.
