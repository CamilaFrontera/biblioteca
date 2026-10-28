package com.biblioteca.modelo;

/**
 * Perfiles que se eligen en el menú inicial.
 */
public enum Perfil {
    SOCIO("Socio"),
    BIBLIOTECARIO("Bibliotecario"),
    ADMINISTRADOR("Administrador");

    private final String descripcion;

    Perfil(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
