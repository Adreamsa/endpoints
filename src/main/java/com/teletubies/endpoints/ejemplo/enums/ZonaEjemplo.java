package com.teletubies.endpoints.ejemplo.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Zonas de DEMOSTRACION. No son las del ejercicio.
 *
 * Existen para que el contrato de cotizacion compile y puedas ver la API completa
 * funcionando desde el primer `make run`. El enunciado te pide definir tu tabla de
 * tarifas, y eso empieza por decidir cuales son tus zonas: pueden ser otras, mas, o
 * llamarse distinto.
 *
 * TE TOCA A TI:
 *   1. Renombra el schema `ZonaEjemplo` a `Zona` en open-api/enum.yaml con TUS valores.
 *   2. Actualiza los tres $ref que apuntan ahi (1 en requests.yaml, 2 en resources.yaml).
 *   3. Escribe cotizacion/enums/Zona.java copiando la forma de esta clase.
 *   4. Reapunta sus dos lineas en el pom.xml.
 *
 * Fijate en lo que gana el enum por estar escrito a mano: la descripcion legible que
 * devuelve GET /api/v1/zonas. Cuando definas tu tabla de tarifas, el costo base y el
 * tiempo de entrega caben aqui como dos campos mas. Ponerlos aqui o en el servicio es
 * decision tuya; documenta la que tomes.
 */
@Getter
@RequiredArgsConstructor
public enum ZonaEjemplo {

    LOCAL("Envio dentro de la misma ciudad"),
    NACIONAL("Envio dentro del territorio nacional"),
    INTERNACIONAL("Envio fuera del pais");

    private final String descripcion;
}
