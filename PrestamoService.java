package com.biblioteca.servicio;

import java.sql.SQLException;
import java.time.Clock;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

import com.biblioteca.modelo.Libro;
import com.biblioteca.modelo.Prestamo;
import com.biblioteca.modelo.Socio;
import com.biblioteca.persistencia.ILibroRepository;
import com.biblioteca.persistencia.IPrestamoRepository;
import com.biblioteca.persistencia.ISocioRepository;

/**
 * Coordina préstamos y devoluciones y aplica sus reglas (RN-01 a RN-08, RN-12).
 */
public class PrestamoService {
    public static final int MAXIMO_SIN_DEVOLVER = 3;

    private final IPrestamoRepository prestamos;
    private final ISocioRepository socios;
    private final ILibroRepository libros;
    private final PermisosService permisos;
    private final Clock reloj;

    public PrestamoService(IPrestamoRepository prestamos, ISocioRepository socios,
                           ILibroRepository libros, PermisosService permisos) {
        this(prestamos, socios, libros, permisos, Clock.systemDefaultZone());
    }

    /** El reloj se recibe para poder probar los vencimientos con fechas fijas. */
    public PrestamoService(IPrestamoRepository prestamos, ISocioRepository socios,
                           ILibroRepository libros, PermisosService permisos, Clock reloj) {
        this.prestamos = Objects.requireNonNull(prestamos);
        this.socios = Objects.requireNonNull(socios);
        this.libros = Objects.requireNonNull(libros);
        this.permisos = Objects.requireNonNull(permisos);
        this.reloj = Objects.requireNonNull(reloj);
    }

    /** RN-12: las operaciones usan la fecha actual del sistema. */
    public LocalDate hoy() {
        return LocalDate.now(reloj);
    }

    /**
     * Pasos 4 y 5 del CU-04: identifica al socio y comprueba que pueda pedir un préstamo.
     */
    public Socio verificarSocioParaPrestamo(int numeroSocio) throws SQLException {
        permisos.exigirBibliotecario();
        Socio socio = socios.buscarPorNumero(numeroSocio);
        if (socio == null) {
            throw new IllegalArgumentException("Socio inexistente: no hay un socio con el número " + numeroSocio);
        }
        // RN-01
        if (!socio.isActivo()) {
            throw new ReglaNegocioException("El socio está inactivo, no puede pedir préstamos");
        }
        // RN-06 y RN-07
        List<Prestamo> vencidos = prestamos.listarVencidosPorSocio(socio.getId(), hoy());
        if (!vencidos.isEmpty()) {
            throw new ReglaNegocioException("El socio tiene préstamos vencidos. Debe regularizar su situación "
                    + "antes de pedir otro libro", vencidos);
        }
        // RN-03
        int sinDevolver = prestamos.contarSinDevolverPorSocio(socio.getId());
        if (sinDevolver >= MAXIMO_SIN_DEVOLVER) {
            List<Prestamo> pendientes = prestamos.listarPorSocio(socio.getId()).stream()
                    .filter(p -> !p.estaDevuelto()).toList();
            throw new ReglaNegocioException("El socio ya tiene " + MAXIMO_SIN_DEVOLVER
                    + " préstamos sin devolver", pendientes);
        }
        return socio;
    }

    /** CU-04: registra el préstamo de un ejemplar a un socio. */
    public Prestamo registrarPrestamo(int numeroSocio, int idLibro) throws SQLException {
        Socio socio = verificarSocioParaPrestamo(numeroSocio);

        // RN-02
        Libro libro = libros.buscarPorId(idLibro);
        if (libro == null || !libro.isActivo()) {
            throw new ReglaNegocioException("El libro no existe o está dado de baja");
        }
        if (prestamos.existeSinDevolverPorLibro(idLibro)) {
            throw new ReglaNegocioException("El libro \"" + libro.getTitulo() + "\" está prestado");
        }

        // RN-04, RN-05 y RN-12
        Prestamo prestamo = new Prestamo(socio, libro, hoy());
        try {
            prestamos.guardar(prestamo);
        } catch (SQLException e) {
            // 23505 = violación del índice único: otro préstamo del mismo ejemplar se registró antes
            if ("23505".equals(e.getSQLState())) {
                throw new ReglaNegocioException("El libro \"" + libro.getTitulo() + "\" está prestado");
            }
            throw e;
        }
        libro.setPrestado(true);
        return prestamo;
    }

    /** CU-05: registra la devolución y el ejemplar queda disponible. */
    public Prestamo registrarDevolucion(int idPrestamo) throws SQLException {
        permisos.exigirBibliotecario();
        Prestamo prestamo = prestamos.buscarPorId(idPrestamo);
        if (prestamo == null) {
            throw new IllegalArgumentException("No existe un préstamo con el identificador " + idPrestamo);
        }
        // RN-08
        if (prestamo.estaDevuelto()) {
            throw new ReglaNegocioException("La devolución de este préstamo ya fue registrada el "
                    + prestamo.getFechaDevolucion().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        }
        LocalDate fecha = hoy();
        if (!prestamos.registrarDevolucion(idPrestamo, fecha)) {
            throw new ReglaNegocioException("La devolución de este préstamo ya fue registrada");
        }
        prestamo.registrarDevolucion(fecha);
        prestamo.getLibro().setPrestado(false);
        return prestamo;
    }
}
