package com.teletubies.endpoints.cotizacion.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum Zona {
    LOCAL("LOCAL", 80.00),
    NACIONAL("NACIONAL", 100.00),
    INTERNACIONAL("INTERNACIONAL", 150.00);

    private final String value;
    private final Double tarifa;

}
