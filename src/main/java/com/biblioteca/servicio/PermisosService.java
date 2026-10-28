package com.biblioteca.servicio;

import java.util.Objects;

import com.biblioteca.modelo.Perfil;
import com.biblioteca.modelo.Socio;

/**
 * Autorización: decide qué puede hacer el perfil que ingresó.
 * Ocultar opciones del menú no alcanza; cada servicio lo verifica antes de operar.
 */
public class PermisosService {
    private final Perfil perfil;
    private final Socio socioActual;

    public PermisosService(Perfil perfil, Socio socioActual) {
        this.perfil = Objects.requireNonNull(perfil);
        if (perfil == Perfil.SOCIO && socioActual == null) {
            throw new IllegalArgumentException("Para ingresar como socio hay que indicar un socio existente");
        }
        this.socioActual = socioActual;
    }

    public static PermisosService bibliotecario() {
        return new PermisosService(Perfil.BIBLIOTECARIO, null);
    }

    public static PermisosService administrador() {
        return new PermisosService(Perfil.ADMINISTRADOR, null);
    }

    public static PermisosService socio(Socio socio) {
        return new PermisosService(Perfil.SOCIO, socio);
    }

    public void exigirBibliotecario() {
        if (perfil != Perfil.BIBLIOTECARIO) {
            throw new SecurityException("Operación permitida solo para el bibliotecario");
        }
    }

    public void exigirAdministrador() {
        if (perfil != Perfil.ADMINISTRADOR) {
            throw new SecurityException("Operación permitida solo para el administrador");
        }
    }

    /** Consultas generales de socios y préstamos (RF-07). */
    public void exigirBibliotecarioOAdministrador() {
        if (perfil == Perfil.SOCIO) {
            throw new SecurityException("Operación permitida solo para bibliotecario o administrador");
        }
    }

    public void exigirSocio() {
        if (perfil != Perfil.SOCIO) {
            throw new SecurityException("Operación permitida solo para socios");
        }
    }

    public Perfil getPerfil() {
        return perfil;
    }

    public Socio getSocioActual() {
        return socioActual;
    }
}
