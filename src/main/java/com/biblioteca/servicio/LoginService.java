package com.biblioteca.servicio;

import java.sql.SQLException;
import java.util.Objects;

import com.biblioteca.modelo.Socio;
import com.biblioteca.persistencia.ISocioRepository;

/**
 * Acceso simplificado: el perfil se elige en el menú inicial.
 * Para ingresar como socio hay que indicar un socio existente.
 */
public class LoginService {
    private final ISocioRepository socios;

    public LoginService(ISocioRepository socios) {
        this.socios = Objects.requireNonNull(socios);
    }

    public PermisosService ingresarComoSocio(int numeroSocio) throws SQLException {
        Socio socio = socios.buscarPorNumero(numeroSocio);
        if (socio == null) {
            throw new IllegalArgumentException("No existe un socio con el número " + numeroSocio);
        }
        return PermisosService.socio(socio);
    }

    public PermisosService ingresarComoBibliotecario() {
        return PermisosService.bibliotecario();
    }

    public PermisosService ingresarComoAdministrador() {
        return PermisosService.administrador();
    }
}
