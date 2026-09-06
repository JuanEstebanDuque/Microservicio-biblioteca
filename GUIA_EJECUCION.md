# Guía de Ejecución — Sistema de Gestión de Biblioteca Universitaria

## Requisitos previos

- **Java 17+** instalado
- **Maven 3.8+** instalado
- **Docker Desktop** corriendo
- **Postman** (para pruebas de API)

---

## 1. Levantar Keycloak (Docker)

Desde la raíz del proyecto (`ReposProfe/`):

```powershell
docker compose up -d
```

Verificar que esté corriendo:
```powershell
docker ps
```

Keycloak quedará disponible en: `http://localhost:8180`

---

## 2. Configurar Keycloak (solo la primera vez)

### 2.1 Acceder a la consola admin
- URL: `http://localhost:8180`
- Usuario: `admin` / Contraseña: `admin`

### 2.2 Crear el Realm
1. Menú desplegable arriba a la izquierda → **Create Realm**
2. Realm name: `biblioteca`
3. **Create**

### 2.3 Crear los Roles
Dentro del realm `biblioteca`:
1. **Realm roles** → **Create role**
2. Crear estos 3 roles:
   - `ESTUDIANTE`
   - `BIBLIOTECARIO`
   - `ADMIN`

### 2.4 Crear el Client
1. **Clients** → **Create client**
2. Client ID: `biblioteca-client`
3. Client type: `OpenID Connect` → **Next**
4. Client authentication: **OFF** (public client) → **Next**
5. Valid redirect URIs: `*` → **Save**

### 2.5 Crear usuarios de prueba

**Usuario estudiante:**
1. **Users** → **Create new user**
2. Username: `estudiante1` → **Create**
3. Pestaña **Credentials** → **Set password** → contraseña: `estudiante123` → Temporary: **OFF** → **Save**
4. Pestaña **Role mapping** → **Assign role** → seleccionar `ESTUDIANTE`

**Usuario bibliotecario:**
1. Username: `bibliotecario1` → contraseña: `biblio123` → rol: `BIBLIOTECARIO`

**Usuario admin:**
1. Username: `admin1` → contraseña: `admin123` → rol: `ADMIN`

---

## 3. Arrancar los microservicios (orden importante)

Abrir una terminal por cada servicio desde la raíz del proyecto:

```powershell
# Terminal 1 — Eureka Server (esperar hasta ver "Started EurekaServerApplication")
cd eureka-server
mvn spring-boot:run

# Terminal 2 — Microservicio Usuarios
cd microservicio-usuarios
mvn spring-boot:run

# Terminal 3 — Microservicio Catálogo
cd microservicio-catalogo
mvn spring-boot:run

# Terminal 4 — Microservicio Circulación
cd microservicio-circulacion
mvn spring-boot:run

# Terminal 5 — Microservicio Notificaciones
cd microservicio-notificacion
mvn spring-boot:run

# Terminal 6 — API Gateway (arrancar último)
cd api-gateway
mvn spring-boot:run
```

### Puertos asignados

| Servicio              | Puerto |
|-----------------------|--------|
| Eureka Server         | 8761   |
| API Gateway           | 8080   |
| Microservicio Usuarios | 8081  |
| Microservicio Catálogo | 8082  |
| Microservicio Circulación | 8083 |
| Microservicio Notificaciones | 8084 |
| Keycloak              | 8180   |

---

## 4. Verificar que todo está registrado en Eureka

Abrir: `http://localhost:8761`

Deben aparecer registrados:
- `USUARIOS-SERVICE`
- `CATALOGO-SERVICE`
- `CIRCULACION-SERVICE`
- `NOTIFICACION-SERVICE`
- `API-GATEWAY`

---

## 5. Obtener token JWT (Postman)

**POST** `http://localhost:8180/realms/biblioteca/protocol/openid-connect/token`

Body → `x-www-form-urlencoded`:

