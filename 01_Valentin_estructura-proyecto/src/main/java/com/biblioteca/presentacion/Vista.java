package com.biblioteca.presentacion;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.biblioteca.modelo.Libro;
import com.biblioteca.modelo.Prestamo;
import com.biblioteca.modelo.Socio;

/**
 * Muestra listados y comprobantes en la consola.
 */
public class Vista {
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final String LINEA = "-".repeat(78);

    private Vista() {
    }

    public static String fecha(LocalDate f) {
        return f == null ? "-" : f.format(FECHA);
    }

    public static void mensaje(String texto) {
        System.out.println(">> " + texto);
    }

    public static void error(String texto) {
        System.out.println("ERROR: " + texto);
    }

    public static void socios(List<Socio> socios) {
        if (socios.isEmpty()) {
            mensaje("No hay socios registrados.");
            return;
        }
        System.out.printf("%-4s %-8s %-10s %-30s %-8s%n", "ID", "N° socio", "DNI", "Apellido y nombre", "Estado");
        System.out.println(LINEA);
        for (Socio s : socios) {
            System.out.printf("%-4d %-8d %-10s %-30s %-8s%n", s.getId(), s.getNumeroSocio(), s.getDni(),
                    recortar(s.getApellido() + ", " + s.getNombre(), 30), s.isActivo() ? "Activo" : "Inactivo");
        }
    }

    public static void socio(Socio s) {
        System.out.println("ID: " + s.getId() + " | N° socio: " + s.getNumeroSocio() + " | DNI: " + s.getDni());
        System.out.println("Nombre: " + s.getNombreCompleto() + " | Estado: " + (s.isActivo() ? "Activo" : "Inactivo"));
    }

    public static void libros(List<Libro> libros) {
        if (libros.isEmpty()) {
            mensaje("No se encontraron libros.");
            return;
        }
        System.out.printf("%-4s %-28s %-22s %-14s %-5s %-11s%n", "ID", "Título", "Autor", "Editorial", "Año", "Estado");
        System.out.println(LINEA);
        for (Libro l : libros) {
            System.out.printf("%-4d %-28s %-22s %-14s %-5s %-11s%n", l.getId(), recortar(l.getTitulo(), 28),
                    recortar(l.getAutor(), 22), recortar(l.getEditorial() == null ? "-" : l.getEditorial(), 14),
                    l.getAnioPublicacion() == null ? "-" : l.getAnioPublicacion(), estadoLibro(l));
        }
    }

    public static void libro(Libro l) {
        System.out.println("ID: " + l.getId() + " | " + l.getTitulo() + " - " + l.getAutor());
        System.out.println("Editorial: " + (l.getEditorial() == null ? "-" : l.getEditorial())
                + " | Año: " + (l.getAnioPublicacion() == null ? "-" : l.getAnioPublicacion())
                + " | Estado: " + estadoLibro(l));
    }

    public static void prestamos(List<Prestamo> prestamos, LocalDate hoy, boolean mostrarSocio) {
        if (prestamos.isEmpty()) {
            mensaje("No hay préstamos para mostrar.");
            return;
        }
        if (mostrarSocio) {
            System.out.printf("%-4s %-20s %-24s %-10s %-10s %-10s %-8s%n",
                    "ID", "Socio", "Libro", "Préstamo", "Vence", "Devuelto", "Estado");
        } else {
            System.out.printf("%-4s %-30s %-10s %-10s %-10s %-8s%n",
                    "ID", "Libro", "Préstamo", "Vence", "Devuelto", "Estado");
        }
        System.out.println(LINEA);
        for (Prestamo p : prestamos) {
            String estado = p.getEstado(hoy);
            if (p.estaVencido(hoy)) {
                estado += " (" + p.diasDeAtraso(hoy) + " d)";
            }
            if (mostrarSocio) {
                System.out.printf("%-4d %-20s %-24s %-10s %-10s %-10s %-8s%n", p.getId(),
                        recortar(p.getSocio().getNumeroSocio() + " " + p.getSocio().getApellido(), 20),
                        recortar("#" + p.getLibro().getId() + " " + p.getLibro().getTitulo(), 24),
                        fecha(p.getFechaPrestamo()), fecha(p.getFechaVencimiento()),
                        fecha(p.getFechaDevolucion()), estado);
            } else {
                System.out.printf("%-4d %-30s %-10s %-10s %-10s %-8s%n", p.getId(),
                        recortar("#" + p.getLibro().getId() + " " + p.getLibro().getTitulo(), 30),
                        fecha(p.getFechaPrestamo()), fecha(p.getFechaVencimiento()),
                        fecha(p.getFechaDevolucion()), estado);
            }
        }
    }

    public static void comprobantePrestamo(Prestamo p) {
        System.out.println();
        System.out.println("=============== COMPROBANTE DE PRÉSTAMO ===============");
        System.out.println("N° de préstamo:     " + p.getId());
        System.out.println("Socio:              " + p.getSocio().getNumeroSocio() + " - " + p.getSocio().getNombreCompleto()
                + " (DNI " + p.getSocio().getDni() + ")");
        System.out.println("Libro:              #" + p.getLibro().getId() + " - " + p.getLibro().getTitulo()
                + " (" + p.getLibro().getAutor() + ")");
        System.out.println("Fecha de préstamo:  " + fecha(p.getFechaPrestamo()));
        System.out.println("Fecha de vencimiento: " + fecha(p.getFechaVencimiento()) + " (" + Prestamo.DIAS_DE_PRESTAMO
                + " días corridos)");
        System.out.println("=======================================================");
    }

    public static void comprobanteDevolucion(Prestamo p) {
        long atraso = p.diasDeAtraso(p.getFechaDevolucion());
        System.out.println();
        System.out.println("============== COMPROBANTE DE DEVOLUCIÓN ==============");
        System.out.println("N° de préstamo:     " + p.getId());
        System.out.println("Socio:              " + p.getSocio().getNumeroSocio() + " - " + p.getSocio().getNombreCompleto());
        System.out.println("Libro:              #" + p.getLibro().getId() + " - " + p.getLibro().getTitulo());
        System.out.println("Fecha de préstamo:  " + fecha(p.getFechaPrestamo()));
        System.out.println("Fecha de vencimiento: " + fecha(p.getFechaVencimiento()));
        System.out.println("Fecha de devolución: " + fecha(p.getFechaDevolucion()));
        System.out.println("Situación:          " + (atraso == 0 ? "Devuelto en término" : "Devuelto con " + atraso + " día(s) de atraso"));
        System.out.println("El ejemplar quedó disponible.");
        System.out.println("=======================================================");
    }

    private static String estadoLibro(Libro l) {
        if (!l.isActivo()) {
            return "Dado de baja";
        }
        return l.isPrestado() ? "Prestado" : "Disponible";
    }

    private static String recortar(String texto, int largo) {
        if (texto == null) {
            return "";
        }
        return texto.length() <= largo ? texto : texto.substring(0, largo - 1) + "…";
    }
}
