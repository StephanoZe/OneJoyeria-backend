# One Joyería — Backend (Spring Boot)

API REST del sistema de inventario de One Joyería. La consume el frontend Angular (`OneJoyeria_Frontend`) y se prueba con Postman o Thunder Client.

- **Puerto:** 8089 → `http://localhost:8089/api/...`
- **Base de datos:** MySQL 8 en Docker (puerto 3306, usuario `root`, contraseña `root`, base `onejoyeria`)
- **Login de prueba:** usuario `admin`, contraseña `admin`

## Cómo ejecutarlo

1. **Levanta MySQL en Docker** (dentro de esta carpeta):
   ```
   docker compose up -d
   ```
   Sin Docker Compose: `docker run -d --name onejoyeria-mysql -e MYSQL_ROOT_PASSWORD=root -e MYSQL_DATABASE=onejoyeria -p 3306:3306 mysql:8.4`
2. **Arranca el backend**, de una de estas formas:
   - Doble clic en `ejecutar.bat` (usa el Maven de la carpeta `apache-maven-3.10.0`, al lado de este proyecto).
   - En la terminal: `..\apache-maven-3.10.0\bin\mvn spring-boot:run` (o `mvn spring-boot:run` si tienes Maven instalado).
   - En VS Code: botón **Run** sobre `main` en `OneJoyeriaApplication.java`.
3. Cuando aparezca `Started OneJoyeriaApplication`, prueba `http://localhost:8089/api/productos`.

La primera vez Hibernate crea las tablas (`ddl-auto=update`) y se cargan datos de ejemplo: 5 tipos de joya, 5 materiales, 4 proveedores, 20 piezas y 20 movimientos.

**Sin Docker:** `ejecutar-sin-docker.bat` usa una base H2 guardada en la carpeta `data/` (consola en `http://localhost:8089/h2-console`).

## Piezas principales (para explicar el código)

| Archivo | Qué hace |
|---|---|
| `modelo/Producto.java` | Entidad JPA de la pieza. `estado()` calcula **Disponible**, **Stock bajo** o **Agotado** a partir del stock y del mínimo, como el `estadoAcademico` del ejemplo de alumnos. |
| `repositorio/ProductoRepositorio.java` | Extiende `JpaRepository<Producto, Long>`: CRUD sin escribir SQL, más consultas por nombre de método (`findByCodigoAndActivoTrue`). |
| `servicio/InventarioServicio.java` | Reglas de negocio: validaciones, entradas y salidas (no deja el stock en negativo), panel y reportes. |
| `controlador/ProductoControlador.java` | Endpoints REST con `ResponseEntity`: 200, 201 Created, 204 No Content, 400, 404 y 409. |
| `config/CorsConfig.java` | **CORS:** permite que Angular (`http://localhost:4200` o una URL `*.app.github.dev` de Codespaces) llame a la API. Los orígenes están en `app.cors.origenes`. |
| `excepcion/ManejadorErrores.java` | Convierte los errores en JSON `{ "mensaje": "..." }`. |
| `application.properties` | Conexión a MySQL, puerto 8089, `ddl-auto=update`, usuario admin y orígenes CORS. |
| `docker-compose.yml` | Contenedor de MySQL 8.4 con volumen para no perder los datos. |

## Endpoints

| Método | Ruta | Resultado |
|---|---|---|
| POST | `/api/auth/login` | 200 con el usuario, o 401 |
| GET | `/api/panel` | Datos del panel |
| GET | `/api/productos?busqueda=&categoriaId=&estado=` | Lista de piezas (200) |
| GET | `/api/productos/{codigo}` | Pieza (200) o 404 |
| POST | `/api/productos` | 201 Created (409 si el código ya existe) |
| PUT | `/api/productos/{codigo}` | 200 OK o 404 |
| DELETE | `/api/productos/{codigo}` | 204 No Content o 404 |
| GET / POST | `/api/productos/{codigo}/movimientos` | Historial / registrar entrada o salida (201) |
| GET | `/api/productos/alertas`, `/api/productos/resumen` | Alertas e indicadores |
| GET | `/api/movimientos?desde=&hasta=&categoriaId=` | Entradas y salidas del periodo |
| GET | `/api/categorias`, `/api/materiales` | Listas de apoyo |
| GET / POST | `/api/proveedores` | Lista / registrar (201) |
| GET / PUT / DELETE | `/api/proveedores/{id}` | Ficha, edición, baja (204) o 404 |
| GET | `/api/reportes/movimientos/excel`, `/api/reportes/inventario/excel` | Descarga de Excel |

## Pruebas con Postman o Thunder Client

Importa `postman/OneJoyeria_Java_API.postman_collection.json` (Thunder Client también acepta colecciones de Postman: *Collections > menú > Import*). La variable `baseUrl` ya apunta a `http://localhost:8089`. Con **Run collection** deben pasar 38 peticiones con 87 verificaciones; se puede repetir sin dañar los datos.

## En GitHub Codespaces

1. `docker compose up -d` y luego `mvn spring-boot:run` (o `../apache-maven-3.10.0/bin/mvn spring-boot:run`).
2. En la pestaña **Puertos**, haz público el puerto **8089** (clic derecho > *Port Visibility* > *Public*).
3. Usa esa URL (`https://…-8089.app.github.dev`) en Postman y en el `environment.ts` del frontend.
