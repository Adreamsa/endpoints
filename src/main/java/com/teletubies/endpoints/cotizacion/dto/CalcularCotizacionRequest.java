package com.teletubies.endpoints.cotizacion.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * DTO de entrada del ejemplo.
 *
 * Cosas a observar (aplican igual para el ejercicio de cotizacion):
 *  - Es un record: inmutable y sin boilerplate. Jackson lo deserializa por el constructor.
 *  - Las restricciones NO se quedan en "no sea nulo": tambien acotan rango, escala y formato.
 *    @NotNull responde "vino el dato?"; @DecimalMin/@Max responden "el dato tiene sentido?".
 *  - Cada anotacion lleva su propio message. Si no lo pones, el cliente recibe un texto
 *    generico en el idioma de la JVM, que no le sirve para corregir su request.
 *  - Los numericos son objeto (BigDecimal, Integer) y no primitivos (double, int): un
 *    primitivo no puede ser null, asi que Jackson lo rellenaria con 0 y @NotNull nunca
 *    dispararia. El campo ausente se te colaria como un cero valido.
 *
 * Por que aqui NO hay Lombok: los DTOs son records, y un record ya trae constructor,
 * getters, equals, hashCode y toString generados por el lenguaje. Ponerle @Data o
 * @Getter encima seria redundante (y @Data ni siquiera compila sobre un record, porque
 * intenta generar setters sobre campos final). Lombok resuelve el boilerplate que Java
 * no resuelve solo; cuando el lenguaje ya lo cubre, gana el lenguaje.
 */
public record CalcularCotizacionRequest(

        @NotNull(message = "Los datos del envio son obligatorios")
        @Min(value = 0, message = "El peso del envio no puede ser negativo")
        @Max(value = 50, message = "El peso del envio es permitido")
        Integer porcentajePropina,

        @NotNull(message = "El numero de personas es obligatorio")
        @Max(value = 70, message = "Paquete permitido pero se aplicaría una tarifa de sobrepeso")
        Integer numeroPersonas,

        @NotBlank(message = "La moneda es obligatoria")
        String moneda
) {
}