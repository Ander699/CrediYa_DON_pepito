# CrediYa S.A.S.

![Java](https://img.shields.io/badge/Java-11%2B-orange)
![Maven](https://img.shields.io/badge/Maven-3.x-blue)
![MySQL](https://img.shields.io/badge/MySQL-opcional-4479A1)
![Interfaz](https://img.shields.io/badge/interfaz-consola-lightgrey)

Aplicación de consola en **Java** para administrar una empresa de préstamos: empleados, clientes, préstamos, abonos y reportes de cartera. Los datos se guardan en **archivos de texto** y, si hay conexión, también en **MySQL**, de modo que el programa siempre funciona aunque la base de datos esté caída.

---

## Contenido

1. [Características](#características)
2. [Requisitos](#requisitos)
3. [Instalación y ejecución](#instalación-y-ejecución)
4. [Configuración de la base de datos](#configuración-de-la-base-de-datos)
5. [Uso del programa](#uso-del-programa)
6. [Validaciones de entrada](#validaciones-de-entrada)
7. [Reglas de negocio](#reglas-de-negocio)
8. [Arquitectura del proyecto](#arquitectura-del-proyecto)
9. [Persistencia de datos](#persistencia-de-datos)
10. [Solución de problemas](#solución-de-problemas)

---

## Características

- **Empleados:** registrar, listar y buscar por documento. Roles: Asesor, Cobrador y Administrador.
- **Clientes:** registrar, listar y consultar los préstamos de cada cliente.
- **Préstamos:** crear con monto, interés total y número de cuotas; cambiar el estado entre `PENDIENTE` y `PAGADO`.
- **Pagos:** registrar abonos, ver el historial y el saldo pendiente. El préstamo pasa a `PAGADO` solo cuando el saldo llega a cero.
- **Reportes:** préstamos activos y vencidos, clientes morosos, cartera pendiente total, deuda por cliente, préstamos por empleado y préstamos con monto mayor a un valor dado.
- **Validaciones completas** de cédula, celular, correo, nombres, roles y valores de dinero, con mensajes de error que explican qué corregir.
- **Doble persistencia** (archivos + MySQL) con respaldo automático: si MySQL no está disponible, trabaja solo con archivos.

## Requisitos

| Herramienta | Versión | Notas |
|---|---|---|
| JDK | 11 o superior | Probado con Java 21 |
| Maven | 3.x | Solo si ejecutas por terminal; los IDE ya lo incluyen |
| MySQL | 8.x | **Opcional**; sin él el programa usa solo archivos |

## Instalación y ejecución

### Estructura esperada

```
CrediYa/
├── pom.xml
├── config.properties        (opcional, ver más abajo)
├── datos/                   (se crea sola al ejecutar)
└── src/main/java/com/crediya/...
```

### Desde un IDE (IntelliJ, Eclipse, VS Code)

1. Abre la carpeta que contiene el `pom.xml` como **proyecto Maven**.
2. Espera a que se descarguen las dependencias (la primera vez tarda un momento).
3. Ejecuta la clase `com.crediya.Main`.

### Desde la terminal

Ubícate en la carpeta donde está el `pom.xml` y ejecuta:

```bash
mvn compile exec:java
```

> Ejecuta siempre desde la carpeta raíz del proyecto: ahí es donde el programa busca `config.properties` y donde crea la carpeta `datos/`.

## Configuración de la base de datos

La conexión se lee del archivo `config.properties`, ubicado en la raíz del proyecto:

```properties
db.url=jdbc:mysql://localhost:3306/crediya_db?serverTimezone=UTC
db.user=root
db.password=TU_CLAVE
```

Si el archivo no existe, se usan estos valores por defecto: base `crediya_db`, usuario `root` y clave vacía.

Al iniciar, el programa prueba la conexión y lo informa:

- `Conectado a MySQL (crediya_db) + archivos en /datos`: guarda en ambos lugares.
- `Modo solo archivos (/datos)`: MySQL no está disponible y se trabaja solo con archivos.

### Tablas que espera la base de datos

El programa usa estas cuatro tablas. Si todavía no las tienes, este script de **referencia** las crea con tamaños de columna que coinciden con las validaciones:

```sql
CREATE DATABASE IF NOT EXISTS crediya_db;
USE crediya_db;

CREATE TABLE clientes (
    id        INT AUTO_INCREMENT PRIMARY KEY,
    nombre    VARCHAR(60)  NOT NULL,
    documento VARCHAR(10)  NOT NULL UNIQUE,
    correo    VARCHAR(100) NOT NULL,
    telefono  VARCHAR(10)  NOT NULL
);

CREATE TABLE empleados (
    id        INT AUTO_INCREMENT PRIMARY KEY,
    nombre    VARCHAR(60)  NOT NULL,
    documento VARCHAR(10)  NOT NULL UNIQUE,
    rol       VARCHAR(20)  NOT NULL,
    correo    VARCHAR(100) NOT NULL,
    salario   DECIMAL(14,2) NOT NULL
);

CREATE TABLE prestamos (
    id           INT AUTO_INCREMENT PRIMARY KEY,
    cliente_id   INT NOT NULL,
    empleado_id  INT NOT NULL,
    monto        DECIMAL(14,2) NOT NULL,
    interes      DECIMAL(5,2)  NOT NULL,
    cuotas       INT NOT NULL,
    fecha_inicio DATE NOT NULL,
    estado       VARCHAR(10) NOT NULL,
    FOREIGN KEY (cliente_id)  REFERENCES clientes(id),
    FOREIGN KEY (empleado_id) REFERENCES empleados(id)
);

CREATE TABLE pagos (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    prestamo_id INT NOT NULL,
    fecha_pago  DATE NOT NULL,
    monto       DECIMAL(14,2) NOT NULL,
    FOREIGN KEY (prestamo_id) REFERENCES prestamos(id)
);
```

> Si ya tienes tus tablas creadas y funcionando, no necesitas este script.

## Uso del programa

Al iniciar aparece el menú principal:

```
===== CREDIYA S.A.S. =====
1. Empleados
2. Clientes
3. Prestamos
4. Pagos
5. Reportes
0. Salir
```

Consejos de uso:

- Si un dato es inválido, el programa **vuelve a pedir solo ese campo**, sin perder lo que ya escribiste.
- Escribe `cancelar` en cualquier campo para abortar la operación.
- Los montos aceptan varios formatos: `1500000`, `1.500.000`, `1,500,000` o `$ 1.500.000,50`.
- La cédula acepta puntos: `1.098.765.432`. La búsqueda por documento también.

### Ejemplo de registro de cliente

```
-- CLIENTES --
Nombre: Ana María Gómez
Documento (6 a 10 digitos): 1.098.765.432
Correo: Ana@Gmail.com
Celular (10 digitos, empieza por 3): +57 300 123 4567
Registrado: [1] Ana María Gómez | Doc: 1098765432 | ana@gmail.com | Tel: 3001234567
```

Observa que los datos se **normalizan** al guardarse: la cédula queda sin puntos, el correo en minúsculas y el celular sin prefijo ni espacios.

## Validaciones de entrada

Toda la lógica está centralizada en `util/Validador.java`. Cada validación comprueba el dato, lo limpia y devuelve un mensaje claro si algo está mal.

| Campo | Regla |
|---|---|
| **Nombre** | Entre 3 y 60 caracteres. Solo letras (con tildes y ñ), espacios, apóstrofe, punto y guion. |
| **Cédula / documento** | Entre 6 y 10 dígitos. Sin letras, sin cero inicial y sin todos los dígitos iguales. Acepta puntos, espacios y guiones. |
| **Celular** | Exactamente 10 dígitos y empieza por 3. Acepta `+57`, espacios, guiones y paréntesis. |
| **Correo** | Una sola `@`, dominio con punto, sin espacios ni puntos seguidos, máximo 100 caracteres. Se guarda en minúsculas. |
| **Rol** | Solo `Asesor`, `Cobrador` o `Administrador` (no distingue mayúsculas). |
| **Salario** | Entre $100.000 y $100.000.000. |
| **Monto del préstamo** | Entre $10.000 y $1.000.000.000. |
| **Interés total** | Entre 0 % y 100 %. |
| **Cuotas** | Entre 1 y 120. |
| **Abono** | Mayor que cero y no puede superar el saldo pendiente. |
| **Ids** | Números enteros mayores que cero. |

Además, el sistema impide registrar **documentos o correos repetidos** entre clientes y entre empleados, y rechaza valores no numéricos como `NaN` o `Infinity` en los campos de dinero.

> Todos los límites son constantes al inicio de `Validador.java` (por ejemplo `CEDULA_MAX`, `MONTO_MIN`, `INTERES_MAX`). Cámbialos allí y se aplican en todo el programa.

## Reglas de negocio

- **Monto total** = capital × (1 + interés / 100). El interés es **total sobre el capital**, no mensual.
- **Valor de cada cuota** = monto total ÷ número de cuotas.
- **Fecha de vencimiento** = fecha de inicio + número de cuotas (en meses).
- **Saldo pendiente** = monto total − suma de los abonos.
- Un préstamo está **vencido** si sigue `PENDIENTE` y la fecha actual es posterior a su vencimiento.
- Un **cliente moroso** es el que tiene al menos un préstamo vencido.
- Al registrar un abono que deja el saldo en cero, el préstamo pasa automáticamente a `PAGADO`.
- No se pueden registrar abonos sobre un préstamo ya pagado.

## Arquitectura del proyecto

```
src/main/java/com/crediya/
├── Main.java                  Menús y punto de entrada
├── model/                     Entidades del negocio
│   ├── Entidad.java             Interfaz con id (getId / setId)
│   ├── Persona.java             Clase abstracta base
│   ├── Cliente.java             Hereda de Persona (+ teléfono)
│   ├── Empleado.java            Hereda de Persona (+ rol y salario)
│   ├── Prestamo.java            Cálculos de total, cuota y vencimiento
│   ├── Pago.java
│   └── EstadoPrestamo.java      Enum: PENDIENTE, PAGADO
├── service/                   Reglas de negocio y validación
│   ├── ClienteService.java
│   ├── EmpleadoService.java
│   ├── PrestamoService.java
│   ├── PagoService.java
│   └── ReporteService.java      Reportes con lambdas y Stream API
├── repository/                Persistencia
│   ├── Repositorio.java         Interfaz genérica (guardar, actualizar, listar, buscar)
│   ├── ArchivoRepositorio.java  Base para los repos de archivo (*ArchivoRepo)
│   ├── JdbcRepositorio.java     Base para los repos MySQL (*JdbcRepo)
│   └── RepositorioDual.java     Combina archivo + MySQL
├── ui/
│   └── Consola.java             Lectura por consola con reintento
├── util/
│   ├── Validador.java           Validaciones y normalización
│   └── ConexionDB.java          Conexión a MySQL (Singleton)
└── exception/
    └── CrediYaException.java    Excepción propia del sistema
```

### Conceptos y patrones aplicados

- **Herencia y polimorfismo:** `Cliente` y `Empleado` extienden `Persona`; cada uno implementa su propio `resumen()`.
- **Interfaces y genéricos:** `Repositorio<T extends Entidad>` permite reutilizar la misma lógica de persistencia para todas las entidades.
- **Singleton:** `ConexionDB` mantiene una única configuración de conexión.
- **Facade / Composite:** `RepositorioDual` escribe en archivo y MySQL a la vez, y lee de MySQL con respaldo en archivo.
- **Capas separadas:** interfaz → servicios → repositorios → almacenamiento.
- **Excepciones propias:** `CrediYaException` unifica los errores de negocio y de persistencia.
- **Lambdas y Stream API** en los reportes.
- **PreparedStatement** en todas las consultas SQL (protege contra inyección SQL).

## Persistencia de datos

### Archivos de texto

Se guardan en la carpeta `datos/` (se crea automáticamente) con un registro por línea y los campos separados por `;`:

| Archivo | Formato de cada línea |
|---|---|
| `clientes.txt` | `id;nombre;documento;correo;telefono` |
| `empleados.txt` | `id;nombre;documento;rol;correo;salario` |
| `prestamos.txt` | un préstamo por línea |
| `pagos.txt` | un abono por línea |

### Comportamiento con MySQL

| Operación | Con MySQL disponible | Sin MySQL |
|---|---|---|
| Guardar / actualizar | Escribe en MySQL y en el archivo | Solo en el archivo |
| Listar | Lee de MySQL | Lee del archivo |
| Si MySQL falla a mitad de operación | Muestra un `[AVISO]` y continúa con el archivo | — |

## Solución de problemas

| Problema | Causa probable y solución |
|---|---|
| `[AVISO] MySQL no disponible (No suitable driver found...)` | No se cargó el driver. Recarga las dependencias de Maven en tu IDE. |
| `[AVISO] MySQL no disponible (Access denied...)` | Usuario o clave incorrectos. Revisa `config.properties`. |
| `[AVISO] No se encontro config.properties` | No es un error: se usan los valores por defecto. Créalo si tu MySQL tiene otra clave. |
| El IDE marca error en los `package` o `import` | Los paquetes deben ser `com.crediya...` y la carpeta `src/main/java/com/crediya`. |
| No encuentra la carpeta `datos/` o `config.properties` | Ejecuta el programa desde la carpeta donde está el `pom.xml`. |
| Registros viejos no se detectan como duplicados | Se guardaron con otro formato de cédula. Revisa los archivos de `datos/`. |

---

**CrediYa S.A.S.** — Proyecto de práctica en Java orientado a objetos, persistencia dual y validación de datos.
