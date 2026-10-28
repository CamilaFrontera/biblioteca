-- TP N° 2 - Sistema de Gestión de Biblioteca
-- Script 3: datos ficticios de prueba.
-- Las fechas son relativas al día en que se ejecuta el script, así siempre hay
-- préstamos vigentes, vencidos y devueltos para probar.
--   psql -U postgres -d gestion_biblioteca -f sql/03_datos.sql

INSERT INTO socios (numero_socio, dni, nombre, apellido, activo) VALUES
    (1001, '30111222', 'Lucía',   'Fernández', TRUE),   -- id 1: sin préstamos
    (1002, '31222333', 'Martín',  'Gómez',     TRUE),   -- id 2: tiene un préstamo vencido
    (1003, '32333444', 'Sofía',   'Pereyra',   TRUE),   -- id 3: tiene 3 préstamos (límite)
    (1004, '33444555', 'Diego',   'Romero',    FALSE),  -- id 4: socio dado de baja
    (1005, '34555666', 'Valeria', 'Acosta',    TRUE),   -- id 5: un préstamo vigente
    (1006, '35666777', 'Tomás',   'Benítez',   TRUE);   -- id 6: solo préstamos devueltos

INSERT INTO libros (titulo, autor, editorial, anio_publicacion, activo) VALUES
    ('Rayuela',                         'Julio Cortázar',        'Sudamericana',     1963, TRUE),  -- 1
    ('Rayuela',                         'Julio Cortázar',        'Sudamericana',     1963, TRUE),  -- 2 (segundo ejemplar)
    ('Ficciones',                       'Jorge Luis Borges',     'Sur',              1944, TRUE),  -- 3
    ('El Aleph',                        'Jorge Luis Borges',     'Losada',           1949, TRUE),  -- 4
    ('Cien años de soledad',            'Gabriel García Márquez','Sudamericana',     1967, TRUE),  -- 5
    ('Ingeniería del software',         'Ian Sommerville',       'Pearson',          2011, TRUE),  -- 6
    ('Clean Code',                      'Robert C. Martin',      'Prentice Hall',    2008, TRUE),  -- 7
    ('Patrones de diseño',              'Erich Gamma',           'Addison-Wesley',   1994, TRUE),  -- 8
    ('El túnel',                        'Ernesto Sabato',        'Sur',              1948, TRUE),  -- 9
    ('Martín Fierro',                   'José Hernández',        'Eudeba',           1872, FALSE), -- 10 (dado de baja)
    ('Sobre héroes y tumbas',           'Ernesto Sabato',        'Sudamericana',     1961, TRUE),  -- 11
    ('Don Segundo Sombra',              'Ricardo Güiraldes',     'Losada',           1926, TRUE);  -- 12

INSERT INTO prestamos (socio_id, libro_id, fecha_prestamo, fecha_vencimiento, fecha_devolucion) VALUES
    -- Martín (2): préstamo vencido sin devolver
    (2, 3,  CURRENT_DATE - 12, CURRENT_DATE - 5,  NULL),
    -- Sofía (3): tres préstamos vigentes, alcanzó el límite
    (3, 1,  CURRENT_DATE - 2,  CURRENT_DATE + 5,  NULL),
    (3, 6,  CURRENT_DATE - 1,  CURRENT_DATE + 6,  NULL),
    (3, 9,  CURRENT_DATE,      CURRENT_DATE + 7,  NULL),
    -- Valeria (5): un préstamo vigente
    (5, 7,  CURRENT_DATE - 3,  CURRENT_DATE + 4,  NULL),
    -- Tomás (6): historial devuelto (uno en término y uno con atraso)
    (6, 5,  CURRENT_DATE - 30, CURRENT_DATE - 23, CURRENT_DATE - 25),
    (6, 8,  CURRENT_DATE - 20, CURRENT_DATE - 13, CURRENT_DATE - 10),
    -- Diego (4): préstamo devuelto antes de darse de baja
    (4, 12, CURRENT_DATE - 60, CURRENT_DATE - 53, CURRENT_DATE - 55);
