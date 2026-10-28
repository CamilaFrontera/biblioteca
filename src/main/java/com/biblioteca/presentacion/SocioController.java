package com.biblioteca.presentacion;

import java.sql.SQLException;
import java.util.Objects;

import com.biblioteca.modelo.Socio;
import com.biblioteca.servicio.SocioService;

/**
 * Opciones de consola de gestión de socios. Pide los datos, llama al
 * servicio y muestra el resultado. No contiene reglas de negocio ni SQL.
 */
public class SocioController {
    private final SocioService service;
    private final Consola consola;

    public SocioController(SocioService service, Consola consola) {
        this.service = Objects.requireNonNull(service);
        this.consola = Objects.requireNonNull(consola);
    }

    public void registrar() throws SQLException {
        System.out.println("\n--- Registrar socio ---");
        int numero = consola.leerEntero("Número de socio");
        String dni = consola.leerTexto("DNI (sin puntos)");
        String nombre = consola.leerTexto("Nombre");
        String apellido = consola.leerTexto("Apellido");
        Socio socio = service.registrar(new Socio(numero, dni, nombre, apellido));
        Vista.mensaje("Socio registrado con ID " + socio.getId() + ".");
    }

    public void modificar() throws SQLException {
        System.out.println("\n--- Modificar socio ---");
        Socio socio = service.buscarPorNumero(consola.leerEntero("Número de socio a modificar"));
        Vista.socio(socio);
        System.out.println("Presione Enter para conservar el valor actual.");
        socio.setNumeroSocio(consola.leerEnteroOpcional("Número de socio", socio.getNumeroSocio()));
        socio.setDni(consola.leerTextoOpcional("DNI", socio.getDni()));
        socio.setNombre(consola.leerTextoOpcional("Nombre", socio.getNombre()));
        socio.setApellido(consola.leerTextoOpcional("Apellido", socio.getApellido()));
        service.modificar(socio);
        Vista.mensaje("Socio modificado.");
    }

    public void consultar() throws SQLException {
        System.out.println("\n--- Consultar socio ---");
        Vista.socio(service.buscarPorNumero(consola.leerEntero("Número de socio")));
    }

    public void listar() throws SQLException {
        System.out.println("\n--- Socios ---");
        Vista.socios(service.listar());
    }

    public void darDeBaja() throws SQLException {
        System.out.println("\n--- Dar de baja socio ---");
        int numero = consola.leerEntero("Número de socio");
        Vista.socio(service.buscarPorNumero(numero));
        if (consola.confirmar("¿Confirma la baja?")) {
            service.darDeBaja(numero);
            Vista.mensaje("Socio dado de baja. Su historial se conserva.");
        } else {
            Vista.mensaje("Operación cancelada.");
        }
    }
}
