# Sabores del Litoral — Backend

API REST para la plataforma de delivery del proyecto integrador de Ingeniería de Software II.

## Tecnologías

- Java 21
- Spring Boot 4.1.1
- Spring Security + JWT
- Spring Data JPA
- H2 en archivo
- Maven Wrapper

## Requisitos y ejecución

Se necesita JDK 21. Maven está incluido mediante el wrapper.

```bash
# Linux, macOS o Git Bash
./mvnw spring-boot:run

# Windows
.\mvnw.cmd spring-boot:run
```

La API queda disponible en `http://localhost:8080`.

## Configuración

### Base de datos

La aplicación usa H2 en archivo en `./data/sabores_litoral`. Al iniciar ejecuta:

- `src/main/resources/schema.sql`: crea las tablas.
- `src/main/resources/data.sql`: carga categorías, repartidores y el administrador inicial.
- `src/main/resources/data-demo.sql`: agrega locales, productos, clientes, usuarios y pedidos de demostración.

Con la configuración actual, `spring.sql.init.mode=always` recrea la base al iniciar. Para conservar los datos entre reinicios se puede cambiar a `never`.

La consola H2 está disponible en `http://localhost:8080/h2-console`:

| Campo | Valor |
|---|---|
| JDBC URL | `jdbc:h2:file:./data/sabores_litoral` |
| Usuario | `sa` |
| Contraseña | vacía |

### Seguridad

El login valida la contraseña con BCrypt y devuelve un JWT de 8 horas. Las rutas protegidas esperan:

```http
Authorization: Bearer <token>
```

| Requisito | Acceso |
|---|---|
| Login, catálogo público, creación de pedido y seguimiento | Público |
| Pedidos, detalle, avance, cancelación, repartidores y en la calle | `ADMIN` u `OPERADOR` |
| Locales administrativos, productos, ventas y usuarios | `ADMIN` |

- Sin token o con token inválido: `401`.
- Con token de operador en una ruta de administrador: `403`.
- Un usuario inactivo no puede ingresar.
- RN-09 se valida en el backend: un administrador no puede desactivarse ni quitarse el rol.
- Las contraseñas nunca se devuelven en las respuestas.

El secreto se configura con `JWT_SECRET`. En desarrollo hay un valor por defecto en `application.properties`; en producción se debe definir un secreto propio de al menos 32 caracteres.

CORS está habilitado para la app web local. La app nativa de Expo Go no depende de CORS.

### Datos de demostración

La configuración local carga automáticamente `data-demo.sql`. Los tests no lo cargan, porque usan solo `data.sql`.

| Usuario | Contraseña | Rol |
| --- | --- | --- |
| `admin` | `Admin2026!` | `ADMIN` |
| `operador` | `Operador2026!` | `OPERADOR` |

Pedidos precargados:

| Código | Estado | Local |
| --- | --- | --- |
| `DEMO0001` | `ENTREGADO` | Sabores del Puerto |
| `DEMO0002` | `EN_CAMINO` | La Cocina de la Costa |
| `DEMO0003` | `RECIBIDO` | Sabores del Puerto |
| `DEMO0004` | `EN_PREPARACION` | Sabores del Puerto |

Para desactivar los datos de demostración, quitar `classpath:data-demo.sql` de `spring.sql.init.data-locations` en `application.properties`.

## Endpoints principales

| Área | Endpoints | Acceso |
| --- | --- | --- |
| Autenticación | `POST /api/auth/login` | Público |
| Catálogo | `GET /api/locales`, `GET /api/locales/{id}/carta`, `GET /api/categorias` | Público |
| Pedidos | `POST /api/pedidos`, `GET /api/pedidos/seguimiento/{codigo}` | Público |
| Pedidos | `GET /api/pedidos`, `GET /api/pedidos/{id}`, `PATCH /api/pedidos/{id}/avanzar`, `PATCH /api/pedidos/{id}/cancelar` | Personal |
| Repartidores | `GET /api/repartidores`, `GET /api/repartidores/en-calle` | Personal |
| Locales | `GET /api/locales/todos`, `POST/PUT/PATCH /api/locales` | Admin |
| Productos | `GET/POST/PUT/PATCH /api/productos` | Admin |
| Ventas | `GET /api/ventas?desde=YYYY-MM-DD&hasta=YYYY-MM-DD` | Admin |
| Usuarios | `GET/POST/PUT/PATCH /api/usuarios` | Admin |

## Tests

Los tests usan H2 en memoria y no modifican la base local.

```bash
# Linux, macOS o Git Bash
./mvnw test

# Windows
.\mvnw.cmd test
```

La cobertura incluye catálogo, locales, productos, pedidos, transiciones, cancelación, ventas, repartidores, usuarios, RN-09 y seguridad JWT (`401`, `403`, BCrypt y CORS).

## Estructura

```text
src/main/java/com/grupo1/saboresdellitoral/
├── config/        seguridad y configuración
├── controller/    endpoints REST
├── dto/           contratos de entrada y salida
├── entity/        modelo JPA
├── exception/     manejo centralizado de errores
├── repository/    acceso a datos
├── security/      JWT, filtro y respuestas 401/403
└── service/       lógica de negocio
```

## Alcance

No se incluyen cuentas de cliente, historial personal, pagos en línea, cupones, propinas, mapas, GPS ni tiempos estimados. Locales, productos y usuarios no se eliminan: se desactivan. Las categorías y los repartidores vienen precargados y solo se consultan.

La autenticación es stateless: no hay refresh token ni sesión del lado del servidor. Al expirar el token o reiniciar la app, el personal debe volver a ingresar.
