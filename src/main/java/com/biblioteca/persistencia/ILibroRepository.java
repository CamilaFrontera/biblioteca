package com.biblioteca.persistencia;

import java.sql.SQLException;
import java.util.List;

import com.biblioteca.modelo.Libro;

/**
 * Contrato de acceso a datos de libros (ejemplares).
 */
public interface ILibroRepository {
    int guardar(Libro libro) throws SQLException;
    void modificar(Libro libro) throws SQLException;
    Libro buscarPorId(int id) throws SQLException;
    List<Libro> buscarPorTitulo(String texto) throws SQLException;
    List<Libro> buscarPorAutor(String texto) throws SQLException;
    List<Libro> listar() throws SQLException;
}
