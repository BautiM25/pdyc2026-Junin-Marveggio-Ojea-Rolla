# Greater Events – Backend API

Proyecto backend para la gestión de eventos musicales y artistas, desarrollado como parte de la materia **Programación Distribuña y Concurrente (2026)**.

## Estado actual

- **Práctica 2** completamente implementada: CRUD de artistas y eventos con reglas de negocio, base de datos PostgreSQL, capas Controller – Service – Repository.
- **Práctica 3** completamente implementada:
  - Autenticación y autorización mediante OAuth2 + JWT (Keycloak como Identity Provider).
  - Protección de todos los endpoints existentes (`/admin/artists/**` y `/admin/events/**`) mediante token JWT válido.
  - CRUD de **usuarios administradores** (obtener, crear, eliminar) usando `Keycloak Admin Client`.
  - Llamadas asíncronas a Keycloak envueltas en `CompletableFuture` para no bloquear el hilo principal.
- **Práctica 4** completamente implementada:
  - Gestión de usuarios finales: entidad `User` con relaciones a `artistas` (following) y `eventos` (favoritos).
  - Registro de usuarios (`POST /auth/register`) que crea el usuario en Keycloak y en la base de datos local.
  - Endpoints para usuarios autenticados (`/me/**`):
    - Seguir / dejar de seguir artistas (`POST /me/following`, `DELETE /me/following/{artistId}`)
    - Listar artistas seguidos (`GET /me/following`)
    - Marcar / desmarcar eventos favoritos (`POST /me/favorite-events`, `DELETE /me/favorite-events/{eventId}`)
    - Listar eventos favoritos vigentes (`GET /me/favorite-events`)
    - Listar próximos eventos de artistas seguidos (`GET /me/following/events`)
  - Notificaciones: se generan automáticamente cuando un evento cambia a `confirmed`, `rescheduled` o `canceled` para los usuarios que siguen a algún artista participante o tienen el evento como favorito.
    - Endpoints: `GET /me/notifications`, `PATCH /me/notifications/{id}`, `DELETE /me/notifications/{id}`
  - Endpoints públicos:
    - `GET /artists` – listado de artistas activos
    - `GET /artists/{artistId}/events` – próximos eventos confirmados o reprogramados de un artista
    - `GET /events` – eventos vigentes y próximos (confirmados o reprogramados)
    - `GET /events/{eventId}` – detalles de un evento (excluye estados tentative)
  - Roles y autorización:
    - Rol `admin` en Keycloak para usuarios administradores.
    - `AuthenticationConverter` personalizado para extraer roles del JWT y mapearlos a GrantedAuthority.
    - `SecurityFilterChain` configurado con políticas diferenciadas:
      - Endpoints públicos (sin token)
      - Endpoints de usuario autenticado (`/me/**`)
      - Endpoints de administrador (`/admin/**`) – requieren rol `admin`
  - Refresh Token: flujo implementado con Keycloak (configuración en Postman) para renovar el access token sin reintroducir credenciales.


## Tecnologías utilizadas

