package com.teletubies.endpoints.cotizacion.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

@Getter
@RequiredArgsConstructor
public enum Zona {

    LOCAL("Envio dentro de la misma ciudad", new BigDecimal("80.00"), 1),
    NACIONAL("Envio dentro del territorio nacional", new BigDecimal("180.00"), 3),
    EXPRESS("Envio nacional con entrega prioritaria", new BigDecimal("350.00"), 1),
    INTERNACIONAL("Envio fuera del pais", new BigDecimal("950.00"), 10);

    private final String descripcion;
    private final BigDecimal costoBase;
    private final int diasEntrega;
}
