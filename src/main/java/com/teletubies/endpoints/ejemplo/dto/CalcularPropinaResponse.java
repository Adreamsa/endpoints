package com.teletubies.endpoints.ejemplo.dto;

import java.math.BigDecimal;

/**
 * DTO de salida del ejemplo.
 *
 * Por que un DTO de salida separado del de entrada:
 *  - El contrato de respuesta evoluciona distinto al de peticion.
 *  - Exponer directamente tus objetos de dominio te obliga a filtrar campos despues.
 *
 * Nota de contrato: se devuelve tanto el desglose (propina, total) como el dato que el
 * cliente realmente iba a usar (totalPorPersona). Devolver el calculo intermedio le evita
 * al front tener que reimplementar el redondeo y llegar a un numero distinto al tuyo.
 */
public record CalcularPropinaResponse(
        String moneda,
        BigDecimal montoCuenta,
        Integer porcentajePropina,
        BigDecimal montoPropina,
        BigDecimal total,
        Integer numeroPersonas,
        BigDecimal totalPorPersona
) {
}
