package com.biblioteca.servicio;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.biblioteca.modelo.Libro;
import com.biblioteca.modelo.Prestamo;
import com.biblioteca.modelo.Socio;
import com.biblioteca.persistencia.ILibroRepository;
import com.biblioteca.persistencia.IPrestamoRepository;
import com.biblioteca.persistencia.ISocioRepository;

/**
 * Repositorios en memoria para probar la capa de negocio sin PostgreSQL.
 * Es posible porque los servicios dependen de interfaces.
 */
class RepositoriosEnMemoria {

    static class Socios implements ISocioRepository {
        final List<Socio> datos = new ArrayList<>();

        public int guardar(Socio s) { s.setId(datos.size() + 1); datos.add(s); return s.getId(); }
        public void modificar(Socio s) { datos.replaceAll(x -> x.getId() == s.getId() ? s : x); }
        public Socio buscarPorId(int id) { return datos.stream().filter(s -> s.getId() == id).findFirst().orElse(null); }
        public Socio buscarPorNumero(int n) { return datos.stream().filter(s -> s.getNumeroSocio() == n).findFirst().orElse(null); }
        public Socio buscarPorDni(String dni) { return datos.stream().filter(s -> s.getDni().equals(dni)).findFirst().orElse(null); }
        public List<Socio> listar() { return List.copyOf(datos); }
    }

    static class Libros implements ILibroRepository {
        final List<Libro> datos = new ArrayList<>();
        Prestamos prestamos;

        public int guardar(Libro l) { l.setId(datos.size() + 1); datos.add(l); return l.getId(); }
        public void modificar(Libro l) { datos.replaceAll(x -> x.getId() == l.getId() ? l : x); }
        public Libro buscarPorId(int id) {
            Libro l = datos.stream().filter(x -> x.getId() == id).findFirst().orElse(null);
            if (l != null && prestamos != null) l.setPrestado(prestamos.existeSinDevolverPorLibro(id));
            return l;
        }
        public List<Libro> buscarPorTitulo(String t) {
            return datos.stream().filter(l -> l.isActivo() && l.getTitulo().toLowerCase().contains(t.toLowerCase())).toList();
        }
        public List<Libro> buscarPorAutor(String a) {
            return datos.stream().filter(l -> l.isActivo() && l.getAutor().toLowerCase().contains(a.toLowerCase())).toList();
        }
        public List<Libro> listar() { return List.copyOf(datos); }
    }

    static class Prestamos implements IPrestamoRepository {
        final List<Prestamo> datos = new ArrayList<>();

        public int guardar(Prestamo p) { p.setId(datos.size() + 1); datos.add(p); return p.getId(); }
        public boolean registrarDevolucion(int id, LocalDate fecha) {
            Prestamo p = datos.stream().filter(x -> x.getId() == id).findFirst().orElse(null);
            if (p == null || p.estaDevuelto()) return false;
            p.setFechaDevolucion(fecha);
            return true;
        }
        public Prestamo buscarPorId(int id) {
            Prestamo p = datos.stream().filter(x -> x.getId() == id).findFirst().orElse(null);
            if (p == null) return null;
            // copia, como haría la base al leer de nuevo
            Prestamo c = new Prestamo();
            c.setId(p.getId()); c.setSocio(p.getSocio()); c.setLibro(p.getLibro());
            c.setFechaPrestamo(p.getFechaPrestamo()); c.setFechaVencimiento(p.getFechaVencimiento());
            c.setFechaDevolucion(p.getFechaDevolucion());
            return c;
        }
        public List<Prestamo> listarPorSocio(int socioId) {
            return datos.stream().filter(p -> p.getSocio().getId() == socioId).toList();
        }
        public List<Prestamo> listarSinDevolver() { return datos.stream().filter(p -> !p.estaDevuelto()).toList(); }
        public int contarSinDevolverPorSocio(int socioId) {
            return (int) datos.stream().filter(p -> p.getSocio().getId() == socioId && !p.estaDevuelto()).count();
        }
        public List<Prestamo> listarVencidosPorSocio(int socioId, LocalDate hoy) {
            return datos.stream().filter(p -> p.getSocio().getId() == socioId && p.estaVencido(hoy)).toList();
        }
        public boolean existeSinDevolverPorLibro(int libroId) {
            return datos.stream().anyMatch(p -> p.getLibro().getId() == libroId && !p.estaDevuelto());
        }
    }
}
