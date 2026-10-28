package com.biblioteca.persistencia;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Conexión a PostgreSQL. Los datos se toman de variables de entorno para no
 * dejar credenciales en el código; si no están definidas se usan los valores
 * por defecto de una instalación local.
 *
 *   DB_URL      (por defecto jdbc:postgresql://localhost:5432/gestion_biblioteca)
 *   DB_USER     (por defecto postgres)
 *   DB_PASSWORD (por defecto postgres)
 */
public class ConexionBD {
    private static final String URL = leer("DB_URL",
            "jdbc:postgresql://localhost:5432/gestion_biblioteca");
    private static final String USUARIO = leer("DB_USER", "postgres");
    private static final String CLAVE = leer("DB_PASSWORD", "postgres");

    private ConexionBD() {
    }

    public static Connection conectar() throws SQLException {
        return DriverManager.getConnection(URL, USUARIO, CLAVE);
    }

    private static String leer(String variable, String porDefecto) {
        String valor = System.getenv(variable);
        return (valor == null || valor.isBlank()) ? porDefecto : valor;
    }
}
