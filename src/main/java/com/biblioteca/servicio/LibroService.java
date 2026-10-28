package com.biblioteca.servicio;

import java.sql.SQLException;
import java.time.Year;
import java.util.List;
import java.util.Objects;

import com.biblioteca.modelo.Libro;
import com.biblioteca.persistencia.ILibroRepository;
import com.biblioteca.persistencia.IPrestamoRepository;

/**
 * Reglas de negocio de libros (RF-03, RF-04, RN-09, RN-10).
 */
public class LibroService {
    private final ILibroRepository libros;
    private final IPrestamoRepository prestamos;
    private final PermisosService permisos;

    public LibroService(ILibroRepository libros, IPrestamoRepository prestamos, PermisosService permisos) {
        this.libros = Objects.requireNonNull(libros);
        this.prestamos = Objects.requireNonNull(prestamos);
        this.permisos = Objects.requireNonNull(permisos);
    }

    public Libro registrar(Libro libro) throws SQLException {
        permisos.exigirAdministrador();
        normalizar(libro);
        validar(libro);
        libro.setActivo(true);
        libros.guardar(libro);
        return libro;
    }

    public Libro modificar(Libro libro) throws SQLException {
        permisos.exigirAdministrador();
        Libro actual = libros.buscarPorId(libro.getId());
        if (actual == null) {
            throw new IllegalArgumentException("No existe el libro a modificar");
        }
        normalizar(libro);
        validar(libro);
        libro.setActivo(actual.isActivo()); // la baja se hace solo con darDeBaja
        libros.modificar(libro);
        return libro;
    }

    public Libro buscarPorId(int id) throws SQLException {
        Libro libro = libros.buscarPorId(id);
        if (libro == null) {
            throw new IllegalArgumentException("No existe un libro con el identificador " + id);
        }
        return libro;
    }

    /** RF-04: búsqueda por título, disponible para todos los perfiles. */
    public List<Libro> buscarPorTitulo(String texto) throws SQLException {
        return libros.buscarPorTitulo(textoDeBusqueda(texto));
    }

    /** RF-04: búsqueda por autor, disponible para todos los perfiles. */
    public List<Libro> buscarPorAutor(String texto) throws SQLException {
        return libros.buscarPorAutor(textoDeBusqueda(texto));
    }

    public List<Libro> listar() throws SQLException {
        permisos.exigirBibliotecarioOAdministrador();
        return libros.listar();
    }

    /** RN-09 y RN-10: baja lógica, bloqueada si el ejemplar está prestado. */
    public Libro darDeBaja(int id) throws SQLException {
        permisos.exigirAdministrador();
        Libro libro = buscarPorId(id);
        if (!libro.isActivo()) {
            throw new ReglaNegocioException("El libro ya está dado de baja");
        }
        if (prestamos.existeSinDevolverPorLibro(id)) {
            throw new ReglaNegocioException("No se puede dar de baja: el libro tiene un préstamo sin devolver");
        }
        libro.darDeBaja();
        libros.modificar(libro);
        return libro;
    }

    private String textoDeBusqueda(String texto) {
        if (texto == null || texto.isBlank()) {
            throw new IllegalArgumentException("Ingrese un texto para buscar");
        }
        return texto.trim();
    }

    private void normalizar(Libro libro) {
        libro.setTitulo(libro.getTitulo() == null ? null : libro.getTitulo().trim());
        libro.setAutor(libro.getAutor() == null ? null : libro.getAutor().trim());
        if (libro.getEditorial() != null) {
            String editorial = libro.getEditorial().trim();
            libro.setEditorial(editorial.isEmpty() ? null : editorial);
        }
    }

    private void validar(Libro libro) {
        if (libro.getTitulo() == null || libro.getTitulo().isBlank()) {
            throw new IllegalArgumentException("El título es obligatorio");
        }
        if (libro.getAutor() == null || libro.getAutor().isBlank()) {
            throw new IllegalArgumentException("El autor es obligatorio");
        }
        if (libro.getTitulo().length() > 150 || libro.getAutor().length() > 100) {
            throw new IllegalArgumentException("El título admite 150 caracteres y el autor 100");
        }
        Integer anio = libro.getAnioPublicacion();
        if (anio != null && (anio < 1450 || anio > Year.now().getValue())) {
            throw new IllegalArgumentException("Año de publicación inválido");
        }
    }
}
