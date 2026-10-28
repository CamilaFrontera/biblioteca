package com.biblioteca.servicio;

import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

import com.biblioteca.modelo.Socio;
import com.biblioteca.persistencia.IPrestamoRepository;
import com.biblioteca.persistencia.ISocioRepository;

/**
 * Reglas de negocio de socios (RF-01, RF-02, RN-09, RN-10, RN-11).
 */
public class SocioService {
    private final ISocioRepository socios;
    private final IPrestamoRepository prestamos;
    private final PermisosService permisos;

    public SocioService(ISocioRepository socios, IPrestamoRepository prestamos, PermisosService permisos) {
        this.socios = Objects.requireNonNull(socios);
        this.prestamos = Objects.requireNonNull(prestamos);
        this.permisos = Objects.requireNonNull(permisos);
    }

    public Socio registrar(Socio socio) throws SQLException {
        permisos.exigirBibliotecario();
        normalizar(socio);
        validar(socio);
        verificarDuplicados(socio);
        socio.setActivo(true);
        socios.guardar(socio);
        return socio;
    }

    public Socio modificar(Socio socio) throws SQLException {
        permisos.exigirBibliotecario();
        Socio actual = socios.buscarPorId(socio.getId());
        if (actual == null) {
            throw new IllegalArgumentException("No existe el socio a modificar");
        }
        normalizar(socio);
        validar(socio);
        verificarDuplicados(socio);
        socio.setActivo(actual.isActivo()); // la baja se hace solo con darDeBaja
        socios.modificar(socio);
        return socio;
    }

    public Socio buscarPorNumero(int numeroSocio) throws SQLException {
        permisos.exigirBibliotecarioOAdministrador();
        Socio socio = socios.buscarPorNumero(numeroSocio);
        if (socio == null) {
            throw new IllegalArgumentException("No existe un socio con el número " + numeroSocio);
        }
        return socio;
    }

    public List<Socio> listar() throws SQLException {
        permisos.exigirBibliotecarioOAdministrador();
        return socios.listar();
    }

    /** RN-09 y RN-10: baja lógica, bloqueada si tiene préstamos sin devolver. */
    public Socio darDeBaja(int numeroSocio) throws SQLException {
        permisos.exigirBibliotecario();
        Socio socio = socios.buscarPorNumero(numeroSocio);
        if (socio == null) {
            throw new IllegalArgumentException("No existe un socio con el número " + numeroSocio);
        }
        if (!socio.isActivo()) {
            throw new ReglaNegocioException("El socio ya está dado de baja");
        }
        int sinDevolver = prestamos.contarSinDevolverPorSocio(socio.getId());
        if (sinDevolver > 0) {
            throw new ReglaNegocioException("No se puede dar de baja: el socio tiene "
                    + sinDevolver + " préstamo(s) sin devolver",
                    prestamos.listarPorSocio(socio.getId()).stream().filter(p -> !p.estaDevuelto()).toList());
        }
        socio.darDeBaja();
        socios.modificar(socio);
        return socio;
    }

    private void normalizar(Socio socio) {
        socio.setDni(socio.getDni() == null ? null : socio.getDni().trim().replace(".", ""));
        socio.setNombre(socio.getNombre() == null ? null : socio.getNombre().trim());
        socio.setApellido(socio.getApellido() == null ? null : socio.getApellido().trim());
    }

    private void validar(Socio socio) {
        if (socio.getNumeroSocio() <= 0) {
            throw new IllegalArgumentException("El número de socio debe ser mayor a cero");
        }
        if (socio.getDni() == null || !socio.getDni().matches("\\d{7,8}")) {
            throw new IllegalArgumentException("El DNI debe tener 7 u 8 dígitos");
        }
        if (socio.getNombre() == null || socio.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }
        if (socio.getApellido() == null || socio.getApellido().isBlank()) {
            throw new IllegalArgumentException("El apellido es obligatorio");
        }
        if (socio.getNombre().length() > 60 || socio.getApellido().length() > 60) {
            throw new IllegalArgumentException("Nombre y apellido admiten hasta 60 caracteres");
        }
    }

    /** RN-11: el DNI y el número de socio no pueden repetirse. */
    private void verificarDuplicados(Socio socio) throws SQLException {
        Socio mismoDni = socios.buscarPorDni(socio.getDni());
        if (mismoDni != null && mismoDni.getId() != socio.getId()) {
            throw new ReglaNegocioException("Ya existe un socio con el DNI " + socio.getDni());
        }
        Socio mismoNumero = socios.buscarPorNumero(socio.getNumeroSocio());
        if (mismoNumero != null && mismoNumero.getId() != socio.getId()) {
            throw new ReglaNegocioException("El número de socio " + socio.getNumeroSocio() + " ya está registrado");
        }
    }
}
