# Greater Events – Backend API

Proyecto backend para la gestión de eventos musicales y artistas, desarrollado como parte de la materia **Programación Distribuña y Concurrente (2026)**.

## Estado actual

- **Práctica 2** completamente implementada: CRUD de artistas y eventos con reglas de negocio, base de datos PostgreSQL, capas Controller – Service – Repository.
- **Práctica 3** completamente implementada:
  - Autenticación y autorización mediante OAuth2 + JWT (Keycloak como Identity Provider).
  - Protección de todos los endpoints existentes (`/admin/artists/**` y `/admin/events/**`) mediante token JWT válido.
  - CRUD de **usuarios administradores** (obtener, crear, eliminar) usando `Keycloak Admin Client`.
  - Llamadas asíncronas a Keycloak envueltas en `CompletableFuture` para no bloquear el hilo principal.

## Tecnologías utilizadas

- **Java 21** (también funciona con Java 17)
- **Spring Boot** 4.0.5
- **Spring Web** (REST API)
- **Spring Data JPA** (ORM)
- **Spring Security** + **OAuth2 Resource Server** (JWT)
- **Keycloak Admin Client** (gestión de usuarios)
- **PostgreSQL** (versión 13 o superior – testeado con v16)
- **Maven** (wrapper incluido)
- **Lombok** (reducción de código boilerplate)
- **Docker** / **Docker Compose** (para ejecutar Keycloak)

## Estructura del proyecto

```bash
pdyc2026-Junin-Marveggio-Ojea-Rolla/
│   .env
│   .gitignore
│   README.md
├───docker/
│   └───docker-compose.yml
└───events/
    ├───pom.xml
    ├───src/main/java/.../events/
    │   ├───config/
    │   │   ├───SecurityConfig.java         # OAuth2 Resource Server JWT
    │   │   └───KeycloakAdminConfig.java    # Bean de Keycloak (client credentials)
    │   ├───controller/
    │   │   ├───ArtistController.java
    │   │   ├───EventController.java
    │   │   └───AdminUserController.java    # CRUD de usuarios
    │   ├───dto/                            # Records (Java 21)
    │   ├───model/                          # Entity + enums
    │   ├───repository/                     # Spring Data JPA
    │   └───service/
    │       ├───ArtistService.java
    │       ├───EventService.java
    │       └───AdminUserService.java       # CompletableFuture hacia Keycloak
    └───src/main/resources/
        └───application.properties
```

> El archivo `.env` está excluido del repositorio mediante `.gitignore`. Cada desarrollador debe crearlo localmente.

## Requisitos previos

Antes de ejecutar el proyecto, asegurate de tener instalado:

- **JDK 21** (o 17)
- **PostgreSQL** (versión 13 a 16)
- **Git** (para clonar)
- **Docker** y **Docker Compose** (para Keycloak)
- **Postman** (opcional, para pruebas)

## Configuración del entorno

### 1. Clonar el repositorio

```bash
git clone https://github.com/tu-usuario/pdyc2026-Junin-Marveggio-Ojea-Rolla.git
cd pdyc2026-Junin-Marveggio-Ojea-Rolla
```
### 2. Crear la base de datos en PostgreSQL

Accedé a psql y ejecutá:

```bash
CREATE DATABASE pdyc2026;
```

### 3. Configurar variables de entorno (.env)

En la **raíz del repositorio** (es decir, dentro de la carpeta `pdyc2026-Junin-Marveggio-Ojea-Rolla/`), creá un archivo llamado `.env` con el siguiente contenido:

```bash
DB_NAME=pdyc2026
DB_USERNAME=tu_usuario_postgres
DB_PASSWORD=tu_contraseña
```

Ajustá los valores según tu instalación de PostgreSQL.

### 4. Levantar Keycloak con Docker

Usá el archivo `docker/docker-compose.yml` provisto en la práctica:

```bash
cd docker
docker-compose up -d
```

Keycloak quedará disponible en **http://localhost:8080**.

### 5. Configurar Keycloak (realm, client, usuarios)

Accedé a la consola de administración:
**http://localhost:8080/admin/master/console/**
Usuario: admin / Contraseña: admin.

- Creá un nuevo Realm llamado unnoba.
- Creá un Client llamado pdyc; ajustá luego en `application.properties`.
  - En Settings:
    - Valid redirect URIs: **https://oauth.pstmn.io/v1/callback**
    - Client authentication: ON
    - Standard flow y Service account roles: activados.
  - En Credentials: generá un Client Secret.
  - En Service Account Roles: asigná el rol `manage-users` (filtrar por Clients).
- Creá un usuario (por ejemplo testuser) con contraseña (no temporal) para obtener tokens.

### 6. Configurar el backend (application.properties)
El archivo `events/src/main/resources/application.properties` ya contiene:

