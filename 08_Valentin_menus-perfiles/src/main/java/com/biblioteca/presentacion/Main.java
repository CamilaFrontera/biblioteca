package com.biblioteca.presentacion;

import java.sql.SQLException;
import java.util.Scanner;

import com.biblioteca.persistencia.ILibroRepository;
import com.biblioteca.persistencia.IPrestamoRepository;
import com.biblioteca.persistencia.ISocioRepository;
import com.biblioteca.persistencia.LibroRepository;
import com.biblioteca.persistencia.PrestamoRepository;
import com.biblioteca.persistencia.SocioRepository;
import com.biblioteca.servicio.LibroService;
import com.biblioteca.servicio.LoginService;
import com.biblioteca.servicio.PermisosService;
import com.biblioteca.servicio.PrestamoService;
import com.biblioteca.servicio.ReglaNegocioException;
import com.biblioteca.servicio.SocioService;

/**
 * Punto de entrada. Muestra el menú inicial de perfiles y, según el perfil,
 * el menú con sus opciones. Arma las capas pasando cada dependencia por constructor.
 */
public class Main {
    private final Consola consola;
    private final ISocioRepository socioRepository = new SocioRepository();
    private final ILibroRepository libroRepository = new LibroRepository();
    private final IPrestamoRepository prestamoRepository = new PrestamoRepository();

    private SocioController socios;
    private LibroController libros;
    private PrestamoController prestamos;

    public Main(Consola consola) {
        this.consola = consola;
    }

    public static void main(String[] args) {
        try (Scanner teclado = new Scanner(System.in)) {
            new Main(new Consola(teclado)).iniciar();
        } catch (Consola.EntradaFinalizadaException e) {
            System.out.println("\nFin del sistema.");
        }
    }

    private void iniciar() {
        LoginService login = new LoginService(socioRepository);
        int opcion;
        do {
            System.out.println("\n========== SISTEMA DE GESTIÓN DE BIBLIOTECA ==========");
            System.out.println("Seleccione su perfil:");
            System.out.println("1. Socio");
            System.out.println("2. Bibliotecario");
            System.out.println("3. Administrador");
            System.out.println("0. Salir");
            opcion = consola.leerEntero("Opción");
            switch (opcion) {
                case 1 -> ingresarComoSocio(login);
                case 2 -> menuBibliotecario(login.ingresarComoBibliotecario());
                case 3 -> menuAdministrador(login.ingresarComoAdministrador());
                case 0 -> System.out.println("Fin del sistema.");
                default -> Vista.error("Opción incorrecta.");
            }
        } while (opcion != 0);
    }

    private void ingresarComoSocio(LoginService login) {
        int numero = consola.leerEntero("Ingrese su número de socio");
        try {
            menuSocio(login.ingresarComoSocio(numero));
        } catch (IllegalArgumentException e) {
            Vista.error(e.getMessage());
        } catch (SQLException e) {
            Vista.error("No se pudo acceder a PostgreSQL. Verifique la conexión. (" + e.getMessage() + ")");
        }
    }

    /** Crea los servicios y controladores con los permisos del perfil que ingresó. */
    private void armarCapas(PermisosService permisos) {
        socios = new SocioController(new SocioService(socioRepository, prestamoRepository, permisos), consola);
        libros = new LibroController(new LibroService(libroRepository, prestamoRepository, permisos), consola);
        prestamos = new PrestamoController(
                new PrestamoService(prestamoRepository, socioRepository, libroRepository, permisos), consola);
    }

    private void menuSocio(PermisosService permisos) {
        armarCapas(permisos);
        int opcion;
        do {
            System.out.println("\n--- MENÚ SOCIO: " + permisos.getSocioActual().getNombreCompleto() + " ---");
            if (!permisos.getSocioActual().isActivo()) {
                System.out.println("(Socio inactivo: puede consultar, pero no pedir préstamos)");
            }
            System.out.println("1. Buscar libros y ver disponibilidad");
            System.out.println("2. Mis préstamos");
            System.out.println("0. Volver al menú inicial");
            opcion = consola.leerEntero("Opción");
            switch (opcion) {
                case 1 -> ejecutar(libros::buscar);
                case 2 -> ejecutar(prestamos::misPrestamos);
                case 0 -> { }
                default -> Vista.error("Opción incorrecta.");
            }
        } while (opcion != 0);
    }

