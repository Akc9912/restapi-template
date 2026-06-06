# RestAPI

[![Use this template](https://img.shields.io/badge/Use%20this%20template-2ea44f?style=flat-square)](https://github.com/Akc9912/restapi/generate)

Template base para construir APIs REST con Spring Boot, JWT, y arquitectura modular.

## Empezar un nuevo proyecto

Este repositorio es un template de GitHub. Para crear un nuevo proyecto:

```bash
# 1. Click en "Use this template" en GitHub -> genera tu propio repo

# 2. Clonar el repositorio generado
git clone <tu-repo-url> mi-proyecto
cd mi-proyecto

# 3. Inicializar el proyecto (renombra packages, directorios, y pom.xml)
./script/init-project.sh

# 4. Configurar variables de entorno
cp .env.example .env
# Editar .env con tus valores

# 5. Crear la base de datos y ejecutar
psql -U postgres -c "CREATE DATABASE mi-proyecto;"
psql -U postgres -d mi-proyecto -f script/database/00-init.sql

# 6. Ejecutar
./mvnw spring-boot:run
```

> `init-project.sh` reemplaza `com.project.restapi` por tu nuevo package, renombra los directorios, y actualiza `pom.xml` y `application.properties`.

## Stack

| Tecnología        | Versión                              |
| ----------------- | ------------------------------------ |
| Java              | 21                                   |
| Spring Boot       | 3.3.x (LTS)                          |
| Spring Security   | 6.x                                  |
| Spring Data JPA   | 3.x                                  |
| JWT (jjwt)        | 0.12.6                               |
| OpenAPI / Swagger | springdoc 3.0.2                      |
| Base de datos     | PostgreSQL (producción) / H2 (tests) |
| Build             | Maven 3.9+                           |
| Lombok            | Última                               |

## Módulos

### Auth (`/api/auth/v1`)

- `POST /register` — Registro de usuario
- `POST /login` — Inicio de sesión
- `POST /refresh` — Refrescar tokens
- `POST /logout` — Cerrar sesión
- `POST /verify-email` — Verificar email
- `POST /password-reset` — Solicitar reseteo de contraseña
- `POST /password-reset/confirm` — Confirmar reseteo de contraseña

### Users (`/api/users/v1`)

- `GET /me` — Usuario actual
- `GET /{id}` — Usuario por ID
- `GET /` — Listar usuarios
- `PATCH /{id}` — Actualizar usuario
- `DELETE /{id}` — Eliminar usuario (soft delete)
- `POST /{id}/change-password` — Cambiar contraseña

## Requisitos

- Java 21+
- PostgreSQL 15+
- Maven 3.9+ (o usar `./mvnw`)

## Quick Start (template por defecto)

Si quieres probar el template sin renombrar:

```bash
# 1. Clonar
git clone <repo-url> restapi
cd restapi

# 2. Configurar variables de entorno
cp .env.example .env
# Editar .env con tus valores

# 3. Crear la base de datos
psql -U postgres -c "CREATE DATABASE restapi;"
psql -U postgres -d restapi -f script/database/00-init.sql

# 4. Ejecutar
./mvnw spring-boot:run
```

La API estará disponible en `http://localhost:8080` y Swagger en `http://localhost:8080/swagger-ui.html`.

## Variables de Entorno

| Variable               | Default                                       | Descripción                   |
| ---------------------- | --------------------------------------------- | ----------------------------- |
| `DB_URL`               | `jdbc:postgresql://localhost:5432/restapi`    | URL de la base de datos       |
| `DB_USERNAME`          | `postgres`                                    | Usuario de base de datos      |
| `DB_PASSWORD`          | `postgres`                                    | Contraseña de base de datos   |
| `JWT_SECRET`           | (valor por defecto para desarrollo)           | Secreto para firmar JWT       |
| `JWT_EXPIRATION_HOURS` | `24`                                          | Horas de expiración del token |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:5173,http://localhost:3000` | Orígenes permitidos           |
| `SERVER_PORT`          | `8080`                                        | Puerto del servidor           |

## Tests

```bash
./mvnw test
```

## Estructura del Proyecto

```
src/
├── main/
│   ├── java/com/project/restapi/
│   │   ├── config/           # Configuraciones (CORS, OpenAPI)
│   │   ├── security/         # Seguridad JWT (provider, filter, config)
│   │   ├── Shared/           # Utilidades y excepciones compartidas
│   │   │   └── Exception/    # Manejador global + excepciones personalizadas
│   │   └── Modules/
│   │       ├── Auth/         # Módulo de autenticación
│   │       │   ├── api/      # DTOs + interface de la API
│   │       │   ├── controller/
│   │       │   ├── Service/
│   │       │   ├── Repository/
│   │       │   ├── Entity/
│   │       │   └── Enums/
│   │       └── Users/        # Módulo de usuarios
│   │           ├── api/      # DTOs + interface de la API
│   │           ├── controller/
│   │           ├── Service/
│   │           ├── Repository/
│   │           ├── Entity/
│   │           └── Enums/
│   └── resources/
│       ├── application.properties
│       └── logback-spring.xml
└── test/
    └── java/com/project/restapi/
        ├── Modules/
        │   ├── Auth/         # Tests de Auth
        │   └── Users/        # Tests de Users
        └── security/         # Tests de seguridad
```