- **Java 21** (también funciona con Java 17)
- **Spring Boot** 4.0.5
- **Spring Web** (REST API)
- **Spring Data JPA** (ORM)
- **Spring Security** + **OAuth2 Resource Server** (JWT)
- **Keycloak Admin Client** (gestión de usuarios y roles)
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
│   
├───.github
│   ├───java-upgrade
│   │   │   .gitignore
│   │   │   
│   │   └───hooks
│   │       └───scripts
│   │               recordToolUse.ps1
│   │               recordToolUse.sh
│   │               
│   └───modernize
│       └───java-upgrade
│           │   .gitignore
│           │   
│           └───hooks
│               └───scripts
│                       recordToolUse.ps1
│                       recordToolUse.sh
│                       
├───.vscode
│       settings.json
│       
├───docker
│       docker-compose.yml
│       
└───events
    │   .gitattributes
    │   .gitignore
    │   HELP.md
    │   mvnw
    │   mvnw.cmd
    │   pom.xml
    │   
    ├───.mvn
    │   └───wrapper
    │           maven-wrapper.properties
    │           
    ├───src
    │   ├───main
    │   │   ├───java
    │   │   │   └───ar
    │   │   │       └───edu
    │   │   │           └───unnoba
    │   │   │               └───pdyc2026
    │   │   │                   └───events
    │   │   │                       │   EventsApplication.java
    │   │   │                       │   
    │   │   │                       ├───config
    │   │   │                       │       KeycloakAdminConfig.java
    │   │   │                       │       SecurityConfig.java
    │   │   │                       │       
    │   │   │                       ├───controller
    │   │   │                       │       AdminUserController.java
    │   │   │                       │       ArtistController.java
    │   │   │                       │       AuthController.java
    │   │   │                       │       EventController.java
    │   │   │                       │       GlobalExceptionHandler.java
    │   │   │                       │       PublicArtistController.java
    │   │   │                       │       PublicEventController.java
    │   │   │                       │       UserActionController.java
    │   │   │                       │       
    │   │   │                       ├───dto
    │   │   │                       │       AddArtistRequest.java
    │   │   │                       │       ArtistResponse.java
    │   │   │                       │       CreateAdminUserRequest.java
    │   │   │                       │       CreateArtistRequest.java
    │   │   │                       │       CreateEventRequest.java
    │   │   │                       │       EventDetail.java
    │   │   │                       │       EventSummary.java
    │   │   │                       │       FavoriteEventRequest.java
    │   │   │                       │       FollowArtistRequest.java
    │   │   │                       │       NotificationResponse.java
    │   │   │                       │       RegisterRequest.java
    │   │   │                       │       RescheduleRequest.java
    │   │   │                       │       UpdateArtistRequest.java
    │   │   │                       │       UpdateEventRequest.java
    │   │   │                       │       UpdateNotificationRequest.java
    │   │   │                       │       
    │   │   │                       ├───model
    │   │   │                       │       Artist.java
    │   │   │                       │       Event.java
    │   │   │                       │       EventStatus.java
    │   │   │                       │       Genre.java
    │   │   │                       │       Notification.java
    │   │   │                       │       User.java
    │   │   │                       │       
    │   │   │                       ├───repository
    │   │   │                       │       ArtistRepository.java
    │   │   │                       │       EventRepository.java
    │   │   │                       │       NotificationRepository.java
    │   │   │                       │       UserRepository.java
    │   │   │                       │       
    │   │   │                       └───service
    │   │   │                               AdminUserService.java
    │   │   │                               ArtistService.java
    │   │   │                               AuthService.java
    │   │   │                               EventService.java
    │   │   │                               NotificationService.java
    │   │   │                               UserActionService.java
    │   │   │                               
    │   │   └───resources
    │   │       │   application.properties
    │   │       │   
    │   │       ├───static
    │   │       └───templates
    │   └───test
    │       └───java
    │           └───ar
    │               └───edu
    │                   └───unnoba
    │                       └───pdyc2026
    │                           └───events
    │                                   EventsApplicationTests.java
    │                                   
    └───target
        ├───classes
        │   │   application.properties
        │   │   
        │   └───ar
        │       └───edu
        │           └───unnoba
        │               └───pdyc2026
        │                   └───events
        │                       │   EventsApplication.class
        │                       │   
        │                       ├───config
        │                       │       KeycloakAdminConfig.class
        │                       │       SecurityConfig.class
        │                       │       
        │                       ├───controller
        │                       │       AdminUserController.class
        │                       │       ArtistController.class
        │                       │       AuthController.class
        │                       │       EventController.class
        │                       │       GlobalExceptionHandler.class
        │                       │       PublicArtistController.class
        │                       │       PublicEventController.class
        │                       │       UserActionController.class
        │                       │       
        │                       ├───dto
        │                       │       AddArtistRequest.class
        │                       │       ArtistResponse.class
        │                       │       CreateAdminUserRequest.class
        │                       │       CreateArtistRequest.class
        │                       │       CreateEventRequest.class
        │                       │       EventDetail.class
        │                       │       EventSummary.class
        │                       │       FavoriteEventRequest.class
        │                       │       FollowArtistRequest.class
        │                       │       NotificationResponse.class
        │                       │       RegisterRequest.class
        │                       │       RescheduleRequest.class
        │                       │       UpdateArtistRequest.class
        │                       │       UpdateEventRequest.class
        │                       │       UpdateNotificationRequest.class
        │                       │       
        │                       ├───model
        │                       │       Artist.class
        │                       │       Event.class
        │                       │       EventStatus.class
        │                       │       Genre.class
        │                       │       Notification.class
        │                       │       User.class
        │                       │       
        │                       ├───repository
        │                       │       ArtistRepository.class
        │                       │       EventRepository.class
        │                       │       NotificationRepository.class
        │                       │       UserRepository.class
        │                       │       
        │                       └───service
        │                               AdminUserService.class
        │                               ArtistService.class
        │                               AuthService.class
        │                               EventService.class
        │                               NotificationService.class
        │                               UserActionService.class
        │                               
        ├───generated-sources
        │   └───annotations
        ├───generated-test-sources
        │   └───test-annotations
        └───test-classes
            └───ar
                └───edu
                    └───unnoba
                        └───pdyc2026
                            └───events
                                    EventsApplicationTests.class
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

