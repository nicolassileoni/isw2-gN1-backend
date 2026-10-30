# Sabores del Litoral

Backend REST para la plataforma de delivery del proyecto integrador de Ingeniería de Software II.

## Requisitos y ejecución

Se necesita JDK 21. Maven está incluido mediante el wrapper.

```bash
# Linux, macOS o Git Bash
./mvnw spring-boot:run

# Windows
.\mvnw.cmd spring-boot:run
```

La API queda disponible en `http://localhost:8080`.

## Base de datos y H2

La aplicación usa H2 en archivo en `./data/sabores_litoral`. Al iniciar ejecuta:

- `src/main/resources/schema.sql`: crea las tablas.
- `src/main/resources/data.sql`: carga categorías, repartidores y el administrador inicial.
- `src/main/resources/data-demo.sql`: agrega locales, productos, clientes y pedidos de demostración.

Con la configuración actual, `spring.sql.init.mode=always` recrea los datos al iniciar. Para conservarlos entre reinicios se puede cambiar a `never`.

### Datos de demostración

La configuración local carga automáticamente `data-demo.sql`. Los tests no lo cargan, porque usan solo `data.sql`.

Usuarios disponibles:

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

La consola H2 está en `http://localhost:8080/h2-console`:

- JDBC URL: `jdbc:h2:file:./data/sabores_litoral`
- Usuario: `sa`
- Contraseña: vacía

## Endpoints principales

| Área | Endpoints |
| --- | --- |
| Catálogo | `GET /api/locales`, `GET /api/locales/{id}/carta`, `GET /api/categorias`, `GET /api/repartidores` |
| Locales y productos | `GET /api/locales/todos`, `POST/PUT/PATCH /api/locales`, `GET/POST/PUT/PATCH /api/productos` |
| Pedidos | `POST /api/pedidos`, `GET /api/pedidos`, `GET /api/pedidos/{id}`, `GET /api/pedidos/seguimiento/{codigo}` |
| Operación | `PATCH /api/pedidos/{id}/avanzar`, `PATCH /api/pedidos/{id}/cancelar`, `GET /api/repartidores/en-calle` |
| Reportes | `GET /api/ventas?desde=YYYY-MM-DD&hasta=YYYY-MM-DD` |
| Usuarios | `POST /api/auth/login`, `GET/POST/PUT/PATCH /api/usuarios` |

## Tests

Los tests usan H2 en memoria y no modifican la base de datos local.

```bash
# Linux, macOS o Git Bash
./mvnw test

# Windows
.\mvnw.cmd test
```

## Estado de autenticación

El login actual valida credenciales, pero la autenticación con token y las restricciones por rol todavía están pendientes. Por ahora, los endpoints administrativos y de operación permanecen abiertos.

## Cobertura funcional

| Requisito | Rama y pruebas principales |
| --- | --- |
| RF-03, RN-10 | `feat/locales` · `LocalesTests` |
| RF-04, RN-06 | `feat/productos` · `ProductosTests` |
| RF-05 | `feat/catalogo` · `CatalogoTests` |
| RF-06, RN-01/02/07/08 | `feat/pedidos-creacion` · `PedidoCreacionTests` |
| RF-07, RF-08 | `feat/pedidos-consulta` · `PedidoConsultaTests` |
| RF-09, RN-03/05 | `feat/pedidos-transiciones` · `PedidoTransicionTests` |
| RF-10, RN-04 | `feat/pedidos-cancelacion` · `PedidoCancelacionTests` |
| RF-11 | `feat/ventas` · `VentasTests` |
| RF-12 | `feat/repartidores-calle` · `RepartidorCalleTests` |
