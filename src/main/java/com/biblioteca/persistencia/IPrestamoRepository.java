package com.biblioteca.persistencia;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import com.biblioteca.modelo.Prestamo;

/**
 * Contrato de acceso a datos de préstamos.
 */
public interface IPrestamoRepository {
    int guardar(Prestamo prestamo) throws SQLException;

    /** Guarda la devolución solo si todavía no tenía una. Devuelve false si ya estaba registrada. */
    boolean registrarDevolucion(int idPrestamo, LocalDate fecha) throws SQLException;

    Prestamo buscarPorId(int id) throws SQLException;
    List<Prestamo> listarPorSocio(int socioId) throws SQLException;
    List<Prestamo> listarSinDevolver() throws SQLException;
    int contarSinDevolverPorSocio(int socioId) throws SQLException;
    List<Prestamo> listarVencidosPorSocio(int socioId, LocalDate hoy) throws SQLException;
    boolean existeSinDevolverPorLibro(int libroId) throws SQLException;
}
