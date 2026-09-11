package com.teletubies.endpoints.ejemplo.exception;

/**
 * Excepcion de negocio del ejemplo.
 *
 * Fijate en lo que NO tiene: ninguna anotacion de Spring, ningun codigo HTTP.
 * La excepcion describe QUE paso en terminos del negocio; traducir eso a un status
 * es responsabilidad de la capa web (ver EjemploExceptionHandler).
 *
 * Es RuntimeException (unchecked) a proposito: obligar a la capa web a hacer try/catch
 * de cada regla de negocio solo agrega ruido, para eso existe el @RestControllerAdvice.
 */
public class MonedaNoSoportadaException extends RuntimeException {

    private final String codigoRecibido;

    public MonedaNoSoportadaException(String codigoRecibido) {
        super("La moneda '" + codigoRecibido + "' no esta soportada");
        this.codigoRecibido = codigoRecibido;
    }

    public String getCodigoRecibido() {
        return codigoRecibido;
    }
}
