# Sistema de Gestión de Biblioteca Universitaria — Microservicios

Sistema de gestión de biblioteca implementado con arquitectura de microservicios usando Spring Boot 3, Spring Cloud, Keycloak y JWT.

---

## Arquitectura

| Servicio | Puerto | Descripción |
|----------|--------|-------------|
| Eureka Server | 8761 | Registro y descubrimiento de servicios |
| API Gateway | 8080 | Punto de entrada único, valida JWT |
| Microservicio Usuarios | 8081 | Gestión de usuarios |
| Microservicio Catálogo | 8082 | Gestión de libros |
| Microservicio Circulación | 8083 | Préstamos y devoluciones |
| Microservicio Notificaciones | 8084 | Envío de notificaciones |
| Keycloak | 8180 | Servidor de autenticación OAuth2/JWT |

---

## Requisitos previos

- Java 17+
- Maven 3.8+
- Docker Desktop (corriendo)
- Postman

---

## 1. Ejecución

### 1.1 Levantar Keycloak

Desde la raíz del proyecto:

```bash
docker compose up -d
```

### 1.2 Arrancar microservicios (respetar el orden)

Abrir una terminal por servicio:

```bash
# Terminal 1
cd eureka-server && mvn spring-boot:run

# Terminal 2
cd microservicio-usuarios && mvn spring-boot:run

# Terminal 3
cd microservicio-catalogo && mvn spring-boot:run

# Terminal 4
cd microservicio-circulacion && mvn spring-boot:run

# Terminal 5
cd microservicio-notificacion && mvn spring-boot:run

# Terminal 6 (último)
cd api-gateway && mvn spring-boot:run
```

Verificar en Eureka que todos aparezcan como UP: `http://localhost:8761`

---

## 2. Configuración de Keycloak (primera vez)

### 2.1 Acceder a la consola admin
- URL: `http://localhost:8180`
- Usuario: `admin` | Contraseña: `admin`

### 2.2 Crear Realm
1. Menú desplegable superior izquierdo → **Create Realm**
2. Realm name: `biblioteca` → **Create**

### 2.3 Crear Roles
**Realm roles** → **Create role** → crear los 3 roles:
- `ESTUDIANTE`
- `BIBLIOTECARIO`
- `ADMIN`

### 2.4 Crear Client
1. **Clients** → **Create client**
2. Client ID: `biblioteca-client` | Client type: `OpenID Connect` → **Next**
3. Client authentication: **OFF** | Direct access grants: **ON** → **Next**
4. Valid redirect URIs: `*` → **Save**

### 2.5 Crear usuarios de prueba

Para cada usuario:
1. **Users** → **Create new user**
2. Llenar **Username**, **First name**, **Last name**, **Email** → activar **Email verified: ON** → **Create**
3. Pestaña **Credentials** → **Set password** → Temporary: **OFF** → **Save**
4. Pestaña **Role mapping** → **Assign role** → seleccionar el rol

| Username | Password | Rol |
|----------|----------|-----|
| `estudiante1` | `estudiante123` | ESTUDIANTE |
| `bibliotecario1` | `biblio123` | BIBLIOTECARIO |
| `admin1` | `admin123` | ADMIN |

---

## 3. Pruebas en Postman

### 3.1 Obtener token JWT

**POST** `http://localhost:8180/realms/biblioteca/protocol/openid-connect/token`

Body → `x-www-form-urlencoded`:

| Key | Value |
|-----|-------|
| grant_type | password |
| client_id | biblioteca-client |
| username | estudiante1 |
| password | estudiante123 |

Copiar el `access_token` de la respuesta y usarlo en todas las peticiones como:
```
Authorization: Bearer <access_token>
```

> Los tokens expiran en 5 minutos. Obtener uno nuevo si aparece 401.

---

### 3.2 Flujo completo (ejecutar en orden, sin reiniciar servicios)

#### Paso 1 — Crear usuario
**POST** `http://localhost:8080/usuarios`
```json
{
  "id": "u001",
  "nombre": "Juan Pérez",
  "email": "juan@universidad.edu",
  "calle": "Calle 123",
  "ciudad": "Bogotá",
  "codigoPostal": "110111",
  "username": "juanp",
  "passwordHash": "clave123"
}
```

#### Paso 2 — Consultar usuario
**GET** `http://localhost:8080/usuarios/u001`

#### Paso 3 — Crear libro
**POST** `http://localhost:8080/libros`
```json
{
  "id": "lib001",
  "titulo": "Clean Code",
  "isbn": "978-0132350884",
  "categoria": "Programación",
  "disponible": true
}
```

#### Paso 4 — Listar libros
**GET** `http://localhost:8080/libros`

#### Paso 5 — Consultar libro por ID
**GET** `http://localhost:8080/libros/lib001`

#### Paso 6 — Realizar préstamo
**POST** `http://localhost:8080/circulacion/prestar?usuarioId=u001&libroId=lib001`

Sin body. Token de `estudiante1`, `bibliotecario1` o `admin1`.

#### Paso 7 — Ver todos los préstamos (token de admin o bibliotecario)
**GET** `http://localhost:8080/circulacion/prestamos`

Copiar el UUID del campo `id.prestamoid_value`.

#### Paso 8 — Devolver libro
**POST** `http://localhost:8080/circulacion/devolver?prestamoId=<UUID-del-paso-7>`

---

### 3.3 Pruebas de seguridad

#### Sin token → 401 Unauthorized
```
GET http://localhost:8080/libros
(sin header Authorization)
```
Respuesta esperada: `401 Unauthorized`

#### Token expirado → 401 Unauthorized
Esperar más de 5 minutos después de obtener el token y repetir cualquier petición.
Respuesta esperada: `401 Unauthorized`

#### Rol insuficiente → 403 Forbidden
Con token de `estudiante1`:
```
GET http://localhost:8080/circulacion/prestamos
Authorization: Bearer <token_estudiante>
```
Respuesta esperada: `403 Forbidden`

Solo `BIBLIOTECARIO` y `ADMIN` pueden ver todos los préstamos.

---

## 4. Exportar configuración de Keycloak

El taller requiere entregar el archivo de configuración del realm exportado.

1. Ir a `http://localhost:8180` → realm `biblioteca`
2. **Realm settings** (menú izquierdo)
3. Pestaña **Action** (botón arriba a la derecha) → **Export**
4. Activar **Export groups and roles** y **Export clients** → **Export**
5. Se descarga un archivo `realm-export.json` — incluirlo en el repositorio

---

## 5. Swagger UI

Documentación interactiva por servicio (acceso directo, no por Gateway):

| Servicio | URL |
|----------|-----|
| Usuarios | `http://localhost:8081/swagger-ui/index.html` |
| Catálogo | `http://localhost:8082/swagger-ui/index.html` |
| Circulación | `http://localhost:8083/swagger-ui/index.html` |
| Notificaciones | `http://localhost:8084/swagger-ui/index.html` |

---

## 6. Notas importantes

- Las bases de datos son **H2 en memoria**: los datos se borran al reiniciar cualquier servicio. Recrear los datos desde el Paso 1 si se reinicia algún servicio.
- El **orden de inicio** importa: Eureka primero, microservicios después, Gateway al final.
- Los **tokens JWT expiran en 5 minutos**: si aparece 401 inesperado, obtener un token nuevo.
