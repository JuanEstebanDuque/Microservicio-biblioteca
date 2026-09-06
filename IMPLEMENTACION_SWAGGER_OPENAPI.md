# Documentación de API con OpenAPI y Swagger

## ¿Qué es OpenAPI?

**OpenAPI** es un estándar de la industria para describir APIs RESTful de forma estructurada y legible tanto por humanos como por máquinas. Define un contrato de la API: qué endpoints existen, qué parámetros reciben, qué respuestas retornan y qué modelos de datos manejan.

La especificación se genera automáticamente en formato JSON y es accesible en:
```
http://localhost:{puerto}/v3/api-docs
```

## ¿Qué es Swagger UI?

**Swagger** es un conjunto de herramientas construidas sobre OpenAPI. Su pieza más visible es **Swagger UI**: una interfaz web interactiva que lee la especificación OpenAPI y genera automáticamente una página de documentación donde se pueden:

- Ver todos los endpoints disponibles
- Leer descripciones de cada operación
- Probar las llamadas directamente desde el navegador (sin necesidad de Postman u otra herramienta)

La interfaz es accesible en:
```
http://localhost:{puerto}/swagger-ui/index.html
```

---

## Dependencia utilizada

La librería `springdoc-openapi` integra OpenAPI 3 y Swagger UI en proyectos Spring Boot con una sola dependencia. Ya estaba incluida en todos los microservicios:

```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.8.5</version>
</dependency>
```

No requiere ninguna configuración adicional de Spring para activarse. Al arrancar la aplicación, el endpoint `/swagger-ui/index.html` queda disponible automáticamente.

---

## Anotaciones implementadas

### `@Tag` — Agrupa endpoints por controlador

Se aplica a nivel de clase. Agrupa todos los endpoints del controlador bajo un nombre común en la UI de Swagger.

```java
@RestController
@RequestMapping("/usuarios")
@Tag(name = "Usuarios", description = "Gestión de usuarios del sistema de biblioteca universitaria")
public class UsuarioController { ... }
```

### `@Operation` — Documenta un endpoint

Se aplica a cada método del controlador. Permite añadir un resumen corto y una descripción detallada.

```java
@Operation(
    summary = "Realizar préstamo de un libro",
    description = "Registra el préstamo de un libro a un usuario. " +
                  "Verifica la disponibilidad en el catálogo y envía notificación al usuario."
)
@PostMapping("/prestar")
public void prestarLibro(...) { ... }
```

### `@ApiResponse` / `@ApiResponses` — Documenta los códigos de respuesta HTTP

```java
@ApiResponses({
    @ApiResponse(responseCode = "200", description = "Préstamo registrado exitosamente"),
    @ApiResponse(responseCode = "400", description = "El libro no está disponible"),
    @ApiResponse(responseCode = "404", description = "Usuario o libro no encontrado")
})
```

### `@Parameter` — Documenta parámetros individuales

Se aplica a cada parámetro del método para explicar su propósito.

```java
public void prestarLibro(
    @Parameter(description = "Identificador del usuario que realiza el préstamo")
    @RequestParam String usuarioId,
    @Parameter(description = "Identificador del libro a prestar")
    @RequestParam String libroId) { ... }
```

### `OpenAPI` Bean — Información global de la API

Se creó una clase `OpenApiConfig` en cada microservicio para definir el título, descripción y versión de la API:

```java
@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Microservicio de Circulación")
                        .description("API para la gestión de préstamos y devoluciones de libros.")
                        .version("1.0.0"));
    }
}
```

---

## Lo que se documentó en cada microservicio

### Microservicio de Usuarios — `localhost:8081`

| Método | Endpoint | Descripción |
|---|---|---|
| GET | `/usuarios/{id}` | Obtener usuario completo por ID |
| GET | `/usuarios/{id}/email` | Obtener solo el email (para notificaciones) |
| PUT | `/usuarios/{id}/email` | Actualizar email de un usuario |

Swagger UI: `http://localhost:8081/swagger-ui/index.html`

---

### Microservicio de Catálogo — `localhost:8082`

| Método | Endpoint | Descripción |
|---|---|---|
| GET | `/libros/{id}` | Obtener libro completo por ID |
| GET | `/libros/{id}/disponible` | Consultar si el libro está disponible |
| PUT | `/libros/{id}/disponibilidad` | Actualizar disponibilidad del libro |
| GET | `/libros/buscar?criterio=` | Buscar libros por título |

Swagger UI: `http://localhost:8082/swagger-ui/index.html`

---

### Microservicio de Circulación — `localhost:8083`

| Método | Endpoint | Descripción |
|---|---|---|
| POST | `/circulacion/prestar` | Registrar préstamo (verifica catálogo + notifica) |
| POST | `/circulacion/devolver` | Registrar devolución (libera catálogo + notifica) |
| GET | `/circulacion/prestamos` | Listar todos los préstamos |

Swagger UI: `http://localhost:8083/swagger-ui/index.html`

---

### Microservicio de Notificaciones — `localhost:8084`

| Método | Endpoint | Descripción |
|---|---|---|
| POST | `/notificar` | Enviar notificación (consulta email en usuarios) |

Swagger UI: `http://localhost:8084/swagger-ui/index.html`

---

## Cómo probar con Swagger UI

1. Arrancar los servicios en orden (ver `IMPLEMENTACION_MICROSERVICIOS.md`)
2. Abrir en el navegador la URL del servicio que se quiere probar
3. Seleccionar un endpoint, hacer clic en **"Try it out"**
4. Completar los parámetros requeridos
5. Hacer clic en **"Execute"** para enviar la petición real

### Ejemplo — Prestar un libro desde Swagger UI

```
URL: http://localhost:8083/swagger-ui/index.html
Sección: Circulación
Endpoint: POST /circulacion/prestar
Parámetros:
  usuarioId: 1
  libroId: 1
```

---

## Relación entre las capas de documentación

```
Código Java (anotaciones @Operation, @Tag, @ApiResponse)
        │
        ▼
springdoc-openapi genera automáticamente
        │
        ├──► JSON en /v3/api-docs  (legible por máquinas / Postman / herramientas CI)
        │
        └──► HTML en /swagger-ui/  (interfaz visual interactiva para pruebas)
```

---

## Resumen de archivos añadidos/modificados

| Archivo | Cambio |
|---|---|
| `*/config/OpenApiConfig.java` | Nuevo — título, descripción y versión de cada API |
| `*/controller/UsuarioController.java` | `@Tag`, `@Operation`, `@ApiResponse`, `@Parameter` en los 3 endpoints |
| `*/controller/CatalogoController.java` | `@Tag`, `@Operation`, `@ApiResponse`, `@Parameter` en los 4 endpoints |
| `*/controller/CirculacionController.java` | `@Tag`, `@Operation`, `@ApiResponse`, `@Parameter` en los 3 endpoints |
| `*/controller/NotificacionController.java` | `@Tag`, `@Operation`, `@ApiResponse` en el endpoint de notificación |
