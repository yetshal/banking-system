# Banking System API

API REST para la gestión básica de un sistema bancario: clientes, cuentas y transacciones (consignación, retiro y transferencias). Construida con **Spring Boot 3.2**, **Java 17**, **Maven**, **Spring Data JPA** y **MySQL**.

## Prueba técnica (contexto)

Este proyecto fue desarrollado siguiendo el documento **“Prueba Técnica”** (ver PDF). En resumen, se solicita una aplicación backend en Java para:

- Administrar **clientes** (CRUD) con validaciones (mayoría de edad, formatos, timestamps automáticos).
- Administrar **productos** (cuentas: ahorro/corriente) vinculados a un cliente, con numeración automática y estados.
- Realizar **transacciones** (consignación, retiro, transferencia) actualizando saldos.
- Persistencia en BD SQL (MySQL) y arquitectura por capas (MVC / capas).
- **Tests unitarios** para capas **Service** y **Controller**.

### Checklist de requerimientos (PDF) y evidencia

- **Backend en Java**: Spring Boot 3.2 + Java 17.
- **Arquitectura por capas (entity/service/controller/repository)**:
  - `src/main/java/com/banking/system/entity`
  - `src/main/java/com/banking/system/repository`
  - `src/main/java/com/banking/system/service`
  - `src/main/java/com/banking/system/controller`
- **CRUD Clientes**:
  - Endpoints: `POST/GET/GET{id}/PUT/DELETE` en `/api/clientes`
  - Validaciones: mayoría de edad, email, longitud mínima (ver entidad `Cliente`)
  - Timestamps automáticos: `@CreationTimestamp` y `@UpdateTimestamp`
  - Restricción de eliminación si tiene productos: validación en `ClienteService.eliminarCliente`
- **Productos (Cuentas) ahorro/corriente**:
  - Endpoints CRUD/gestión en `/api/cuentas`
  - Vinculación obligatoria a cliente: creación requiere `clienteId`
  - Número único y automático (10 dígitos): prefijos **53** (ahorro) y **33** (corriente) + consecutivo
  - Estados: `ACTIVA`, `INACTIVA`, `CANCELADA` (PATCH estado + cancelar)
  - Regla: ahorro no queda en negativo; cancelar solo con saldo 0
  - Timestamps automáticos: `@CreationTimestamp` y `@UpdateTimestamp`
- **Transacciones**:
  - Endpoints en `/api/transacciones` para consignación, retiro y transferencia
  - Actualización de saldo en cada transacción exitosa
  - Transferencia: genera movimientos de **débito** (envío) y **crédito** (recepción) como 2 registros
- **Consulta de “estado de cuenta”**:
  - Historial por cuenta: `GET /api/transacciones/cuenta/{cuentaId}`
  - Consulta de cuenta: `GET /api/cuentas/{id}` / `GET /api/cuentas/numero/{numeroCuenta}`
- **Tests unitarios (Service y Controller)**:
  - Service: `src/test/java/com/banking/system/service/*ServiceTest.java`
  - Controller (MockMvc): `src/test/java/com/banking/system/controller/*ControllerTest.java`
- **Git**:
  - Repositorio versionado con Git (commits/push como evidencia de avance).

## Funcionalidades

- **Clientes**
  - Crear, listar, consultar, actualizar y eliminar clientes.
  - Reglas de negocio:
    - El cliente debe ser **mayor de edad** (≥ 18).
    - `numeroIdentificacion` y `correoElectronico` deben ser **únicos**.
    - No se puede eliminar un cliente si tiene **cuentas vinculadas**.
- **Cuentas**
  - Crear cuenta para un cliente, listar y consultar por ID o por número.
  - Consultar cuentas por cliente.
  - Actualizar estado, cancelar y eliminar (con validaciones).
  - Reglas de negocio:
    - El saldo no puede ser negativo.
    - Cuentas de ahorro no pueden quedar en saldo negativo.
    - Cancelación solo cuando el **saldo es 0**.
    - Eliminación solo si la cuenta está **CANCELADA** y con saldo **0**.
  - Numeración automática:
    - Prefijo **33** para `CUENTA_CORRIENTE`
    - Prefijo **53** para `CUENTA_AHORRO`
    - Total: **10 dígitos** (prefijo + 8 dígitos consecutivos).
