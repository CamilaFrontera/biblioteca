package com.biblioteca.servicio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.biblioteca.modelo.Libro;
import com.biblioteca.modelo.Prestamo;
import com.biblioteca.modelo.Socio;

/**
 * Pruebas de las reglas de negocio RN-01 a RN-12 sobre la capa de servicio.
 */
class ReglasDeNegocioTest {
    private static final LocalDate HOY = LocalDate.of(2026, 10, 21);

    private RepositoriosEnMemoria.Socios socios;
    private RepositoriosEnMemoria.Libros libros;
    private RepositoriosEnMemoria.Prestamos prestamos;
    private PrestamoService prestamoService;
    private SocioService socioService;
    private LibroService libroAdmin;

    @BeforeEach
    void preparar() {
        socios = new RepositoriosEnMemoria.Socios();
        libros = new RepositoriosEnMemoria.Libros();
        prestamos = new RepositoriosEnMemoria.Prestamos();
        libros.prestamos = prestamos;
        Clock reloj = Clock.fixed(HOY.atStartOfDay(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());
        PermisosService bibliotecario = PermisosService.bibliotecario();
        prestamoService = new PrestamoService(prestamos, socios, libros, bibliotecario, reloj);
        socioService = new SocioService(socios, prestamos, bibliotecario);
        libroAdmin = new LibroService(libros, prestamos, PermisosService.administrador());

        socios.guardar(new Socio(1001, "30111222", "Lucía", "Fernández"));
        for (int i = 1; i <= 5; i++) {
            libros.guardar(new Libro("Libro " + i, "Autor " + i, "Editorial", 2000));
        }
    }

    @Test
    void registraPrestamoConVencimientoA7Dias() throws Exception {
        Prestamo p = prestamoService.registrarPrestamo(1001, 1);
        assertEquals(HOY, p.getFechaPrestamo());                  // RN-12
        assertEquals(HOY.plusDays(7), p.getFechaVencimiento());   // RN-05
        assertFalse(libros.buscarPorId(1).estaDisponible());
    }

    @Test
    void rechazaSocioInactivo() throws Exception {                 // RN-01
        socios.buscarPorNumero(1001).setActivo(false);
        assertThrows(ReglaNegocioException.class, () -> prestamoService.registrarPrestamo(1001, 1));
    }

    @Test
    void rechazaLibroPrestadoOInactivo() throws Exception {        // RN-02
        prestamoService.registrarPrestamo(1001, 1);
        socios.guardar(new Socio(1002, "31222333", "Martín", "Gómez"));
        assertThrows(ReglaNegocioException.class, () -> prestamoService.registrarPrestamo(1002, 1));
        libros.buscarPorId(2).setActivo(false);
        assertThrows(ReglaNegocioException.class, () -> prestamoService.registrarPrestamo(1002, 2));
    }

    @Test
    void limiteDeTresPrestamosSinDevolver() throws Exception {     // RN-03
        prestamoService.registrarPrestamo(1001, 1);
        prestamoService.registrarPrestamo(1001, 2);
        Prestamo tercero = prestamoService.registrarPrestamo(1001, 3);
        ReglaNegocioException e = assertThrows(ReglaNegocioException.class,
                () -> prestamoService.registrarPrestamo(1001, 4));
        assertEquals(3, e.getPrestamosRelacionados().size());
        // El límite cuenta solo los préstamos sin devolver, no el historial.
        prestamoService.registrarDevolucion(tercero.getId());
        prestamoService.registrarPrestamo(1001, 4);
    }

    @Test
    void socioConPrestamoVencidoNoPuedePedirOtro() throws Exception {  // RN-06 y RN-07
        Socio socio = socios.buscarPorNumero(1001);
        Prestamo viejo = new Prestamo(socio, libros.buscarPorId(1), HOY.minusDays(8));
        prestamos.guardar(viejo);
        assertTrue(viejo.estaVencido(HOY));
        ReglaNegocioException e = assertThrows(ReglaNegocioException.class,
                () -> prestamoService.registrarPrestamo(1001, 2));
        assertEquals(1, e.getPrestamosRelacionados().size());
    }

    @Test
    void prestamoQueVenceHoyTodaviaNoEstaVencido() {               // RN-07
        Prestamo p = new Prestamo(new Socio(), new Libro(), HOY.minusDays(7));
        assertFalse(p.estaVencido(HOY));
        assertTrue(p.estaVencido(HOY.plusDays(1)));
    }

    @Test
    void devolucionSoloUnaVezYLibroQuedaDisponible() throws Exception {  // RN-08
        Prestamo p = prestamoService.registrarPrestamo(1001, 1);
        Prestamo devuelto = prestamoService.registrarDevolucion(p.getId());
        assertEquals(HOY, devuelto.getFechaDevolucion());
        assertTrue(libros.buscarPorId(1).estaDisponible());
        assertThrows(ReglaNegocioException.class, () -> prestamoService.registrarDevolucion(p.getId()));
        assertThrows(IllegalArgumentException.class, () -> prestamoService.registrarDevolucion(99));
    }

    @Test
    void noSeDaDeBajaConPrestamosSinDevolver() throws Exception {  // RN-09 y RN-10
        Prestamo p = prestamoService.registrarPrestamo(1001, 1);
        assertThrows(ReglaNegocioException.class, () -> socioService.darDeBaja(1001));
        assertThrows(ReglaNegocioException.class, () -> libroAdmin.darDeBaja(1));
        prestamoService.registrarDevolucion(p.getId());
        socioService.darDeBaja(1001);
        libroAdmin.darDeBaja(1);
        assertFalse(socios.buscarPorNumero(1001).isActivo());
        assertEquals(1, prestamos.listarPorSocio(1).size()); // conserva el historial
    }

    @Test
    void dniYNumeroDeSocioNoSeRepiten() {                          // RN-11
        assertThrows(ReglaNegocioException.class,
                () -> socioService.registrar(new Socio(2000, "30111222", "Otra", "Persona")));
        assertThrows(ReglaNegocioException.class,
                () -> socioService.registrar(new Socio(1001, "40111222", "Otra", "Persona")));
        assertThrows(IllegalArgumentException.class,
                () -> socioService.registrar(new Socio(2000, "12ab", "Otra", "Persona")));
    }

    @Test
    void cadaPerfilSoloHaceLoSuyo() {
        LibroService libroBibliotecario = new LibroService(libros, prestamos, PermisosService.bibliotecario());
        assertThrows(SecurityException.class,
                () -> libroBibliotecario.registrar(new Libro("X", "Y", null, null)));
        Socio socio = socios.buscarPorNumero(1001);
        PrestamoService comoSocio = new PrestamoService(prestamos, socios, libros, PermisosService.socio(socio));
        assertThrows(SecurityException.class, () -> comoSocio.registrarPrestamo(1001, 1));
        assertThrows(SecurityException.class, comoSocio::listarVencidos);
    }
}
