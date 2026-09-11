package com.teletubies.endpoints.ejemplo.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Catalogo de zonas de DEMOSTRACION.
 *
 * ---------------------------------------------------------------------------------
 * ESTO NO ES TU CATALOGO DE ZONAS
 *
 * Vive en el paquete `ejemplo` y se llama ZonaEjemplo por la misma razon: estas tres
 * zonas existen para que el contrato de cotizacion compile y puedas ver la API completa
 * funcionando antes de escribir una linea. El enunciado te pide definir TU tabla de
 * tarifas, y eso empieza por decidir cuales son tus zonas: pueden ser otras, pueden ser
 * mas, pueden llamarse distinto.
 *
 * Lo que te toca hacer:
 *   1. Renombra el schema `ZonaEjemplo` a `Zona` en open-api/enum.yaml y pon TUS valores.
 *   2. Actualiza los tres $ref que apuntan ahi (uno en requests.yaml, dos en
 *      resources.yaml).
 *   3. Escribe tu `cotizacion/enums/Zona.java`, copiando la forma de esta clase.
 *   4. Cambia las dos lineas del pom.xml (importMapping y schemaMapping) para que
 *      apunten a la tuya.
 *   5. Borra esta clase junto con el resto del paquete `ejemplo`.
 *
 * Hasta que hagas el paso 4, el pom.xml mapea el schema a ESTA clase, asi que tu API de
 * cotizacion esta usando las zonas de demostracion. Es deliberado: el proyecto arranca
 * entero desde el primer `make run`, y sustituir la pieza de muestra por la tuya es parte
 * del ejercicio, no un requisito previo para empezar.
 * ---------------------------------------------------------------------------------
 *
 * Fijate en lo que gana el enum por estar escrito a mano y no generado: la descripcion
 * legible. Un enum generado solo tendria LOCAL, NACIONAL e INTERNACIONAL, y el texto que
 * devuelve GET /api/v1/zonas habria que guardarlo aparte, en un Map dentro del servicio,
 * lejos del valor al que pertenece.
 *
 * Y es el sitio natural para poner mas cosas: cuando definas tu tabla de tarifas, el costo
 * base y el tiempo de entrega de cada zona caben aqui como dos campos mas. Modelarlo asi o
 * dejarlo en el servicio es decision tuya; documenta la que tomes.
 *
 * Nota de Lombok: @RequiredArgsConstructor genera el constructor a partir de los campos
 * `final`, y @Getter genera getDescripcion().
 */
@Getter
@RequiredArgsConstructor
public enum ZonaEjemplo {

    LOCAL("Envio dentro de la misma ciudad"),
    NACIONAL("Envio dentro del territorio nacional"),
    INTERNACIONAL("Envio fuera del pais");

    private final String descripcion;
}