- **Transacciones**
  - Consignación, retiro y transferencia entre cuentas.
  - Historial de transacciones por cuenta y consulta general.
  - Reglas de negocio:
    - El monto debe ser **> 0**.
    - La(s) cuenta(s) deben estar **ACTIVAS**.
    - La transferencia genera **2 transacciones**: `TRANSFERENCIA_ENVIADA` y `TRANSFERENCIA_RECIBIDA`.

## Stack tecnológico

- **Java** 17
- **Spring Boot** 3.2.0
- **Spring Web** (API REST)
- **Spring Data JPA** (persistencia)
- **Bean Validation** (validaciones con `jakarta.validation`)
- **MySQL** (runtime)
- **H2** (solo para tests)
- **Springdoc OpenAPI** (Swagger UI)
- **JUnit 5 / Mockito** (tests)

## Requisitos

- **JDK 17**
- **Maven** (opcional si usas Maven Wrapper incluido)
- **MySQL** (recomendado 8+)

## Configuración

La app usa `src/main/resources/application.properties` con estas claves:

- `spring.datasource.url` (por defecto apunta a `jdbc:mysql://localhost:3306/banking_system`)
- `spring.datasource.username`
- `spring.datasource.password`
- `server.port` (por defecto `8080`)

### Base de datos

1) Crea la base de datos:

```sql
CREATE DATABASE banking_system;
```

2) Ajusta usuario/contraseña en `application.properties`.

> Importante: en este repo hay credenciales en texto plano dentro de `application.properties`. Para un entorno real, se recomienda usar variables de entorno y/o un archivo local no versionado.

## Cómo ejecutar

### Con Maven Wrapper (recomendado)

El repo incluye **Maven Wrapper**, así que no necesitas `mvn` instalado.

Windows (PowerShell / CMD):

```bash
.\mvnw.cmd spring-boot:run
```

Linux/macOS:

```bash
./mvnw spring-boot:run
```

### Con Maven

```bash
mvn spring-boot:run
```

La app quedará disponible en:
- API: `http://localhost:8080`

### Empaquetar JAR

```bash
mvn clean package
java -jar target/banking-system-1.0.jar
```

Con Maven Wrapper (Windows):

```bash
.\mvnw.cmd clean package
java -jar target/banking-system-1.0.jar
```

## Documentación Swagger (OpenAPI)

Con `springdoc-openapi`, la UI suele estar en:
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

## Endpoints (resumen)

### Clientes (`/api/clientes`)

- `POST /api/clientes` Crear cliente
- `GET /api/clientes` Listar clientes
- `GET /api/clientes/{id}` Obtener por ID
- `PUT /api/clientes/{id}` Actualizar
- `DELETE /api/clientes/{id}` Eliminar (solo si no tiene cuentas)

### Cuentas (`/api/cuentas`)

- `POST /api/cuentas?clienteId={clienteId}` Crear cuenta para un cliente
- `GET /api/cuentas` Listar cuentas
- `GET /api/cuentas/{id}` Obtener por ID
- `GET /api/cuentas/numero/{numeroCuenta}` Obtener por número
- `GET /api/cuentas/cliente/{clienteId}` Listar cuentas de un cliente
- `PATCH /api/cuentas/{id}/estado?estado=ACTIVA|INACTIVA|CANCELADA` Cambiar estado
- `POST /api/cuentas/{id}/cancelar` Cancelar (saldo debe ser 0)
- `DELETE /api/cuentas/{id}` Eliminar (estado CANCELADA y saldo 0)

### Transacciones (`/api/transacciones`)

- `POST /api/transacciones/consignacion` Consignación
- `POST /api/transacciones/retiro` Retiro
- `POST /api/transacciones/transferencia` Transferencia
- `GET /api/transacciones` Listar transacciones
- `GET /api/transacciones/{id}` Obtener por ID
- `GET /api/transacciones/cuenta/{cuentaId}` Historial por cuenta (origen o destino)

## Ejemplos rápidos (Postman)

Este repo incluye una colección lista para importar en Postman:

