# Sabores del Litoral

Backend de la app de delivery del proyecto integrador de Ingeniería de Software II.

## Integrantes

- Sileoni Nicolás
- Piva Mariano
- Salas Gonzalo
- Alfonsín Laureano

## Tecnologías

- Java 21
- Spring Boot 4.1.1
- Spring Data JPA
- H2 en archivo
- Maven Wrapper

## Cómo levantarlo

Solo hace falta tener el JDK 21 instalado. No es necesario instalar Maven porque el proyecto incluye el wrapper.

En Linux, macOS o Git Bash:

```bash
./mvnw spring-boot:run
```

En Windows, desde PowerShell o CMD:

```bat
mvnw.cmd spring-boot:run
```

La aplicación queda disponible en `http://localhost:8080`.

## Base de datos

La base se crea en `./data/sabores_litoral.mv.db` a partir de los scripts `schema.sql` y `data.sql` que están en `src/main/resources`.

Como `spring.sql.init.mode` está en `always`, las tablas se recrean en cada arranque. Si necesitan conservar los datos entre reinicios, cambien ese valor a `never`.

La consola de H2 está en `http://localhost:8080/h2-console`:

- JDBC URL: `jdbc:h2:file:./data/sabores_litoral`
- Usuario: `sa`
- Contraseña: vacía

El script de datos carga el usuario inicial `admin` con la contraseña `Admin2026!`. Las contraseñas se guardan en texto plano porque el objetivo del trabajo es didáctico.

## Usuarios y login

El login valida usuario y contraseña y devuelve los datos del usuario. No se usan tokens, JWT ni Spring Security.

- `POST /api/auth/login`
- `GET /api/usuarios`
- `POST /api/usuarios`
- `PUT /api/usuarios/{id}`
- `PATCH /api/usuarios/{id}/password`
- `PATCH /api/usuarios/{id}/activo`

Todos los endpoints quedan abiertos; no se implementa autorización por roles.

## Estructura

```
src/main/java/com/grupo1/saboresdellitoral
├── config
├── controller
├── dto
├── entity
├── exception
├── repository
└── service
```

## Ramas

`main` se mantiene como rama estable. Los cambios se hacen en ramas aparte (`config-inicial`, `feat/...`, etc.) y se integran con un Pull Request.
