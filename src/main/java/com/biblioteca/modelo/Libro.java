package com.biblioteca.modelo;

/**
 * Ejemplar físico de un libro. Dos ejemplares del mismo título son dos
 * registros con identificadores distintos.
 *
 * La disponibilidad no se guarda en la tabla: se calcula a partir de los
 * préstamos. Un ejemplar activo está disponible cuando no tiene un préstamo
 * sin devolver. El repositorio completa el atributo "prestado" al leerlo.
 */
public class Libro {
    private int id;
    private String titulo;
    private String autor;
    private String editorial;
    private Integer anioPublicacion;
    private boolean activo;
    private boolean prestado;

    public Libro() {
    }

    public Libro(String titulo, String autor, String editorial, Integer anioPublicacion) {
        this.titulo = titulo;
        this.autor = autor;
        this.editorial = editorial;
        this.anioPublicacion = anioPublicacion;
        this.activo = true;
        this.prestado = false;
    }

    public boolean estaDisponible() {
        return activo && !prestado;
    }

    public void darDeBaja() {
        this.activo = false;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getAutor() { return autor; }
    public void setAutor(String autor) { this.autor = autor; }

    public String getEditorial() { return editorial; }
    public void setEditorial(String editorial) { this.editorial = editorial; }

    public Integer getAnioPublicacion() { return anioPublicacion; }
    public void setAnioPublicacion(Integer anioPublicacion) { this.anioPublicacion = anioPublicacion; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    public boolean isPrestado() { return prestado; }
    public void setPrestado(boolean prestado) { this.prestado = prestado; }
}
