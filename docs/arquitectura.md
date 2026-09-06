# Arquitectura

## Vista general

```
Navegador (index.html + app.js)
        │  fetch()  ─ JSON sobre HTTP
        ▼
┌───────────────────────────────────────────────┐
│  Spring Boot  (servidor Tomcat embebido)      │
│                                               │
│  Controller  →  Service  →  Repository        │
│  (@RestController) (@Service) (JpaRepository)  │
│                                     │         │
│                                     ▼         │
│                              Base de datos H2 │
│                              (archivo ./data) │
└───────────────────────────────────────────────┘
```

## Capas y responsabilidad

| Capa | Paquete | Qué hace | Ejemplo |
|---|---|---|---|
| **Controller** | `controller` | Recibe la petición HTTP, valida el cuerpo, devuelve JSON. No tiene lógica de negocio. | `ProductoController` |
| **Service** | `service` | Lógica de negocio y transacciones. Decide qué es válido. | `MovimientoService` calcula el stock nuevo y evita stock negativo |
| **Repository** | `repository` | Acceso a datos. Interfaces que Spring implementa solo. | `ProductoRepository.findConStockBajo()` |
| **Model** | `model` | Entidades JPA = tablas. | `Producto`, `MovimientoInventario`, `Usuario` |
| **DTO** | `dto` | Objetos de entrada/salida de la API, separados de las entidades. | `ProductoRequest` (entrada), `ProductoResponse` (salida) |
| **Security** | `security` | Autenticación con JWT y autorización por roles. | `JwtAuthenticationFilter`, `SeguridadConfig` |
| **Exception** | `exception` | Excepciones propias + traductor global a códigos HTTP. | `StockInsuficienteException` → HTTP 409 |

## Autenticación con JWT

```
1. POST /api/auth/login { username, password }
        │
        ▼
   AuthService: AuthenticationManager comprueba la contraseña (BCrypt)
        │
        ▼
   JwtService.generarToken()  ──►  devuelve un token firmado
        │
        ▼
   El navegador lo guarda en localStorage

2. En cada petición siguiente:
   Authorization: Bearer <token>
        │
        ▼
   JwtAuthenticationFilter: valida la firma y la expiración,
   carga el usuario y lo marca como autenticado (con su rol)
        │
        ▼
   AuthorizationFilter / @PreAuthorize: ¿el rol permite esta acción?
        │              │
       sí             no ──►  403 (ManejadorAccesoDenegado)
        ▼
     Controlador
```

- **Sin estado (stateless):** el servidor no guarda sesiones. Toda la
  información va en el token, que está firmado y no se puede falsificar sin
  la clave secreta (`APP_JWT_SECRET`).
- **401 vs 403:** falta de token → 401 (`PuntoEntradaNoAutenticado`);
  token válido pero rol insuficiente → 403 (`ManejadorAccesoDenegado`).
- La contraseña **nunca** se guarda en claro: `Usuario.password` es un hash BCrypt.

## ¿Por qué DTOs y no exponer las entidades?

- La API no debería depender de cómo está hecha la tabla.
- Evita exponer campos internos y problemas de carga perezosa (lazy loading).
- Permite validar la entrada con anotaciones (`@NotBlank`, `@Positive`).

## Regla de negocio principal

Registrar un movimiento y ajustar el stock del producto ocurren dentro de la
**misma transacción** (`@Transactional` en `MovimientoService.registrar`).
Si el ajuste falla (por ejemplo, salida mayor al stock disponible), no se
guarda ni el movimiento ni el cambio de stock: la base queda intacta.

## API REST

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/api/productos` | Lista productos. Filtros: `?buscar=` `?categoria=` |
| GET | `/api/productos/{id}` | Un producto |
| GET | `/api/productos/stock-bajo` | Productos en el mínimo o por debajo |
| POST | `/api/productos` | Crea un producto |
| PUT | `/api/productos/{id}` | Actualiza un producto |
| DELETE | `/api/productos/{id}` | Elimina un producto y su historial |
| GET | `/api/movimientos` | Historial de entradas/salidas |
| POST | `/api/movimientos` | Registra un movimiento y ajusta el stock |
| GET | `/api/resumen` | Cifras del panel (totales, valor, alertas) |

### Códigos de respuesta

| Código | Cuándo |
|---|---|
| 200 / 201 | OK |
| 400 | Datos inválidos (nombre vacío, cantidad ≤ 0…) |
| 401 | Falta el token o no es válido |
| 403 | El rol del usuario no permite esa acción |
| 404 | El producto no existe |
| 409 | Salida de más unidades de las que hay en stock, o username duplicado |
