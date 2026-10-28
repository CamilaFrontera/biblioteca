package com.biblioteca.presentacion;

import java.sql.SQLException;
import java.util.Objects;

import com.biblioteca.modelo.Prestamo;
import com.biblioteca.modelo.Socio;
import com.biblioteca.servicio.PrestamoService;

/**
 * Opciones de consola de préstamos y devoluciones.
 */
public class PrestamoController {
    private final PrestamoService service;
    private final Consola consola;

    public PrestamoController(PrestamoService service, Consola consola) {
        this.service = Objects.requireNonNull(service);
        this.consola = Objects.requireNonNull(consola);
    }

    /** CU-04. Primero valida al socio y recién después pide el libro. */
    public void registrarPrestamo() throws SQLException {
        System.out.println("\n--- Registrar préstamo ---");
        int numeroSocio = consola.leerEntero("Número de socio");
        Socio socio = service.verificarSocioParaPrestamo(numeroSocio);
        Vista.mensaje("Socio habilitado: " + socio.getNombreCompleto());
        int idLibro = consola.leerEntero("ID del libro (ejemplar)");
        Prestamo prestamo = service.registrarPrestamo(numeroSocio, idLibro);
        Vista.mensaje("Préstamo registrado.");
        Vista.comprobantePrestamo(prestamo);
    }

    /** CU-05. */
    public void registrarDevolucion() throws SQLException {
        System.out.println("\n--- Registrar devolución ---");
        System.out.println("Si no conoce el N° de préstamo, consulte los préstamos del socio (opción del menú).");
        Prestamo prestamo = service.registrarDevolucion(consola.leerEntero("N° de préstamo"));
        Vista.mensaje("Devolución registrada.");
        Vista.comprobanteDevolucion(prestamo);
    }

    public void historialDeSocio() throws SQLException {
        System.out.println("\n--- Préstamos de un socio ---");
        Vista.prestamos(service.historialDeSocio(consola.leerEntero("Número de socio")), service.hoy(), false);
    }

    public void misPrestamos() throws SQLException {
        System.out.println("\n--- Mis préstamos ---");
        Vista.prestamos(service.misPrestamos(), service.hoy(), false);
    }

    public void listarVigentesYVencidos() throws SQLException {
        System.out.println("\n--- Préstamos vigentes ---");
        Vista.prestamos(service.listarVigentes(), service.hoy(), true);
        System.out.println("\n--- Préstamos vencidos ---");
        Vista.prestamos(service.listarVencidos(), service.hoy(), true);
    }
}
