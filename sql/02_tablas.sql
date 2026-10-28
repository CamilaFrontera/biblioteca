-- TP N° 2 - Sistema de Gestión de Biblioteca
-- Script 2: tablas, restricciones e índices.
-- Ejecutar sobre la base creada:
--   psql -U postgres -d gestion_biblioteca -f sql/02_tablas.sql

DROP TABLE IF EXISTS prestamos;
DROP TABLE IF EXISTS libros;
DROP TABLE IF EXISTS socios;

-- Socios. RN-11: DNI y número de socio únicos. RN-10: baja lógica (activo).
CREATE TABLE socios (
    id            INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    numero_socio  INTEGER      NOT NULL UNIQUE CHECK (numero_socio > 0),
    dni           VARCHAR(8)   NOT NULL UNIQUE CHECK (dni ~ '^[0-9]{7,8}$'),
    nombre        VARCHAR(60)  NOT NULL,
    apellido      VARCHAR(60)  NOT NULL,
    activo        BOOLEAN      NOT NULL DEFAULT TRUE
);

-- Libros. Cada fila es un ejemplar físico. RN-10: baja lógica (activo).
CREATE TABLE libros (
    id                INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    titulo            VARCHAR(150) NOT NULL,
    autor             VARCHAR(100) NOT NULL,
    editorial         VARCHAR(100),
    anio_publicacion  INTEGER CHECK (anio_publicacion BETWEEN 1450 AND 2100),
    activo            BOOLEAN      NOT NULL DEFAULT TRUE
);

-- Préstamos. RN-04: un socio y un único ejemplar.
-- RN-05: vencimiento a 7 días. RN-08: la devolución se registra una sola vez.
CREATE TABLE prestamos (
    id                 INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    socio_id           INTEGER NOT NULL REFERENCES socios(id),
    libro_id           INTEGER NOT NULL REFERENCES libros(id),
    fecha_prestamo     DATE    NOT NULL DEFAULT CURRENT_DATE,
    fecha_vencimiento  DATE    NOT NULL,
    fecha_devolucion   DATE,
    CHECK (fecha_vencimiento = fecha_prestamo + 7),
    CHECK (fecha_devolucion IS NULL OR fecha_devolucion >= fecha_prestamo)
);

-- RN-02 a nivel de base: un ejemplar no puede tener dos préstamos sin devolver.
CREATE UNIQUE INDEX ux_prestamo_libro_sin_devolver
    ON prestamos (libro_id) WHERE fecha_devolucion IS NULL;

CREATE INDEX ix_prestamos_socio ON prestamos (socio_id);
CREATE INDEX ix_libros_titulo ON libros (LOWER(titulo));
CREATE INDEX ix_libros_autor ON libros (LOWER(autor));
