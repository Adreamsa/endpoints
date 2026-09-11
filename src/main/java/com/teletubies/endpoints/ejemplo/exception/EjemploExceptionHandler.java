package com.teletubies.endpoints.ejemplo.exception;

import com.teletubies.endpoints.ejemplo.controller.PropinaController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.OffsetDateTime;

/**
 * Traductor de excepciones de negocio a respuestas HTTP, SOLO para el controller del ejemplo.
 *
 * ------------------------------------------------------------------------------------
 * LEE ESTO ANTES DE COPIARLO
 *
 * Este handler esta acotado a proposito con assignableTypes: aplica unicamente a
 * PropinaController y no toca nada de lo que tu escribas. Esta aqui para que veas EL
 * MECANISMO (@RestControllerAdvice + @ExceptionHandler), no para que lo reutilices tal cual.
 *
 * El diseno del contrato de error de TU API es parte de lo que se evalua, y tienes que
 * resolverlo tu:
 *   - Que forma tiene el cuerpo de error, y es la MISMA en los dos endpoints?
 *   - Como reportas varios errores de validacion a la vez? (un @Valid puede fallar en 3
 *     campos al mismo tiempo; devolver solo el primero deja al cliente corrigiendo de
 *     uno en uno)
 *   - Que status corresponde a cada caso? Un cuerpo mal formado, un campo fuera de rango
 *     y una regla de negocio incumplida no son necesariamente el mismo error.
 *   - Consideras RFC 7807 / ProblemDetail (soportado nativamente por Spring) o defines
 *     tu propio formato? Cualquiera de las dos es defendible: documenta cual elegiste.
 *
 * Nota: aqui NO se maneja MethodArgumentNotValidException a proposito. Eso deja que los
 * fallos de @Valid caigan en el manejo por defecto de Spring, y es exactamente la pieza
 * que te toca disenar a ti.
 * ------------------------------------------------------------------------------------
 */
@Slf4j
@RestControllerAdvice(assignableTypes = PropinaController.class)
public class EjemploExceptionHandler {

    /**
     * Cuerpo de error del ejemplo. Se declara como record anidado para dejar claro que es
     * privado de este ejemplo y no un contrato compartido.
     */
    public record EjemploErrorResponse(
            OffsetDateTime timestamp,
            int status,
            String codigo,
            String mensaje
    ) {
    }

    /**
     * Se eligio 422 Unprocessable Entity y no 400 Bad Request con este criterio:
     *
     *   400 -> el request esta mal FORMADO. El servidor no pudo entenderlo:
     *          JSON roto, un campo obligatorio ausente, un texto donde iba un numero.
     *   422 -> el request esta bien formado y se entendio perfectamente, pero su
     *          contenido incumple una regla de NEGOCIO. "MXN" y "XYZ" son igual de
     *          validos sintacticamente; solo el dominio sabe que uno no existe.
     *
     * Esta distincion es opinable y hay equipos que usan 400 para ambos casos. Ninguna de
     * las dos posturas esta mal. Lo que si esta mal es no tener criterio y que cada
     * endpoint responda distinto. Elige uno, aplicalo parejo, y escribelo en tu README.
     *
     * Lo que nunca es aceptable: responder 200 OK con un {"success": false} en el cuerpo.
     * El codigo de estado ES parte de la respuesta, no un adorno.
     */
    @ExceptionHandler(MonedaNoSoportadaException.class)
    public ResponseEntity<EjemploErrorResponse> manejarMonedaNoSoportada(
            MonedaNoSoportadaException ex) {

        // Se registra el codigo recibido, no la excepcion completa: un stack trace por
        // cada request mal formado llena los logs de ruido y esconde los errores reales.
        log.warn("Peticion rechazada, moneda no soportada: '{}'", ex.getCodigoRecibido());

        EjemploErrorResponse cuerpo = new EjemploErrorResponse(
                OffsetDateTime.now(),
                HttpStatus.UNPROCESSABLE_ENTITY.value(),
                "MONEDA_NO_SOPORTADA",
                ex.getMessage()
        );

        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(cuerpo);
    }
}
