# Sistema de Gestión de Biblioteca

TP N° 2 – Ingeniería de Software – Prof. Ariel Reynoso

Integrantes: Jessica Soria, Claudia Lara, Fatima Gauna, Valentin Zunino, Camila Frontera

Aplicación de consola en Java para gestionar socios, libros (ejemplares) y préstamos de una biblioteca. Usa Maven, arquitectura en capas y PostgreSQL.

## Requisitos

- Java 17 o superior (lo probamos con Java 21)
- Maven 3.8 o superior
- PostgreSQL 14 o superior (lo probamos con la versión 16)

## Configuración de la base de datos

Los scripts están en la carpeta `sql/` y se ejecutan en este orden:

```bash
psql -U postgres -f sql/01_crear_base.sql
psql -U postgres -d gestion_biblioteca -f sql/02_tablas.sql
psql -U postgres -d gestion_biblioteca -f sql/03_datos.sql
```

1. `01_crear_base.sql` crea la base `gestion_biblioteca` (si existía, la borra).
2. `02_tablas.sql` crea las tablas `socios`, `libros` y `prestamos` con sus restricciones.
3. `03_datos.sql` carga datos ficticios. Las fechas de los préstamos se calculan desde el día en que se ejecuta el script, así siempre hay préstamos vigentes, vencidos y devueltos para probar.

También se pueden ejecutar desde pgAdmin con la herramienta Query Tool: el primero conectado a la base `postgres` y los otros dos conectados a `gestion_biblioteca`.

## Conexión

Por defecto la aplicación se conecta a:

| Dato | Valor por defecto | Variable de entorno |
| --- | --- | --- |
| URL | `jdbc:postgresql://localhost:5432/gestion_biblioteca` | `DB_URL` |
| Usuario | `postgres` | `DB_USER` |
| Contraseña | `postgres` | `DB_PASSWORD` |

Si su usuario o contraseña son otros, definan las variables antes de ejecutar:

```bash
# Linux / Mac
export DB_PASSWORD=mi_clave

# Windows (PowerShell)
$env:DB_PASSWORD="mi_clave"
```

## Compilar y ejecutar

```bash
mvn clean package
java -jar target/biblioteca.jar
```

O directamente con Maven:

```bash
mvn compile exec:java
```

`mvn package` también corre las pruebas unitarias de la capa de negocio (`src/test`), que no necesitan la base de datos.

En Windows, si los acentos se ven mal en la consola, ejecutar antes `chcp 65001`.

## Perfiles y operaciones

Al iniciar se elige el perfil desde el menú inicial. Para ingresar como socio hay que indicar un número de socio existente.

| Perfil | Operaciones |
| --- | --- |
| Socio | Buscar libros por título o autor y ver su disponibilidad · Consultar sus propios préstamos |
| Bibliotecario | Registrar préstamo · Registrar devolución · Buscar libros · Consultar préstamos de un socio · Listar préstamos vigentes y vencidos · Registrar, modificar, consultar, listar y dar de baja socios |
| Administrador | Registrar, modificar, consultar, buscar, listar y dar de baja libros · Listar socios · Consultar préstamos de un socio · Listar préstamos vigentes y vencidos |

Al registrar un préstamo o una devolución se muestra el comprobante en la consola.

## Datos de prueba

| N° socio | Socio | Situación para probar |
| --- | --- | --- |
| 1001 | Lucía Fernández | Sin préstamos: puede pedir |
| 1002 | Martín Gómez | Tiene un préstamo vencido (RN-06) |
| 1003 | Sofía Pereyra | Tiene 3 préstamos sin devolver (RN-03) |
| 1004 | Diego Romero | Socio dado de baja (RN-01) |
| 1005 | Valeria Acosta | Un préstamo vigente |
| 1006 | Tomás Benítez | Solo préstamos devueltos (historial) |

Libros: hay 12 ejemplares. Los ID 1 y 2 son dos ejemplares de *Rayuela*; el ID 10 (*Martín Fierro*) está dado de baja; los ID 1, 3, 6, 7 y 9 están prestados al cargar los datos.

## Reglas de negocio

Las reglas RN-01 a RN-12 del enunciado se validan en la capa de servicio (`com.biblioteca.servicio`). Además, la base tiene un índice único que impide que un mismo ejemplar tenga dos préstamos sin devolver, y la devolución se actualiza con la condición `fecha_devolucion IS NULL` para que no se pueda registrar dos veces.

## Estructura del proyecto

```
biblioteca/
├── pom.xml
├── README.md
├── sql/
│   ├── 01_crear_base.sql
│   ├── 02_tablas.sql
│   └── 03_datos.sql
└── src/
    ├── main/java/com/biblioteca/
    │   ├── modelo/         Socio, Libro, Prestamo, Perfil
    │   ├── presentacion/   Main, Consola, Vista, SocioController, LibroController, PrestamoController
    │   ├── servicio/       SocioService, LibroService, PrestamoService, PermisosService, LoginService, ReglaNegocioException
    │   └── persistencia/   ConexionBD, interfaces I...Repository y sus implementaciones
    └── test/java/com/biblioteca/servicio/
        ├── ReglasDeNegocioTest.java
        └── RepositoriosEnMemoria.java
```