    private void menuBibliotecario(PermisosService permisos) {
        armarCapas(permisos);
        int opcion;
        do {
            System.out.println("\n--- MENÚ BIBLIOTECARIO ---");
            System.out.println("1. Registrar préstamo");
            System.out.println("2. Registrar devolución");
            System.out.println("3. Buscar libros");
            System.out.println("4. Consultar préstamos de un socio");
            System.out.println("5. Listar préstamos vigentes y vencidos");
            System.out.println("6. Registrar socio");
            System.out.println("7. Modificar socio");
            System.out.println("8. Consultar socio");
            System.out.println("9. Listar socios");
            System.out.println("10. Dar de baja socio");
            System.out.println("0. Volver al menú inicial");
            opcion = consola.leerEntero("Opción");
            switch (opcion) {
                case 1 -> ejecutar(prestamos::registrarPrestamo);
                case 2 -> ejecutar(prestamos::registrarDevolucion);
                case 3 -> ejecutar(libros::buscar);
                case 4 -> ejecutar(prestamos::historialDeSocio);
                case 5 -> ejecutar(prestamos::listarVigentesYVencidos);
                case 6 -> ejecutar(socios::registrar);
                case 7 -> ejecutar(socios::modificar);
                case 8 -> ejecutar(socios::consultar);
                case 9 -> ejecutar(socios::listar);
                case 10 -> ejecutar(socios::darDeBaja);
                case 0 -> { }
                default -> Vista.error("Opción incorrecta.");
            }
        } while (opcion != 0);
    }

    private void menuAdministrador(PermisosService permisos) {
        armarCapas(permisos);
        int opcion;
        do {
            System.out.println("\n--- MENÚ ADMINISTRADOR ---");
            System.out.println("1. Registrar libro");
            System.out.println("2. Modificar libro");
            System.out.println("3. Consultar libro");
            System.out.println("4. Buscar libros");
            System.out.println("5. Listar catálogo completo");
            System.out.println("6. Dar de baja libro");
            System.out.println("7. Listar socios");
            System.out.println("8. Consultar préstamos de un socio");
            System.out.println("9. Listar préstamos vigentes y vencidos");
            System.out.println("0. Volver al menú inicial");
            opcion = consola.leerEntero("Opción");
            switch (opcion) {
                case 1 -> ejecutar(libros::registrar);
                case 2 -> ejecutar(libros::modificar);
                case 3 -> ejecutar(libros::consultar);
                case 4 -> ejecutar(libros::buscar);
                case 5 -> ejecutar(libros::listar);
                case 6 -> ejecutar(libros::darDeBaja);
                case 7 -> ejecutar(socios::listar);
                case 8 -> ejecutar(prestamos::historialDeSocio);
                case 9 -> ejecutar(prestamos::listarVigentesYVencidos);
                case 0 -> { }
                default -> Vista.error("Opción incorrecta.");
            }
        } while (opcion != 0);
    }

    /**
     * Ejecuta una opción e informa cualquier error sin terminar el programa.
     */
    private void ejecutar(Operacion operacion) {
        try {
            operacion.ejecutar();
        } catch (ReglaNegocioException e) {
            Vista.error(e.getMessage());
            if (!e.getPrestamosRelacionados().isEmpty()) {
                System.out.println("Préstamos relacionados:");
                Vista.prestamos(e.getPrestamosRelacionados(), java.time.LocalDate.now(), false);
            }
        } catch (IllegalArgumentException | SecurityException e) {
            Vista.error(e.getMessage());
        } catch (SQLException e) {
            Vista.error("No se pudo completar la operación en la base de datos. " + e.getMessage());
        }
    }

    @FunctionalInterface
    private interface Operacion {
        void ejecutar() throws SQLException;
    }
}
