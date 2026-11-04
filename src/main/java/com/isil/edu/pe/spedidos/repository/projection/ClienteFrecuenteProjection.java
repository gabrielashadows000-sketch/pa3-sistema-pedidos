package com.isil.edu.pe.spedidos.repository.projection;

import java.math.BigDecimal;

/**
 * Resultado agregado (GROUP BY / HAVING) por cliente, obtenido
 * directamente mediante Spring JDBC.
 */
public record ClienteFrecuenteProjection(
    String cliente,
    long totalPedidos,
    long totalUnidades,
    BigDecimal importeAcumulado,
    BigDecimal ticketPromedio
) {
}
