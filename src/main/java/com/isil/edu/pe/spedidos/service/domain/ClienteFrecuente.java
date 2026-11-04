package com.isil.edu.pe.spedidos.service.domain;

import java.math.BigDecimal;

/**
 * Cliente que alcanza o supera el umbral de pedidos solicitado.
 */
public record ClienteFrecuente(
    String cliente,
    long totalPedidos,
    long totalUnidades,
    BigDecimal importeAcumulado,
    BigDecimal ticketPromedio
) {
}
