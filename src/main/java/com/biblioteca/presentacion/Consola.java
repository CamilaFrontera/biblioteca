package com.biblioteca.presentacion;

import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * Lectura de datos por consola. Vuelve a pedir el dato cuando el formato es
 * incorrecto, así una entrada inválida no termina el programa.
 */
public class Consola {
    private final Scanner teclado;

    public Consola(Scanner teclado) {
        this.teclado = teclado;
    }

    public String leerTexto(String etiqueta) {
        System.out.print(etiqueta + ": ");
        try {
            return teclado.nextLine().trim();
        } catch (NoSuchElementException e) {
            throw new EntradaFinalizadaException();
        }
    }

    public int leerEntero(String etiqueta) {
        while (true) {
            String texto = leerTexto(etiqueta);
            try {
                return Integer.parseInt(texto);
            } catch (NumberFormatException e) {
                System.out.println("  Ingrese un número entero.");
            }
        }
    }

    /** Para modificar: Enter conserva el valor actual. */
    public String leerTextoOpcional(String etiqueta, String actual) {
        String texto = leerTexto(etiqueta + " [" + (actual == null ? "" : actual) + "]");
        return texto.isEmpty() ? actual : texto;
    }

    /** Para modificar: Enter conserva el valor actual. */
    public int leerEnteroOpcional(String etiqueta, int actual) {
        while (true) {
            String texto = leerTexto(etiqueta + " [" + actual + "]");
            if (texto.isEmpty()) {
                return actual;
            }
            try {
                return Integer.parseInt(texto);
            } catch (NumberFormatException e) {
                System.out.println("  Ingrese un número entero.");
            }
        }
    }

    /** Año opcional: Enter lo deja vacío (o conserva el actual al modificar). */
    public Integer leerAnio(String etiqueta, Integer actual) {
        while (true) {
            String texto = leerTexto(etiqueta + (actual == null ? " (opcional)" : " [" + actual + "]"));
            if (texto.isEmpty()) {
                return actual;
            }
            try {
                return Integer.parseInt(texto);
            } catch (NumberFormatException e) {
                System.out.println("  Año de publicación inválido.");
            }
        }
    }

    public boolean confirmar(String pregunta) {
        String respuesta = leerTexto(pregunta + " (S/N)");
        return respuesta.equalsIgnoreCase("s") || respuesta.equalsIgnoreCase("si");
    }

    /** Se lanza cuando se cierra la entrada estándar (por ejemplo, Ctrl+D). */
    public static class EntradaFinalizadaException extends RuntimeException {
        public EntradaFinalizadaException() {
            super("Entrada finalizada");
        }
    }
}
