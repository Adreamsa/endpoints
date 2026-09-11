package com.teletubies.endpoints.ejemplo.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Catalogo del ejemplo.
 *
 * Un enum y no una lista de strings: los valores validos viven en un solo lugar, asi que
 * el endpoint de catalogo y la validacion del POST no se pueden contradecir. No es
 * persistencia ni estado entre requests, es una constante del codigo.
 *
 * ESTA CLASE NO SE GENERA. El schema `Moneda` esta en open-api/enum.yaml, pero el
 * pom.xml lo mapea aqui (importMapping + schemaMapping). El contrato declara los valores;
 * la clase la escribes tu.
 *
 * Lo que se gana: la descripcion legible. Un enum generado solo tendria MXN, USD y EUR, y
 * el texto "Peso mexicano" habria que guardarlo en un Map dentro del servicio, lejos del
 * valor al que pertenece. El precio: enum.yaml y esta clase tienen que decir lo mismo, y
 * nadie lo verifica.
 *
 * Lombok: @RequiredArgsConstructor genera el constructor desde los campos `final`,
 * @Getter genera getDescripcion().
 */
@Getter
@RequiredArgsConstructor
public enum Moneda {

    MXN("Peso mexicano"),
    USD("Dolar estadounidense"),
    EUR("Euro");

    private final String descripcion;
}
