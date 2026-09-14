package com.teletubies.endpoints.cotizacion.exception;

import java.math.BigDecimal;

public class SobrepesoException extends RuntimeException {

    private static final BigDecimal PESO_MAXIMO = new BigDecimal("70");

    private final BigDecimal pesoKg;

    public SobrepesoException(final BigDecimal pesoKg) {
        super("El paquete de " + pesoKg + " kg supera el peso maximo permitido de "
                + PESO_MAXIMO + " kg");
        this.pesoKg = pesoKg;
    }

    public BigDecimal getPesoKg() {
        return pesoKg;
    }
}
