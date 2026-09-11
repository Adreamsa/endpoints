package com.teletubies.endpoints.ejemplo.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

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
 *
 * ---------------------------------------------------------------------------------
 * ESTA CLASE NO SE GENERA
 *
 * El schema `Moneda` esta en open-api/enum.yaml, pero el pom.xml lo mapea aqui con
 * <importMapping>. El contrato declara los valores; la clase la escribes tu.
 *
 * Fijate en lo que gana el enum por estar a mano: la descripcion legible. Un enum
 * generado solo tendria MXN, USD y EUR, y el texto "Peso mexicano" habria que guardarlo
 * aparte, en un Map dentro del servicio, lejos del valor al que pertenece. Aqui el codigo
 * y su etiqueta viajan juntos, que es justo lo que necesita el endpoint de catalogo.
 *
 * El precio: enum.yaml y esta clase tienen que decir lo mismo. Nadie lo verifica por ti.
 * ---------------------------------------------------------------------------------
 *
 * Nota de Lombok: @RequiredArgsConstructor genera el constructor a partir de los campos
 * `final`, y @Getter genera getDescripcion(). Sin las dos anotaciones tendrias que
 * escribir ambos a mano, que es exactamente el boilerplate que Lombok existe para evitar.
 */
@Getter
@RequiredArgsConstructor
public enum Moneda {

    MXN("Peso mexicano"),
    USD("Dolar estadounidense"),
    EUR("Euro");

    private final String descripcion;
}
