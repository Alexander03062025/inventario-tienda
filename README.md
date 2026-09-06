# 📦 Inventario de tienda — aplicación web full-stack

Sistema web para gestionar el inventario de una tienda pequeña: productos con
stock, precio y categoría; registro de entradas y salidas; alertas de stock
bajo; panel con las cifras del negocio; **login con roles**.

**Backend:** Java 21 + Spring Boot 3 · API REST · Spring Security + JWT
**Frontend:** HTML + CSS + JavaScript puro (sin frameworks), consumo de la API con `fetch`
**Base de datos:** H2 en archivo (local) · PostgreSQL (producción)
**Despliegue:** Docker + Render (`render.yaml`)

> 🌐 **Demo en vivo:** _pendiente de desplegar — ver sección "Despliegue"_

---

## ¿Qué demuestra este proyecto?

- **API REST** con Spring Boot: `@RestController`, verbos HTTP, códigos correctos (201, 204, 400, 401, 403, 404, 409)
- **Autenticación con JWT** y **Spring Security**: login, tokens firmados, filtro propio, contraseñas con BCrypt
- **Autorización por roles**: `ADMIN` y `VENDEDOR`, con reglas por URL y `@PreAuthorize` por método
- **Arquitectura por capas**: controller → service → repository
- **Persistencia con JPA/Hibernate**: entidades, relaciones `@ManyToOne`, consultas derivadas y `@Query`
- **DTOs + validación** con Bean Validation (`@NotBlank`, `@Positive`…)
- **Transacciones**: registrar un movimiento y ajustar el stock es atómico (`@Transactional`)
- **Manejo global de errores** con `@RestControllerAdvice` y `ProblemDetail`
- **Configuración por entornos**: perfil `prod`, variables de entorno, sin secretos en el repo
- **Contenerización y despliegue**: `Dockerfile` multi-etapa + blueprint de Render

Detalle en [`docs/arquitectura.md`](docs/arquitectura.md).

---

## Roles

| Acción | ADMIN | VENDEDOR |
|---|:---:|:---:|
| Ver inventario, buscar, ver movimientos y resumen | ✅ | ✅ |
| Registrar entradas/salidas de stock | ✅ | ✅ |
| Crear, editar y borrar productos | ✅ | ❌ |
| Gestionar usuarios | ✅ | ❌ |

Usuarios de ejemplo (creados solos al arrancar):

| Usuario | Contraseña | Rol |
|---|---|---|
| `admin` | `admin123` | ADMIN |
| `vendedor` | `vendedor123` | VENDEDOR |

---

## Cómo ejecutarlo en local

Requisitos: **JDK 17 o superior**. Incluye el *Maven wrapper* (`mvnw`), no hace falta instalar Maven.

```bash
git clone https://github.com/Alexander03062025/inventario-tienda.git
cd inventario-tienda

mvnw.cmd spring-boot:run     # Windows
./mvnw spring-boot:run       # Linux / macOS
```

Abre **http://localhost:8080** → te pide iniciar sesión.
La base de datos H2 se crea sola en `./data/` con datos de ejemplo.

---

## API REST

Todas las rutas `/api/**` (menos `/api/auth/**`) requieren la cabecera
`Authorization: Bearer <token>`.

| Método | Ruta | Rol | Descripción |
|---|---|---|---|
| POST | `/api/auth/login` | público | Devuelve el token JWT |
| GET | `/api/productos` | autenticado | Lista productos (`?buscar=` `?categoria=`) |
| GET | `/api/productos/{id}` | autenticado | Un producto |
| GET | `/api/productos/stock-bajo` | autenticado | Productos en el mínimo o por debajo |
| POST | `/api/productos` | ADMIN | Crea un producto |
| PUT | `/api/productos/{id}` | ADMIN | Actualiza un producto |
| DELETE | `/api/productos/{id}` | ADMIN | Elimina un producto y su historial |
| GET | `/api/movimientos` | autenticado | Historial de entradas/salidas |
| POST | `/api/movimientos` | autenticado | Registra un movimiento y ajusta el stock |
| GET | `/api/resumen` | autenticado | Totales, valor del inventario y alertas |
| GET | `/api/usuarios` | ADMIN | Lista de usuarios |
| POST | `/api/usuarios` | ADMIN | Crea un usuario |
| PATCH | `/api/usuarios/{id}/estado?activo=` | ADMIN | Activa / desactiva un usuario |

Ejemplo:

```bash
# 1. Login -> copia el "token" de la respuesta
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'

# 2. Usar el token
TOKEN="eyJhbGci..."
curl http://localhost:8080/api/productos -H "Authorization: Bearer $TOKEN"

# 3. Registrar una salida de stock
curl -X POST http://localhost:8080/api/movimientos \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"productoId":1,"tipo":"SALIDA","cantidad":3,"motivo":"Venta mostrador"}'
```

---

## Despliegue en Render (gratis)

El repo trae [`render.yaml`](render.yaml): describe un servicio web (Docker) y
una base de datos PostgreSQL.

1. Crea una cuenta en <https://render.com> y conéctala con tu GitHub.
2. **New → Blueprint** → elige este repositorio. Render lee `render.yaml`.
3. Te pedirá el valor de **`APP_ADMIN_PASSWORD`** (la contraseña del admin en producción). Ponla ahí, no en el código.
4. **Apply**. Render construye la imagen Docker, crea la base de datos y publica la app.
5. Cuando termine, tendrás una URL `https://inventario-tienda.onrender.com`.

Variables de entorno que usa en producción (las gestiona Render):

| Variable | De dónde sale |
|---|---|
| `SPRING_PROFILES_ACTIVE=prod` | fija en `render.yaml` |
| `DATABASE_URL` | la base de datos PostgreSQL de Render |
| `APP_JWT_SECRET` | Render genera una aleatoria |
| `APP_ADMIN_PASSWORD` | la escribes tú en el panel |

> El plan gratuito de Render "duerme" el servicio tras 15 min de inactividad;
> la primera petición después tarda ~30 s en despertar.

---

## Estructura

```
src/main/
├── java/com/alexander/inventario/
│   ├── InventarioApplication.java     punto de entrada
│   ├── config/         siembra de datos + configuración de PostgreSQL en prod
│   ├── model/          entidades JPA (Producto, Movimiento, Usuario, Rol…)
│   ├── dto/            objetos de entrada/salida de la API
│   ├── repository/     acceso a datos (Spring Data JPA)
│   ├── service/        lógica de negocio + transacciones
│   ├── controller/     endpoints REST
│   ├── security/       JWT: filtro, config, servicio de tokens, handlers 401/403
│   └── exception/      excepciones propias + manejador global
└── resources/
    ├── application.properties          config común + H2 (local)
    ├── application-prod.properties      config de producción
    └── static/                          frontend (login.html, index.html, css, js)
```

---

## Capturas

_Pendiente: ejecutar el proyecto y añadir en `docs/` una captura del login,
del panel principal y de la sección de usuarios._

---

## Posibles mejoras (roadmap)

- [ ] Refresh tokens y expiración corta del access token
- [ ] Pruebas con JUnit + MockMvc + Spring Security Test
- [ ] Paginación y ordenamiento en los listados
- [ ] Reporte de movimientos por rango de fechas (export a CSV)
- [ ] Registro de auditoría (quién hizo cada movimiento)

---

Hecho por **Alexander** — proyecto de práctica full-stack con Java, Spring Boot y despliegue en la nube.