- Colección: `postman/BankingSystem.postman_collection.json`

### Importar y ejecutar

1) Abre Postman → **Import** → selecciona `postman/BankingSystem.postman_collection.json`.
2) En la colección, ajusta las variables si lo necesitas:
   - `baseUrl` (por defecto `http://localhost:8080`)
   - `clienteId`, `cuentaId`, `cuentaOrigenId`, `cuentaDestinoId`
3) Ejecuta las requests de la carpeta **Quick Examples**:
   - Crear cliente
   - Crear cuenta (por `clienteId`)
   - Consignación
   - Retiro
   - Transferencia

## Ejemplos rápidos (curl) (opcional)

### 1) Crear un cliente

```bash
curl -X POST "http://localhost:8080/api/clientes" ^
  -H "Content-Type: application/json" ^
  -d "{\"tipoIdentificacion\":\"CC\",\"numeroIdentificacion\":\"123456789\",\"nombres\":\"Juan\",\"apellido\":\"Pérez\",\"correoElectronico\":\"juan.perez@example.com\",\"fechaNacimiento\":\"1990-01-15\"}"
```

### 2) Crear una cuenta para el cliente (clienteId=1)

`tipoCuenta` debe ser `CUENTA_CORRIENTE` o `CUENTA_AHORRO`.

```bash
curl -X POST "http://localhost:8080/api/cuentas?clienteId=1" ^
  -H "Content-Type: application/json" ^
  -d "{\"tipoCuenta\":\"CUENTA_AHORRO\",\"saldo\":1000.00,\"exentaGMF\":false}"
```

### 3) Consignación

```bash
curl -X POST "http://localhost:8080/api/transacciones/consignacion" ^
  -H "Content-Type: application/json" ^
  -d "{\"cuentaId\":1,\"monto\":250.00,\"descripcion\":\"Depósito inicial\"}"
```

### 4) Retiro

```bash
curl -X POST "http://localhost:8080/api/transacciones/retiro" ^
  -H "Content-Type: application/json" ^
  -d "{\"cuentaId\":1,\"monto\":100.00,\"descripcion\":\"Retiro cajero\"}"
```

### 5) Transferencia

```bash
curl -X POST "http://localhost:8080/api/transacciones/transferencia" ^
  -H "Content-Type: application/json" ^
  -d "{\"cuentaOrigenId\":1,\"cuentaDestinoId\":2,\"monto\":50.00,\"descripcion\":\"Pago\"}"
```

## Respuestas de error (nota)

- Varias validaciones de negocio se devuelven como `400 Bad Request` con un body tipo:

```json
{ "error": "Mensaje de error" }
```

- Las validaciones de `@Valid` (Bean Validation) pueden devolver una estructura estándar de Spring para errores de validación.

## Estructura del proyecto

```
src/main/java/com/banking/system
  controller/   # Controladores REST + DTOs de request
  service/      # Lógica de negocio
  repository/   # Acceso a datos (Spring Data JPA)
  entity/       # Entidades JPA (Cliente, Cuenta, Transaccion)
  BankingSystem.java
src/main/resources
  application.properties
src/test/java/com/banking/system/service
  *ServiceTest.java
```

## Tests

```bash
mvn test
```

Con Maven Wrapper (Windows):

```bash
.\mvnw.cmd test
```

### Unit tests de controllers (MockMvc)

Se agregaron tests unitarios para controllers con `@WebMvcTest` + `MockMvc` (mockeando la capa `service`):

- `src/test/java/com/banking/system/controller/ClienteControllerTest.java`
- `src/test/java/com/banking/system/controller/CuentaControllerTest.java`
- `src/test/java/com/banking/system/controller/TransaccionControllerTest.java`

> Nota (entornos con JDK muy reciente): si tus tests corren con un JDK más nuevo que el soportado oficialmente por Byte Buddy, el `pom.xml` ya incluye el flag `net.bytebuddy.experimental=true` para permitir la instrumentación en tests.

## Sobre mí

**Julián Montero** — Desarrollador de software en formación (Neiva, Huila - Colombia).

- **Contacto**: `julianalfonsomonterotrujillo@gmail.com`
- **Telefono**: `+57 3142021695`