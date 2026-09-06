# Sistema de Gestión de Biblioteca Universitaria — Implementación de Microservicios

## Contexto

El sistema partía de un monolito Java que manejaba en una sola aplicación: gestión de usuarios, catálogo de libros, préstamos/devoluciones y notificaciones, todos compartiendo una única base de datos relacional.

El objetivo del taller fue descomponer ese monolito en microservicios independientes siguiendo los pasos del PDF guía.

---

## Paso 1 — Identificar los contextos acotados (Bounded Contexts)

Aplicando Domain-Driven Design (DDD), se identificaron 4 dominios con responsabilidades claramente separadas:

| Contexto | Responsabilidad |
|---|---|
| Gestión de Usuarios | Estudiantes, profesores y personal |
| Gestión de Catálogo | Libros, autores, ISBN, disponibilidad |
| Circulación | Préstamos y devoluciones |
| Notificaciones | Envío de mensajes a usuarios |

---

## Paso 2 — Microservicios definidos

Cada contexto acotado se convirtió en una aplicación Spring Boot independiente:

| Microservicio | Puerto | Base de datos propia |
|---|---|---|
| `microservicio-usuarios` | 8081 | H2 in-memory `usuariosdb` |
| `microservicio-catalogo` | 8082 | H2 in-memory `catalogodb` |
| `microservicio-circulacion` | 8083 | H2 in-memory `circulaciondb` |
| `microservicio-notificacion` | 8084 | H2 in-memory `notificaciondb` |

Cada servicio tiene su propio `pom.xml`, clase `Application.java` y ciclo de vida independiente. Un fallo en uno no derrumba a los demás.

---

## Paso 3 — Identificar Agregados (Domain-Driven Design)

Los agregados son grupos de entidades que se tratan como una unidad para los cambios de datos. Cada microservicio es dueño de su propio agregado y nadie más lo modifica directamente.

### Agregado Usuario (`microservicio-usuarios`)
- **Entidad raíz**: `Usuario`
- **Value Objects**: `UsuarioId` `@Embeddable`, `Email` `@Embeddable`, `Direccion` `@Embeddable`, `Credenciales` `@Embeddable`
- **Métodos de dominio**: `cambiarEmail()`, `actualizarDireccion()`

### Agregado Libro (`microservicio-catalogo`)
- **Entidad raíz**: `Libro`
- **Value Objects**: `LibroId` `@Embeddable`, `ISBN` `@Embeddable`, `Categoria` `@Embeddable`
- **Entidades internas**: `Autor`
- **Métodos de dominio**: `marcarComoDisponible()`, `marcarComoNoDisponible()`, `actualizarCategoria()`

### Agregado Préstamo (`microservicio-circulacion`)
- **Entidad raíz**: `Prestamo`
- **Value Objects**: `PrestamoId`, `UsuarioId`, `LibroId`, `FechaPrestamo`, `FechaDevolucionPrevista`
- **Enum de estado**: `EstadoPrestamo` → `ACTIVO`, `DEVUELTO`, `VENCIDO`
- **Método de dominio**: `marcarComoDevuelto()`

La comunicación entre agregados se hace **únicamente por ID**, nunca referenciando el objeto completo del otro dominio. Por ejemplo, `Prestamo` guarda `UsuarioId` y `LibroId`, no los objetos `Usuario` y `Libro`.

---

## Paso 4 — Código refactorizado por microservicio

Cada microservicio sigue una arquitectura en capas limpia:

```
model/       → Entidades JPA + Value Objects
repository/  → Interfaces JpaRepository
service/     → Lógica de negocio
controller/  → Endpoints REST
```

---

## Paso 5 — Comunicación entre servicios (REST con Feign)

El PDF propone `RestTemplate` para comunicación síncrona. Se implementó con **OpenFeign** (equivalente moderno declarativo).

### Cómo funciona Feign

En vez de construir llamadas HTTP manualmente, se declara una interfaz anotada y Feign genera el cliente automáticamente:

```java
@FeignClient(name = "catalogo-service", url = "http://localhost:8082")
public interface CatalogoClient {
    @GetMapping("/libros/{libroId}/disponible")
    Boolean isLibroDisponible(@PathVariable("libroId") String libroId);

    @PutMapping("/libros/{libroId}/disponibilidad")
    void actualizarDisponibilidad(@PathVariable("libroId") String libroId, @RequestBody Boolean disponible);
}
```

### Flujo de un préstamo (`POST /circulacion/prestar`)

```
Cliente
  │
  ▼
CirculacionController
  │
  ▼
CirculacionService
  ├──► CatalogoClient  ──► GET  http://catalogo:8082/libros/{id}/disponible
  │                    ──► PUT  http://catalogo:8082/libros/{id}/disponibilidad
  └──► NotificationClient ──► POST http://notificacion:8084/notificar
```

### Flujo de una notificación

```
NotificacionController
  │
  ▼
NotificacionService
  └──► UsuarioClient  ──► GET http://usuarios:8081/usuarios/{id}/email
                               (obtiene el email real del usuario)
```

