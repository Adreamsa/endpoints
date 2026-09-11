package com.teletubies.endpoints.ejemplo.enums;

import java.util.Arrays;
import java.util.Optional;

/**
 * Catalogo del ejemplo, modelado como enum.
 *
 * Por que un enum y no una lista de strings:
 *  - El conjunto de valores validos vive en UN solo lugar. El endpoint de catalogo y la
 *    validacion del POST leen de la misma fuente, asi que no pueden contradecirse.
 *  - El compilador te avisa si agregas un valor y olvidas contemplarlo en algun switch.
 *
 * Ojo: esto NO es persistencia ni estado entre requests. Es una constante del codigo, igual
 * que un archivo de configuracion. El ejercicio prohibe estado, no prohibe constantes.
 */
public enum Moneda {

    MXN("Peso mexicano"),
    USD("Dolar estadounidense"),
    EUR("Euro");

    private final String descripcion;

    Moneda(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    /**
     * Busca de forma tolerante (ignora mayusculas/minusculas y espacios sobrantes).
     *
     * Devuelve Optional en lugar de null o de lanzar la excepcion aqui: quien llama decide
     * que significa "no existe" en su contexto. La capa de dominio no deberia estar
     * eligiendo codigos HTTP.
     */
    public static Optional<Moneda> desdeCodigo(String codigo) {
        if (codigo == null) {
            return Optional.empty();
        }
        String normalizado = codigo.trim().toUpperCase();
        return Arrays.stream(values())
                .filter(moneda -> moneda.name().equals(normalizado))
                .findFirst();
    }
}
