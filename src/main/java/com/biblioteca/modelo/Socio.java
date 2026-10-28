package com.biblioteca.modelo;

/**
 * Socio de la biblioteca. La baja es lógica: el socio queda inactivo
 * y conserva su historial de préstamos (RN-10).
 */
public class Socio {
    private int id;
    private int numeroSocio;
    private String dni;
    private String nombre;
    private String apellido;
    private boolean activo;

    public Socio() {
    }

    public Socio(int numeroSocio, String dni, String nombre, String apellido) {
        this.numeroSocio = numeroSocio;
        this.dni = dni;
        this.nombre = nombre;
        this.apellido = apellido;
        this.activo = true;
    }

    public String getNombreCompleto() {
        return nombre + " " + apellido;
    }

    public void darDeBaja() {
        this.activo = false;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getNumeroSocio() { return numeroSocio; }
    public void setNumeroSocio(int numeroSocio) { this.numeroSocio = numeroSocio; }

    public String getDni() { return dni; }
    public void setDni(String dni) { this.dni = dni; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
}
