package com.teletubies.endpoints.ejemplo.dto;

/**
 * DTO del catalogo del ejemplo.
 *
 * Observa que el catalogo NO se expone como una lista de strings sueltos: se devuelve el
 * codigo (lo que el cliente debe mandar de vuelta en el POST) junto con la descripcion
 * (lo que el cliente le muestra al usuario en el selector). Un catalogo de strings planos
 * obliga al front a hardcodear sus propias etiquetas, y ahi es donde se desincronizan.
 */
public record MonedaSoportadaResponse(
        String codigo,
        String descripcion
) {
}
