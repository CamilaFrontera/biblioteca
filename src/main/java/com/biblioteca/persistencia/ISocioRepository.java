package com.biblioteca.persistencia;

import java.sql.SQLException;
import java.util.List;

import com.biblioteca.modelo.Socio;

/**
 * Contrato de acceso a datos de socios. El servicio depende de esta interfaz,
 * no de la clase concreta, para poder probarlo sin base de datos.
 */
public interface ISocioRepository {
    int guardar(Socio socio) throws SQLException;
    void modificar(Socio socio) throws SQLException;
    Socio buscarPorId(int id) throws SQLException;
    Socio buscarPorNumero(int numeroSocio) throws SQLException;
    Socio buscarPorDni(String dni) throws SQLException;
    List<Socio> listar() throws SQLException;
}