```bash
server.port=8081
spring.security.oauth2.resourceserver.jwt.issuer-uri=http://localhost:8080/realms/unnoba

# Base de datos (usa variables del .env)
spring.datasource.url=jdbc:postgresql://localhost:5432/${DB_NAME}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}

# Keycloak admin client
keycloak.server-url=http://localhost:8080
keycloak.realm=unnoba
keycloak.client-id=pdyc
keycloak.client-secret=EL_SECRET_GENERADO
```

Asegurate de reemplazar EL_SECRET_GENERADO por el Client Secret que obtuviste.

### 7. Ejecutar la aplicación

Desde la carpeta `events/` (donde está el `pom.xml`), ejecutá:

```bash
./mvnw spring-boot:run
```

La aplicación se levantará en **http://localhost:8081**. Todos los endpoints requieren autenticación mediante token JWT.

## Endpoints disponibles

La API expone los siguientes recursos:

**Artists**

| Método | Endpoint                     | Descripción                                  |
| ------ | ---------------------------- | -------------------------------------------- |
| GET    | /admin/artists?genre={genre} | Lista artistas (filtro opcional por género)  |
| GET    | /admin/artists/{id}          | Obtener artista por ID                       |
| POST   | /admin/artists               | Crear nuevo artista                          |
| PUT    | /admin/artists/{id}          | Actualizar artista                           |
| DELETE | /admin/artists/{id}          | Eliminar o desactivar artista                |

**Events**

| Método | Endpoint                              | Descripción                                             |
| ------ | ------------------------------------- | ------------------------------------------------------- |
| GET    | /admin/events?state={state}           | Lista eventos (filtro opcional por estado)              |
| GET    | /admin/events/{id}                    | Obtener evento con detalle + artistas                   |
| POST   | /admin/events                         | Crear evento (estado inicial tentative)                 |
| PUT    | /admin/events/{id}                    | Actualizar evento (solo si tentative)                   |
| DELETE | /admin/events/{id}                    | Eliminar evento (solo si tentative)                     |
| POST   | /admin/events/{id}/artists            | Agregar un artista al evento (solo si estado tentative) |
| DELETE | /admin/events/{id}/artist/{artist_id} | Quitar un artista del evento (solo si estado tentative) |
| PUT	 | /admin/events/{id}/confirmed	         | Confirmar evento (cambia a confirmed)                   |
| PUT	 | /admin/events/{id}/rescheduled        | Reprogramar evento (cambia a rescheduled)               |
| PUT	 | /admin/events/{id}/canceled	         | Cancelar evento (cambia a canceled)                     |

**Usuarios administradores (CRUD contra Keycloak)**

| Método | Endpoint	             | Descripción                          |
| ------ | --------------------- | ------------------------------------ |
| GET	 | /admin/users	         | Listar todos los usuarios del realm  |
| GET	 | /admin/users/{userId} | Obtener un usuario por ID            |
| POST	 | /admin/users          | Crear un nuevo usuario administrador |
| DELETE | /admin/users/{userId} | Eliminar un usuario                  |

## Cómo probar los endpoints con Postman

1. Configurar `OAuth2` en Postman (flujo Authorization Code):
  - Auth URL: **http://localhost:8080/realms/unnoba/protocol/openid-connect/auth**
  - Access Token URL: **http://localhost:8080/realms/unnoba/protocol/openid-connect/token**
  - Client ID: pdyc
  - Client Secret: el generado en Keycloak
  - Callback URL: **https://oauth.pstmn.io/v1/callback**
  - Scope: openid
2. Obtener un token: hacé clic en `Get New Access Token` y logueate con el usuario creado en Keycloak (ej. testuser).
3. Usar el token: en la pestaña `Authorization` de cada request, seleccioná `OAuth 2.0` y elegí el token obtenido. Postman lo enviará automáticamente en el header `Authorization: Bearer <token>`.
4. Probar cualquier endpoint

## Reglas de negocio

- Artista:
  - Si ha participado en algún evento, no se borra: se desactiva (active=false).
  - Desactivado no puede agregarse a nuevos eventos.
- Evento:
  - Estado tentative: permite modificar cualquier campo, agregar/quitar artistas, eliminar.
  - confirmed o rescheduled: solo permiten cancelar o reprogramar (fecha futura).
  - Solo se confirma si la fecha es futura.
  - Reprogramación solo a fecha futura.

## Solución de problemas

| Problema	                                 | Posible causa y solución                                                                          |
| ------------------------------------------ | ------------------------------------------------------------------------------------------------- |
| 401 Unauthorized	                         | Token inválido o expirado. Volver a obtener token en Postman.                                     |
| 500 Internal Server Error al crear usuario | Falta el rol manage-users en el service account del client. Asignarlo en Keycloak.                |
| Connection refused                         | Keycloak no está corriendo (Docker) o el backend no está en puerto 8081.                          |
| JwtDecoder error                           | Verificar que issuer-uri apunte correctamente al realm (**http://localhost:8080/realms/unnoba**). |