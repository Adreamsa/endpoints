package com.teletubies.endpoints.ejemplo.exception;

import java.math.BigDecimal;

/**
 * Excepcion de negocio del ejemplo.
 *
 * Fijate en lo que NO tiene: ninguna anotacion de Spring, ningun codigo HTTP. Describe
 * QUE paso en terminos del negocio; traducirlo a un status es de la capa web.
 *
 * Es RuntimeException a proposito: obligar a la capa web a un try/catch por cada regla
 * solo agrega ruido, para eso existe el @RestControllerAdvice.
 *
 * Guarda los datos que provocaron el fallo, no solo el mensaje ya armado: quien la atrapa
 * puede necesitarlos para la respuesta o para el log.
 */
public class MontoPorPersonaInsuficienteException extends RuntimeException {

    private final BigDecimal total;
    private final Integer numeroPersonas;

    public MontoPorPersonaInsuficienteException(final BigDecimal total, final Integer numeroPersonas) {
        super("Repartir " + total + " entre " + numeroPersonas
                + " personas deja a cada una por debajo de la unidad minima de la moneda");
        this.total = total;
        this.numeroPersonas = numeroPersonas;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public Integer getNumeroPersonas() {
        return numeroPersonas;
    }
}
