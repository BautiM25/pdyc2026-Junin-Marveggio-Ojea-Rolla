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
events/
├── src/main/java/ar/edu/unnoba/pdyc2026/events/
│ ├── EventsApplication.java # Clase principal
│ ├── model/ # Entidades JPA
│ ├── service/ # Lógica de negocio (@Service)
│ ├── controller/ # Controladores REST (@RestController)
│ └── repository/ # Interfaces de repositorio
├── src/main/resources/
│ └── application.properties # Configuración (lee variables de entorno)
├── .env # Variables de entorno (NO subir a Git)
└── pom.xml # Dependencias Maven
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