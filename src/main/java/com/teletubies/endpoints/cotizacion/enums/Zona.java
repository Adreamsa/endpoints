package com.teletubies.endpoints.cotizacion.enums;

public enum Zona {
    LOCAL("LOCAL"),
    NACIONAL("NACIONAL"),
    INTERNACIONAL("INTERNACIONAL");

    private final String value;

    Zona(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
