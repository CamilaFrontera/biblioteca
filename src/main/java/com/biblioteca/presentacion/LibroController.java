package com.biblioteca.presentacion;

import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

import com.biblioteca.modelo.Libro;
import com.biblioteca.servicio.LibroService;

/**
 * Opciones de consola de libros: búsqueda para todos los perfiles y
 * gestión para el administrador.
 */
public class LibroController {
    private final LibroService service;
    private final Consola consola;

    public LibroController(LibroService service, Consola consola) {
        this.service = Objects.requireNonNull(service);
        this.consola = Objects.requireNonNull(consola);
    }

    public void buscar() throws SQLException {
        System.out.println("\n--- Buscar libros ---");
        System.out.println("1. Por título");
        System.out.println("2. Por autor");
        int criterio = consola.leerEntero("Opción");
        if (criterio != 1 && criterio != 2) {
            Vista.error("Opción incorrecta.");
            return;
        }
        String texto = consola.leerTexto(criterio == 1 ? "Título o parte del título" : "Autor o parte del nombre");
        List<Libro> libros = criterio == 1 ? service.buscarPorTitulo(texto) : service.buscarPorAutor(texto);
        Vista.libros(libros);
    }

    public void registrar() throws SQLException {
        System.out.println("\n--- Registrar libro (ejemplar) ---");
        String titulo = consola.leerTexto("Título");
        String autor = consola.leerTexto("Autor");
        String editorial = consola.leerTexto("Editorial (opcional)");
        Integer anio = consola.leerAnio("Año de publicación", null);
        Libro libro = service.registrar(new Libro(titulo, autor, editorial, anio));
        Vista.mensaje("Libro registrado con ID " + libro.getId() + ".");
    }

    public void modificar() throws SQLException {
        System.out.println("\n--- Modificar libro ---");
        Libro libro = service.buscarPorId(consola.leerEntero("ID del libro"));
        Vista.libro(libro);
        System.out.println("Presione Enter para conservar el valor actual.");
        libro.setTitulo(consola.leerTextoOpcional("Título", libro.getTitulo()));
        libro.setAutor(consola.leerTextoOpcional("Autor", libro.getAutor()));
        libro.setEditorial(consola.leerTextoOpcional("Editorial", libro.getEditorial()));
        libro.setAnioPublicacion(consola.leerAnio("Año de publicación", libro.getAnioPublicacion()));
        service.modificar(libro);
        Vista.mensaje("Libro modificado.");
    }

    public void consultar() throws SQLException {
        System.out.println("\n--- Consultar libro ---");
        Vista.libro(service.buscarPorId(consola.leerEntero("ID del libro")));
    }

    public void listar() throws SQLException {
        System.out.println("\n--- Catálogo completo ---");
        Vista.libros(service.listar());
    }

    public void darDeBaja() throws SQLException {
        System.out.println("\n--- Dar de baja libro ---");
        int id = consola.leerEntero("ID del libro");
        Vista.libro(service.buscarPorId(id));
        if (consola.confirmar("¿Confirma la baja?")) {
            service.darDeBaja(id);
            Vista.mensaje("Libro dado de baja. Su historial se conserva.");
        } else {
            Vista.mensaje("Operación cancelada.");
        }
    }
}
