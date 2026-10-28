package com.biblioteca.modelo;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Préstamo de un único ejemplar a un socio (RN-04).
 */
public class Prestamo {
    public static final int DIAS_DE_PRESTAMO = 7;

    private int id;
    private Socio socio;
    private Libro libro;
    private LocalDate fechaPrestamo;
    private LocalDate fechaVencimiento;
    private LocalDate fechaDevolucion;

    public Prestamo() {
    }

    /** RN-05: el vencimiento es 7 días corridos después de la fecha de préstamo. */
    public Prestamo(Socio socio, Libro libro, LocalDate fechaPrestamo) {
        this.socio = socio;
        this.libro = libro;
        this.fechaPrestamo = fechaPrestamo;
        this.fechaVencimiento = fechaPrestamo.plusDays(DIAS_DE_PRESTAMO);
    }

    public boolean estaDevuelto() {
        return fechaDevolucion != null;
    }

    /** RN-07: vencido = superó la fecha de vencimiento y no tiene devolución. */
    public boolean estaVencido(LocalDate hoy) {
        return !estaDevuelto() && hoy.isAfter(fechaVencimiento);
    }

    /** RN-08: la devolución solo puede registrarse una vez. */
    public void registrarDevolucion(LocalDate fecha) {
        if (estaDevuelto()) {
            throw new IllegalStateException("La devolución de este préstamo ya fue registrada el "
                    + fechaDevolucion);
        }
        this.fechaDevolucion = fecha;
    }

    /** Días de atraso a una fecha dada (0 si está en término). */
    public long diasDeAtraso(LocalDate referencia) {
        LocalDate hasta = estaDevuelto() ? fechaDevolucion : referencia;
        long dias = ChronoUnit.DAYS.between(fechaVencimiento, hasta);
        return Math.max(dias, 0);
    }

    public String getEstado(LocalDate hoy) {
        if (estaDevuelto()) {
            return "Devuelto";
        }
        return estaVencido(hoy) ? "Vencido" : "Vigente";
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Socio getSocio() { return socio; }
    public void setSocio(Socio socio) { this.socio = socio; }

    public Libro getLibro() { return libro; }
    public void setLibro(Libro libro) { this.libro = libro; }

    public LocalDate getFechaPrestamo() { return fechaPrestamo; }
    public void setFechaPrestamo(LocalDate fechaPrestamo) { this.fechaPrestamo = fechaPrestamo; }

    public LocalDate getFechaVencimiento() { return fechaVencimiento; }
    public void setFechaVencimiento(LocalDate fechaVencimiento) { this.fechaVencimiento = fechaVencimiento; }

    public LocalDate getFechaDevolucion() { return fechaDevolucion; }
    public void setFechaDevolucion(LocalDate fechaDevolucion) { this.fechaDevolucion = fechaDevolucion; }
}
