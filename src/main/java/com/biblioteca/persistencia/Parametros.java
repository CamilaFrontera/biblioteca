package com.biblioteca.persistencia;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Carga los parámetros "?" de una consulta. Permite reutilizar el código que
 * abre la conexión y recorre el resultado.
 */
@FunctionalInterface
interface Parametros {
    void cargar(PreparedStatement ps) throws SQLException;
}