| Key           | Value             |
|---------------|-------------------|
| `grant_type`  | `password`        |
| `client_id`   | `biblioteca-client` |
| `username`    | `estudiante1`     |
| `password`    | `estudiante123`   |

Copiar el valor de `access_token` de la respuesta.

En todas las peticiones siguientes agregar el header:
```
Authorization: Bearer <access_token>
```

---

## 6. Pruebas funcionales

Todas las rutas pasan por el API Gateway en `http://localhost:8080`.

### 6.1 Usuarios

| Método | URL | Rol requerido |
|--------|-----|---------------|
| POST | `/usuarios` | Autenticado |
| GET | `/usuarios/{id}` | Autenticado |

**Crear usuario** (POST `/usuarios`):
```json
{
  "id": "u001",
  "nombre": "Juan Pérez",
  "email": "juan@universidad.edu",
  "direccion": "Calle 123",
  "credenciales": "clave123"
}
```

**Consultar usuario** (GET `/usuarios/u001`):
```
Authorization: Bearer <token>
```

### 6.2 Catálogo de libros

| Método | URL | Rol requerido |
|--------|-----|---------------|
| POST | `/libros` | Autenticado |
| GET | `/libros` | Autenticado |
| GET | `/libros/{id}` | Autenticado |
| PUT | `/libros/{id}/disponibilidad` | Autenticado |

**Registrar libro** (POST `/libros`):
```json
{
  "id": "lib001",
  "titulo": "Clean Code",
  "autor": "Robert C. Martin",
  "disponible": true
}
```

**Listar libros** (GET `/libros`):
```
Authorization: Bearer <token>
```

### 6.3 Circulación

| Método | URL | Rol requerido |
|--------|-----|---------------|
| POST | `/circulacion/prestar` | ESTUDIANTE, BIBLIOTECARIO, ADMIN |
| POST | `/circulacion/devolver` | ESTUDIANTE, BIBLIOTECARIO, ADMIN |
| GET | `/circulacion/prestamos` | BIBLIOTECARIO, ADMIN |

**Realizar préstamo** (POST `/circulacion/prestar?usuarioId=u001&libroId=lib001`):
```
Authorization: Bearer <token_estudiante>
```

**Ver todos los préstamos** (GET `/circulacion/prestamos`):
```
Authorization: Bearer <token_bibliotecario_o_admin>
```

---

## 7. Pruebas de seguridad

### Sin token → debe retornar 401
```
GET http://localhost:8080/libros
(sin Authorization header)
```

### Rol insuficiente → debe retornar 403
```
GET http://localhost:8080/circulacion/prestamos
Authorization: Bearer <token_estudiante>
```
Un ESTUDIANTE no puede ver todos los préstamos, solo BIBLIOTECARIO y ADMIN.

---

## 8. Swagger UI por servicio

Cada microservicio expone su documentación interactiva (acceder directamente, no por Gateway):

| Servicio | URL Swagger |
|----------|-------------|
| Usuarios | `http://localhost:8081/swagger-ui/index.html` |
| Catálogo | `http://localhost:8082/swagger-ui/index.html` |
| Circulación | `http://localhost:8083/swagger-ui/index.html` |
| Notificaciones | `http://localhost:8084/swagger-ui/index.html` |

---

## 9. Solución de problemas comunes

| Error | Causa | Solución |
|-------|-------|----------|
| `401 Unauthorized` | Token ausente o expirado | Obtener nuevo token en Keycloak |
| `403 Forbidden` | Rol insuficiente | Usar usuario con el rol correcto |
| `503 Service Unavailable` | Servicio no registrado en Eureka | Esperar 30s o reiniciar el servicio |
| `Connection refused` en Feign | Servicio destino caído | Verificar que todos los servicios estén arriba |
| `invalid_grant` en Keycloak | Credenciales incorrectas o usuario no existe | Verificar usuario en consola Keycloak |
