package com.biblioteca.persistencia;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.biblioteca.modelo.Libro;
import com.biblioteca.modelo.Prestamo;
import com.biblioteca.modelo.Socio;

public class PrestamoRepository implements IPrestamoRepository {

    /** Trae el préstamo junto con los datos del socio y del libro. */
    private static final String SELECT_PRESTAMO = """
            SELECT p.id AS p_id, p.fecha_prestamo, p.fecha_vencimiento, p.fecha_devolucion,
                   s.id AS s_id, s.numero_socio, s.dni, s.nombre, s.apellido, s.activo AS s_activo,
                   l.id AS l_id, l.titulo, l.autor, l.editorial, l.anio_publicacion, l.activo AS l_activo
            FROM prestamos p
            JOIN socios s ON s.id = p.socio_id
            JOIN libros l ON l.id = p.libro_id
            """;

    @Override
    public int guardar(Prestamo prestamo) throws SQLException {
        String sql = """
                INSERT INTO prestamos (socio_id, libro_id, fecha_prestamo, fecha_vencimiento)
                VALUES (?, ?, ?, ?)
                """;
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, prestamo.getSocio().getId());
            ps.setInt(2, prestamo.getLibro().getId());
            ps.setObject(3, prestamo.getFechaPrestamo());
            ps.setObject(4, prestamo.getFechaVencimiento());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                prestamo.setId(rs.getInt(1));
                return prestamo.getId();
            }
        }
    }

    @Override
    public boolean registrarDevolucion(int idPrestamo, LocalDate fecha) throws SQLException {
        // La condición "fecha_devolucion IS NULL" impide registrar dos veces (RN-08).
        String sql = "UPDATE prestamos SET fecha_devolucion = ? WHERE id = ? AND fecha_devolucion IS NULL";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setObject(1, fecha);
            ps.setInt(2, idPrestamo);
            return ps.executeUpdate() == 1;
        }
    }

    @Override
    public Prestamo buscarPorId(int id) throws SQLException {
        List<Prestamo> lista = consultar(SELECT_PRESTAMO + " WHERE p.id = ?", ps -> ps.setInt(1, id));
        return lista.isEmpty() ? null : lista.get(0);
    }

    @Override
    public List<Prestamo> listarPorSocio(int socioId) throws SQLException {
        return consultar(SELECT_PRESTAMO + " WHERE p.socio_id = ? ORDER BY p.fecha_prestamo DESC, p.id DESC",
                ps -> ps.setInt(1, socioId));
    }

    @Override
    public List<Prestamo> listarSinDevolver() throws SQLException {
        return consultar(SELECT_PRESTAMO + " WHERE p.fecha_devolucion IS NULL ORDER BY p.fecha_vencimiento, p.id",
                ps -> { });
    }

    @Override
    public int contarSinDevolverPorSocio(int socioId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM prestamos WHERE socio_id = ? AND fecha_devolucion IS NULL";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, socioId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    @Override
    public List<Prestamo> listarVencidosPorSocio(int socioId, LocalDate hoy) throws SQLException {
        return consultar(SELECT_PRESTAMO
                        + " WHERE p.socio_id = ? AND p.fecha_devolucion IS NULL AND p.fecha_vencimiento < ?"
                        + " ORDER BY p.fecha_vencimiento",
                ps -> {
                    ps.setInt(1, socioId);
                    ps.setObject(2, hoy);
                });
    }

    @Override
    public boolean existeSinDevolverPorLibro(int libroId) throws SQLException {
        String sql = "SELECT EXISTS (SELECT 1 FROM prestamos WHERE libro_id = ? AND fecha_devolucion IS NULL)";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, libroId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getBoolean(1);
            }
        }
    }

    private List<Prestamo> consultar(String sql, Parametros parametros) throws SQLException {
        List<Prestamo> prestamos = new ArrayList<>();
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            parametros.cargar(ps);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    prestamos.add(mapearPrestamo(rs));
                }
            }
        }
        return prestamos;
    }

    private Prestamo mapearPrestamo(ResultSet rs) throws SQLException {
        Socio s = new Socio();
        s.setId(rs.getInt("s_id"));
        s.setNumeroSocio(rs.getInt("numero_socio"));
        s.setDni(rs.getString("dni"));
        s.setNombre(rs.getString("nombre"));
        s.setApellido(rs.getString("apellido"));
        s.setActivo(rs.getBoolean("s_activo"));

        Libro l = new Libro();
        l.setId(rs.getInt("l_id"));
        l.setTitulo(rs.getString("titulo"));
        l.setAutor(rs.getString("autor"));
        l.setEditorial(rs.getString("editorial"));
        int anio = rs.getInt("anio_publicacion");
        l.setAnioPublicacion(rs.wasNull() ? null : anio);
        l.setActivo(rs.getBoolean("l_activo"));

        Prestamo p = new Prestamo();
        p.setId(rs.getInt("p_id"));
        p.setSocio(s);
        p.setLibro(l);
        p.setFechaPrestamo(rs.getObject("fecha_prestamo", LocalDate.class));
        p.setFechaVencimiento(rs.getObject("fecha_vencimiento", LocalDate.class));
        p.setFechaDevolucion(rs.getObject("fecha_devolucion", LocalDate.class));
        return p;
    }
}
