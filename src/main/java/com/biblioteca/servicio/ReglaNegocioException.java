package com.biblioteca.servicio;

import java.util.List;

import com.biblioteca.modelo.Prestamo;

/**
 * Se lanza cuando una operación no cumple una regla de negocio.
 * Puede llevar los préstamos que explican el rechazo (por ejemplo, los
 * vencidos de un socio) para que la presentación los muestre.
 */
public class ReglaNegocioException extends RuntimeException {
    private final List<Prestamo> prestamosRelacionados;

    public ReglaNegocioException(String mensaje) {
        this(mensaje, List.of());
    }

    public ReglaNegocioException(String mensaje, List<Prestamo> prestamosRelacionados) {
        super(mensaje);
        this.prestamosRelacionados = List.copyOf(prestamosRelacionados);
    }

    public List<Prestamo> getPrestamosRelacionados() {
        return prestamosRelacionados;
    }
}
