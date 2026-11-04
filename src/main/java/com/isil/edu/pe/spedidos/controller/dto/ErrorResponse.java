package com.isil.edu.pe.spedidos.controller.dto;

/**
 * Cuerpo JSON uniforme para los errores de la API de reportes.
 */
public record ErrorResponse(int status, String error, String mensaje) {
}
