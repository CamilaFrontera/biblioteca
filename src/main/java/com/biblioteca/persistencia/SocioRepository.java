package com.biblioteca.persistencia;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.biblioteca.modelo.Socio;

public class SocioRepository implements ISocioRepository {

    @Override
    public int guardar(Socio socio) throws SQLException {
        String sql = """
                INSERT INTO socios (numero_socio, dni, nombre, apellido, activo)
                VALUES (?, ?, ?, ?, ?)
                """;
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, socio.getNumeroSocio());
            ps.setString(2, socio.getDni());
            ps.setString(3, socio.getNombre());
            ps.setString(4, socio.getApellido());
            ps.setBoolean(5, socio.isActivo());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                socio.setId(rs.getInt(1));
                return socio.getId();
            }
        }
    }

    @Override
    public void modificar(Socio socio) throws SQLException {
        String sql = """
                UPDATE socios
                SET numero_socio = ?, dni = ?, nombre = ?, apellido = ?, activo = ?
                WHERE id = ?
                """;
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, socio.getNumeroSocio());
            ps.setString(2, socio.getDni());
            ps.setString(3, socio.getNombre());
            ps.setString(4, socio.getApellido());
            ps.setBoolean(5, socio.isActivo());
            ps.setInt(6, socio.getId());
            ps.executeUpdate();
        }
    }

    @Override
    public Socio buscarPorId(int id) throws SQLException {
        return buscarUno("SELECT * FROM socios WHERE id = ?", ps -> ps.setInt(1, id));
    }

    @Override
    public Socio buscarPorNumero(int numeroSocio) throws SQLException {
        return buscarUno("SELECT * FROM socios WHERE numero_socio = ?", ps -> ps.setInt(1, numeroSocio));
    }

    @Override
    public Socio buscarPorDni(String dni) throws SQLException {
        return buscarUno("SELECT * FROM socios WHERE dni = ?", ps -> ps.setString(1, dni));
    }

    @Override
    public List<Socio> listar() throws SQLException {
        List<Socio> socios = new ArrayList<>();
        String sql = "SELECT * FROM socios ORDER BY apellido, nombre";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                socios.add(mapearSocio(rs));
            }
        }
        return socios;
    }

    private Socio buscarUno(String sql, Parametros parametros) throws SQLException {
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            parametros.cargar(ps);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapearSocio(rs) : null;
            }
        }
    }

    static Socio mapearSocio(ResultSet rs) throws SQLException {
        Socio s = new Socio();
        s.setId(rs.getInt("id"));
        s.setNumeroSocio(rs.getInt("numero_socio"));
        s.setDni(rs.getString("dni"));
        s.setNombre(rs.getString("nombre"));
        s.setApellido(rs.getString("apellido"));
        s.setActivo(rs.getBoolean("activo"));
        return s;
    }
}