---

## Paso 6 — Aplicaciones Spring Boot independientes

Cada servicio arranca con su propia clase `@SpringBootApplication` y es completamente autónomo. Se puede compilar, desplegar y escalar de forma individual sin afectar a los demás.

---

## Paso 7 — API Gateway

Se creó el proyecto `api-gateway` (puerto **8080**) usando **Spring Cloud Gateway**.

### ¿Qué hace el API Gateway?

Es el único punto de entrada al sistema. El cliente nunca llama directamente a los microservicios; todo pasa por el gateway, que enruta la petición al servicio correcto.

```
Cliente HTTP
     │
     ▼
API Gateway :8080
     ├── /usuarios/**    ──► usuarios-service  :8081
     ├── /libros/**      ──► catalogo-service  :8082
     ├── /circulacion/** ──► circulacion-service :8083
     └── /notificar/**   ──► notificacion-service :8084
```

Configuración en `application.yml`:

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: usuarios-service
          uri: lb://usuarios-service
          predicates:
            - Path=/usuarios/**
```

El prefijo `lb://` indica que la dirección se resuelve a través de Eureka con balanceo de carga.

---

## Paso 8 — Eureka (Service Discovery)

Se creó el proyecto `eureka-server` (puerto **8761**) usando **Spring Cloud Netflix Eureka**.

### ¿Qué es Eureka?

Eureka es el **registro central de servicios** del sistema. Funciona como una agenda de contactos dinámica: los microservicios se registran al arrancar y cualquiera puede consultar dónde está quién.

### Cómo funciona

```
┌─────────────────────────────────────────────────┐
│              EUREKA SERVER :8761                │
│                                                 │
│  usuarios-service    → http://localhost:8081    │
│  catalogo-service    → http://localhost:8082    │
│  circulacion-service → http://localhost:8083    │
│  notificacion-service→ http://localhost:8084    │
│  api-gateway         → http://localhost:8080    │
└─────────────────────────────────────────────────┘
        ▲  Se registran al arrancar
        ▼  Consultan antes de llamar

   Cada microservicio + el gateway
```

**Registro automático**: al añadir `spring-cloud-starter-netflix-eureka-client` al `pom.xml` y configurar la URL del servidor, Spring Boot se registra solo al arrancar.

**Heartbeat**: cada servicio envía un pulso cada 30 segundos. Si deja de llegar, Eureka lo elimina del registro (~90 segundos).

**Dashboard**: `http://localhost:8761` muestra todos los servicios activos en tiempo real.

### Configuración en cada microservicio

`pom.xml`:
```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
</dependency>
```

`application.properties`:
```properties
eureka.client.service-url.defaultZone=http://localhost:8761/eureka/
```

---

## Arquitectura final del sistema

```
                         ┌──────────────────┐
                         │  EUREKA SERVER   │
                         │    :8761         │
                         └────────┬─────────┘
                                  │ registro y descubrimiento
          ┌───────────────────────┼───────────────────────────┐
          │                       │                           │
   ┌──────▼──────┐         ┌──────▼──────┐           ┌───────▼──────┐
   │ API GATEWAY │         │  usuarios   │           │   catalogo   │
   │   :8080     │         │   :8081     │           │    :8082     │
   └──────┬──────┘         │  [H2 DB]    │           │   [H2 DB]    │
          │                └─────────────┘           └──────────────┘
          │ enruta
   ┌──────▼────────────────────────────────────────────────────────┐
   │                    circulacion :8083                          │
   │  prestarLibro() ──► CatalogoClient + NotificationClient       │
   │  devolverLibro() ──► CatalogoClient + NotificationClient      │
   │  [H2 DB]                                                      │
   └───────────────────────────────────────────────────────────────┘
                                  │
                         ┌────────▼─────────┐
                         │  notificacion    │
                         │    :8084         │
                         │  UsuarioClient   │
                         │  [H2 DB]         │
                         └──────────────────┘
```

---

## Orden de arranque

Para que el sistema funcione correctamente, arrancar en este orden:

1. `eureka-server` — debe estar listo antes que el resto
2. `microservicio-usuarios`, `microservicio-catalogo`, `microservicio-notificacion` — en cualquier orden
3. `microservicio-circulacion` — depende de catálogo y notificación
4. `api-gateway` — punto de entrada final

---

## Tecnologías utilizadas

| Tecnología | Versión | Uso |
|---|---|---|
| Spring Boot | 3.3.2 | Base de cada microservicio |
| Spring Data JPA | (Boot) | Persistencia con H2 |
| Spring Cloud Gateway | 2023.0.3 | API Gateway |
| Spring Cloud OpenFeign | 2023.0.3 | Comunicación REST entre servicios |
| Spring Cloud Netflix Eureka | 2023.0.3 | Service Discovery |
| H2 Database | (Boot) | Base de datos en memoria por servicio |
| Lombok | (Boot) | Reducción de boilerplate |
| Java | 17 | Lenguaje |
