package com.biblioteca.persistencia;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import com.biblioteca.modelo.Libro;

public class LibroRepository implements ILibroRepository {

    /**
     * La columna "prestado" se calcula: es verdadera si el ejemplar tiene un
     * préstamo sin devolver. Así la disponibilidad siempre es coherente con
     * los préstamos.
     */
    private static final String SELECT_LIBRO = """
            SELECT l.*,
                   EXISTS (SELECT 1 FROM prestamos p
                           WHERE p.libro_id = l.id AND p.fecha_devolucion IS NULL) AS prestado
            FROM libros l
            """;

    @Override
    public int guardar(Libro libro) throws SQLException {
        String sql = """
                INSERT INTO libros (titulo, autor, editorial, anio_publicacion, activo)
                VALUES (?, ?, ?, ?, ?)
                """;
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            cargarDatos(ps, libro);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                libro.setId(rs.getInt(1));
                return libro.getId();
            }
        }
    }

    @Override
    public void modificar(Libro libro) throws SQLException {
        String sql = """
                UPDATE libros
                SET titulo = ?, autor = ?, editorial = ?, anio_publicacion = ?, activo = ?
                WHERE id = ?
                """;
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            cargarDatos(ps, libro);
            ps.setInt(6, libro.getId());
            ps.executeUpdate();
        }
    }

    @Override
    public Libro buscarPorId(int id) throws SQLException {
        List<Libro> libros = consultar(SELECT_LIBRO + " WHERE l.id = ?", ps -> ps.setInt(1, id));
        return libros.isEmpty() ? null : libros.get(0);
    }

    @Override
    public List<Libro> buscarPorTitulo(String texto) throws SQLException {
        return consultar(SELECT_LIBRO + " WHERE l.activo AND LOWER(l.titulo) LIKE ? ORDER BY l.titulo, l.id",
                ps -> ps.setString(1, "%" + texto.toLowerCase() + "%"));
    }

    @Override
    public List<Libro> buscarPorAutor(String texto) throws SQLException {
        return consultar(SELECT_LIBRO + " WHERE l.activo AND LOWER(l.autor) LIKE ? ORDER BY l.autor, l.titulo, l.id",
                ps -> ps.setString(1, "%" + texto.toLowerCase() + "%"));
    }

    @Override
    public List<Libro> listar() throws SQLException {
        return consultar(SELECT_LIBRO + " ORDER BY l.titulo, l.id", ps -> { });
    }

    private List<Libro> consultar(String sql, Parametros parametros) throws SQLException {
        List<Libro> libros = new ArrayList<>();
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            parametros.cargar(ps);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Libro libro = mapearLibro(rs);
                    libro.setPrestado(rs.getBoolean("prestado"));
                    libros.add(libro);
                }
            }
        }
        return libros;
    }

    private void cargarDatos(PreparedStatement ps, Libro libro) throws SQLException {
        ps.setString(1, libro.getTitulo());
        ps.setString(2, libro.getAutor());
        ps.setString(3, libro.getEditorial());
        if (libro.getAnioPublicacion() == null) {
            ps.setNull(4, Types.INTEGER);
        } else {
            ps.setInt(4, libro.getAnioPublicacion());
        }
        ps.setBoolean(5, libro.isActivo());
    }

    static Libro mapearLibro(ResultSet rs) throws SQLException {
        Libro l = new Libro();
        l.setId(rs.getInt("id"));
        l.setTitulo(rs.getString("titulo"));
        l.setAutor(rs.getString("autor"));
        l.setEditorial(rs.getString("editorial"));
        int anio = rs.getInt("anio_publicacion");
        l.setAnioPublicacion(rs.wasNull() ? null : anio);
        l.setActivo(rs.getBoolean("activo"));
        return l;
    }
}
