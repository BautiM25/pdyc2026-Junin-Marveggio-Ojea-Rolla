# Greater Events – Backend API

Proyecto backend para la gestión de eventos musicales y artistas, desarrollado como parte de la materia **Programación Distribuña y Concurrente (2026)**.

## Tecnologías utilizadas

- **Java 21** (también funciona con Java 17)
- **Spring Boot** 4.0.5
- **Spring Web** (REST API)
- **Spring Data JPA** (ORM)
- **PostgreSQL** (versión 13 o superior – testeado con v16)
- **Maven** (wrapper incluido)
- **Lombok** (reducción de código boilerplate)

## Estructura del proyecto

El código fuente se encuentra dentro de la carpeta `events/`. La estructura principal es:

```bash
pdyc2026-Junin-Marveggio-Ojea-Rolla/
│   .env                           # Variables de entorno (DB_NAME, DB_USERNAME, DB_PASSWORD)
│   .gitignore                     # Archivos ignorados por Git (incluye .env, target, etc.)
│   README.md                      # Documentación del proyecto
│
├───.github/
│   └───java-upgrade/              # Configuración específica de GitHub (hooks, scripts)
│       └───hooks/scripts/
│           ├───recordToolUse.ps1
│           └───recordToolUse.sh
│
└───events/                        # Módulo principal de la aplicación Spring Boot
    │   .gitattributes
    │   .gitignore                 # Ignora target/, .mvn/, etc.
    │   HELP.md                    # Ayuda de Spring Boot (generada automáticamente)
    │   mvnw                       # Maven wrapper para Unix
    │   mvnw.cmd                   # Maven wrapper para Windows
    │   pom.xml                    # Dependencias: Spring Web, JPA, PostgreSQL, Lombok
    │
    ├───.mvn/wrapper/
    │       maven-wrapper.properties
    │
    ├───src/
    │   ├───main/
    │   │   ├───java/ar/edu/unnoba/pdyc2026/events/
    │   │   │   │   EventsApplication.java       # Clase principal @SpringBootApplication
    │   │   │   │
    │   │   │   ├───controller/                  # Capa de controladores REST
    │   │   │   │       ArtistController.java    # Endpoints /admin/artists
    │   │   │   │       EventController.java     # Endpoints /admin/events
    │   │   │   │       GlobalExceptionHandler.java # Manejador global de excepciones
    │   │   │   │
    │   │   │   ├───dto/                         # Java Records para transferencia de datos
    │   │   │   │       AddArtistRequest.java    # { artist_id }
    │   │   │   │       ArtistResponse.java      # { id, name, genre, active }
    │   │   │   │       CreateArtistRequest.java # { name, genre }
    │   │   │   │       CreateEventRequest.java  # { name, start_date, description }
    │   │   │   │       EventDetail.java         # Detalle + lista de artistas
    │   │   │   │       EventSummary.java        # Resumen + cantidad de artistas
    │   │   │   │       RescheduleRequest.java   # { start_date }
    │   │   │   │       UpdateArtistRequest.java # { name, genre }
    │   │   │   │       UpdateEventRequest.java  # { name, start_date, description }
    │   │   │   │
    │   │   │   ├───model/                       # Entidades JPA y enums
    │   │   │   │       Artist.java              # id, name, genre, active, events
    │   │   │   │       Event.java               # id, name, description, startDate, status, artists
    │   │   │   │       EventStatus.java         # tentative, confirmed, rescheduled, cancelled
    │   │   │   │       Genre.java               # rock, techno, pop, jazz, folk
    │   │   │   │
    │   │   │   ├───repository/                  # Interfaces Spring Data JPA
    │   │   │   │       ArtistRepository.java    # findByGenre, findByIdAndActiveTrue
    │   │   │   │       EventRepository.java     # findByStatus
    │   │   │   │
    │   │   │   └───service/                     # Lógica de negocio con @Service
    │   │   │           ArtistService.java       # CRUD + reglas (desactivar si tiene eventos)
    │   │   │           EventService.java        # CRUD + reglas (estados, fechas)
    │   │   │
    │   │   └───resources/
    │   │       │   application.properties       # Configuración: datasource, JPA, ddl-auto
    │   │       ├───static/                      # Archivos estáticos (vacío)
    │   │       └───templates/                   # Plantillas (vacío, API REST pura)
    │   │
    │   └───test/                               # Tests unitarios (pendientes de implementar)
    │       └───java/.../EventsApplicationTests.java
    │
    └───target/                                 # Compilado (ignorado por Git)
        ├───classes/                            # .class generados
        ├───generated-sources/
        ├───generated-test-sources/
        └───test-classes/
```

> El archivo `.env` está excluido del repositorio mediante `.gitignore`. Cada desarrollador debe crearlo localmente.

## Requisitos previos

Antes de ejecutar el proyecto, asegurate de tener instalado:

- **JDK 21** (o 17)
- **PostgreSQL** (versión 13 a 16)
- **Git** (para clonar)

## Configuración local

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

Dentro de la carpeta `events/`, creá un archivo llamado `.env` con el siguiente contenido:

```bash
DB_NAME=pdyc2026
DB_USERNAME=tu_usuario_postgres
DB_PASSWORD=tu_contraseña
```

Ajustá los valores según tu instalación de PostgreSQL.

### 4. Ejecutar la aplicación

Desde la carpeta `events/` (donde está el `pom.xml`), ejecutá:

```bash
./mvnw spring-boot:run
```

La aplicación se levantará en **http://localhost:8080**. Todos los endpoints están bajo el prefijo `/admin/`.

## Endpoints disponibles

La API expone los siguientes recursos:

**Artists**
- `GET /admin/artists` (filtro por género opcional)
- `GET /admin/artists/{id}`
- `POST /admin/artists`
- `PUT /admin/artists/{id}`
- `DELETE /admin/artists/{id}`

**Events**
- `GET /admin/events` (filtro por estado opcional)
- `GET /admin/events/{id}`
- `POST /admin/events`
- `PUT /admin/events/{id}`
- `DELETE /admin/events/{id}`
- `POST /admin/events/{id}/artists`
- `DELETE /admin/events/{id}/artist/{artist_id}`
- `PUT /admin/events/{id}/confirmed`
- `PUT /admin/events/{id}/rescheduled`
- `PUT /admin/events/{id}/canceled`