spring.config.import=optional:file:.env[.properties]

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

**Endpoints públicos (no requieren token)**

| Método | Endpoint | Descripción |
| ------ | -------- | ----------- |
| GET    | /artists | Lista de artistas activos (opcional filtro por género) |
| GET    | /artists/{artistId}/events | Próximos eventos (confirmados o reprogramados) de un artista específico |
| GET    | /events | Lista de eventos vigentes y próximos (confirmados o reprogramados) |
| GET    | /events/{eventId} | Detalle de un evento (solo si no está en estado tentative) |
| POST   | /auth/register | Registro de nuevo usuario (crea en Keycloak y en BD local) |

**Endpoints para usuarios autenticados (/me/*)**

Todos los endpoints bajo `/me` requieren un token JWT válido (cualquier usuario autenticado).

| Método | Endpoint | Descripción |
| ------ | -------- | ----------- |
| POST   | /me/following | Seguir a un artista (body: { "artistId": <id> }) |
| DELETE | /me/following/{artistId} | Dejar de seguir a un artista |
| GET    | /me/following | Listar los artistas que sigo |
| GET    | /me/following/events | Próximos eventos de artistas que sigo (ordenados por fecha) |
| POST   | /me/favorite-events | Marcar un evento como favorito (body: { "eventId": <id> }) |
| DELETE | /me/favorite-events/{eventId} | Quitar un evento de favoritos |
| GET    | /me/favorite-events | Listar mis eventos favoritos que aún están vigentes |
| GET	   | /me/notifications | Obtener todas mis notificaciones |
| PATCH	 | /me/notifications/{id} | Marcar una notificación como leída (body: { "is_read": true }) |
| DELETE | 	/me/notifications/{id} | Eliminar una notificación (solo si pertenece al usuario autenticado) |

**Endpoints de administración (/admin/*)**

Requerirán token JWT y rol `admin`.

| Método | Endpoint | Descripción |
| ------ | -------- | ----------- |
| GET	   | /admin/artists | Lista de artistas (filtro opcional por género) |
| GET	   | /admin/artists/{id} | Obtener artista por ID |
| POST	 | /admin/artists | Crear nuevo artista |
| PUT    | /admin/artists/{id} | Actualizar artista |
| DELETE | /admin/artists/{id} | Eliminar o desactivar artista |
| GET    | /admin/events | Lista de eventos (filtro opcional por estado) |
| GET    | /admin/events/{id} | Obtener evento con detalle + artistas |
| POST   | /admin/events | Crear evento (estado inicial tentative) |
| PUT    | /admin/events/{id} | Actualizar evento (solo si tentative) |
| DELETE | /admin/events/{id} | Eliminar evento (solo si tentative) |
| POST   | /admin/events/{id}/artists | Agregar artista al evento (solo si tentative) |
| DELETE | /admin/events/{id}/artist/{artistId} | Quitar artista del evento (solo si tentative) |
| PUT    | /admin/events/{id}/confirmed | Confirmar evento (cambia a confirmed) |
| PUT    | /admin/events/{id}/rescheduled | Reprogramar evento (cambia a rescheduled) |
| PUT    | /admin/events/{id}/canceled | Cancelar evento (cambia a canceled) |
| GET    | /admin/users | Listar todos los usuarios administradores (de Keycloak) |
| GET    | /admin/users/{userId} | Obtener un administrador por ID |
| POST   | /admin/users | Crear un nuevo administrador (asigna rol admin en Keycloak) |
|DELETE  | /admin/users/{userId} | Eliminar un administrador |

## Cómo probar los endpoints con Postman

**Obtener token (Authorization Code Flow)**

1. Crear una nueva request en Postman.
2. En la pestaña `Authorization`, seleccioná `OAuth 2.0`.
3. En Configure New Token:
  - Auth URL: **http://localhost:8080/realms/unnoba/protocol/openid-connect/auth**
  - Access Token URL: **http://localhost:8080/realms/unnoba/protocol/openid-connect/token**
  - Client ID: pdyc
  - Client Secret: el generado en Keycloak
  - Callback URL: **https://oauth.pstmn.io/v1/callback**
  - Scope: openid
  - Grant Type: Authorization Code
  - Client Authentication: Send client credentials in body
4. Hacé clic en `Get New Access Token` y logueate con el usuario creado en Keycloak (ej. testuser).
5. Postman obtendrá un access token y un refresh token (este último se almacena automáticamente).

**Usar el Refresh Token (para renovar automáticamente)**

En la misma configuración de `OAuth 2.0`, marcá la opción `Auto-refresh token` (si está disponible) o simplemente, cuando el access token expire, volvé a hacer clic en Get New Access Token; Postman utilizará el refresh token para obtener uno nuevo sin pedir credenciales.

**Probar endpoints**

- Seleccioná el token obtenido en el dropdown de la pestaña Authorization.
- Realizá las requests a los endpoints deseados.

## Reglas de negocio

- **Artista:**
  - Un artista puede ser eliminado físicamente solo si no ha participado en ningún evento.
  - Si ha participado en algún evento, no se borra: se desactiva (active=false).
  - Desactivado no puede agregarse a nuevos eventos.
- **Evento:**
  - Estado tentative: permite modificar cualquier campo, agregar/quitar artistas, eliminar.
  - confirmed o rescheduled: solo permiten cancelar o reprogramar (fecha futura).
  - Solo se confirma si la fecha es futura.
  - Reprogramación solo a fecha futura.
- **Usuarios finales:**
  - Un usuario puede seguir a varios artistas y marcar varios eventos como favoritos.
  - Al cambiar un evento a confirmed, rescheduled o canceled, se generan notificaciones para:
    - Todos los usuarios que siguen a algún artista que participa en el evento.
    - Todos los usuarios que tienen el evento en su lista de favoritos.
  - Las notificaciones incluyen el estado anterior y el nuevo, y se almacenan con fecha de creación y flag isRead.
- **Notificaciones**
  - Los endpoints de notificaciones verifican que la notificación pertenezca al usuario autenticado (`me`) para evitar accesos indebidos.


## Solución de problemas

| Problema                                   | Posible causa y solución |
| ------------------------------------------ | ------------------------ |
| 401 Unauthorized                           | Token inválido o expirado. Volver a obtener token en Postman. |
| 403 Forbidden en /admin/**                 | El usuario no tiene rol admin. Asignar el rol en Keycloak. |
| 500 Internal Server Error al crear usuario | Falta el rol manage-users en el service account del client. Asignarlo en Keycloak. |
| Connection refused                         | Keycloak no está corriendo (Docker) o el backend no está en puerto 8081. |
| JwtDecoder error                           | Verificar que issuer-uri apunte correctamente al realm (**http://localhost:8080/realms/unnoba**). |
| No se generan notificaciones               | Asegurar que el evento tenga artistas asignados y que existan usuarios siguiéndolos o con favoritos. |
| Registro de usuario falla                  | Verificar que el cliente pdyc tenga configurado Service account roles y el rol manage-users. |