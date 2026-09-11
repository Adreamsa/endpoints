package com.teletubies.endpoints.ejemplo.exception;

import com.teletubies.endpoints.api.PropinaApiController;
import com.teletubies.endpoints.model.ErrorResource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.OffsetDateTime;

/**
 * Traductor de excepciones de negocio a respuestas HTTP, SOLO para el ejemplo.
 *
 * ------------------------------------------------------------------------------------
 * LEE ESTO ANTES DE COPIARLO
 *
 * Este handler esta acotado a proposito con assignableTypes: aplica unicamente al
 * controller generado del tag `propina` y no toca nada de lo que tu escribas. Esta aqui
 * para que veas EL MECANISMO (@RestControllerAdvice + @ExceptionHandler), no para que lo
 * reutilices tal cual.
 *
 * El diseno del contrato de error de TU API es parte de lo que se evalua, y tienes que
 * resolverlo tu:
 *   - Que codigo corresponde a cada caso? Un cuerpo mal formado, un campo fuera de rango
 *     y una regla de negocio incumplida no son necesariamente el mismo error.
 *   - Como reportas varios errores de validacion a la vez? El contrato ya te da el hueco
 *     (ErrorResource.detalles), pero llenarlo es tuyo: un @Valid puede fallar en 3 campos
 *     al mismo tiempo, y devolver solo el primero deja al cliente corrigiendo de uno en uno.
 *   - Consideras RFC 7807 / ProblemDetail (soportado nativamente por Spring) o usas el
 *     ErrorResource que ya declara el contrato? Cualquiera de las dos es defendible:
 *     documenta cual elegiste. Si te vas por ProblemDetail, actualiza tambien el YAML —
 *     el contrato tiene que describir lo que la API realmente devuelve.
 *
 * Nota: aqui NO se maneja MethodArgumentNotValidException a proposito. Eso deja que los
 * fallos de @Valid caigan en el manejo por defecto de Spring, y es exactamente la pieza
 * que te toca disenar a ti.
 * ------------------------------------------------------------------------------------
 *
 * Fijate en que el cuerpo de error es ErrorResource, un modelo GENERADO desde
 * resources.yaml. Es la diferencia de fondo con hacerlo a mano: el error que devuelve el
 * codigo y el error que promete la documentacion son literalmente la misma clase, asi
 * que no pueden desincronizarse.
 */
@Slf4j
@RestControllerAdvice(assignableTypes = PropinaApiController.class)
public class EjemploExceptionHandler {

    /**
     * Se eligio 422 Unprocessable Entity y no 400 Bad Request con este criterio:
     *
     *   400 -> el request esta mal FORMADO. El servidor no pudo entenderlo:
     *          JSON roto, un campo obligatorio ausente, un valor fuera del rango que
     *          declara el contrato, una moneda que no esta en el enum.
     *   422 -> el request esta bien formado y se entendio perfectamente, pero su
     *          contenido incumple una regla de NEGOCIO. Cada campo es valido por
     *          separado; lo que no se puede procesar es la combinacion.
     *
     * Esta distincion es opinable y hay equipos que usan 400 para ambos casos. Ninguna de
     * las dos posturas esta mal. Lo que si esta mal es no tener criterio y que cada
     * endpoint responda distinto. Elige uno, aplicalo parejo, y escribelo en tu README.
     *
     * Lo que nunca es aceptable: responder 200 OK con un {"success": false} en el cuerpo.
     * El codigo de estado ES parte de la respuesta, no un adorno.
     */
    @ExceptionHandler(MontoPorPersonaInsuficienteException.class)
    public ResponseEntity<ErrorResource> manejarMontoInsuficiente(
            final MontoPorPersonaInsuficienteException ex) {

        // Se registran los datos del caso, no la excepcion completa: un stack trace por
        // cada request mal formado llena los logs de ruido y esconde los errores reales.
        log.warn("Peticion rechazada, reparto imposible: total={} personas={}",
                ex.getTotal(), ex.getNumeroPersonas());

        final ErrorResource cuerpo = ErrorResource.builder()
                .timestamp(OffsetDateTime.now())
                .status(HttpStatus.UNPROCESSABLE_ENTITY.value())
                .codigo("MONTO_POR_PERSONA_INSUFICIENTE")
                .mensaje(ex.getMessage())
                .build();

        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(cuerpo);
    }
}
