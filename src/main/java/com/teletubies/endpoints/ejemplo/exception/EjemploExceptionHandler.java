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
 * Traduce excepciones de negocio a respuestas HTTP, solo para el ejemplo.
 *
 * LEE ESTO ANTES DE COPIARLO. Esta acotado con assignableTypes al controller del tag
 * `propina`, asi que no toca nada de lo tuyo. Muestra EL MECANISMO, no una solucion para
 * reutilizar: el contrato de error de tu API es parte de lo que se evalua y tienes que
 * resolverlo tu.
 *
 *   - Que codigo corresponde a cada caso?
 *   - Como reportas varios errores de validacion a la vez? El contrato ya te da el hueco
 *     (ErrorResource.detalles), pero llenarlo es tuyo.
 *   - Usas ErrorResource o te vas por ProblemDetail? Si eliges lo segundo, actualiza el
 *     YAML: el contrato tiene que describir lo que la API devuelve de verdad.
 *
 * Aqui NO se maneja MethodArgumentNotValidException a proposito: los fallos de @Valid
 * caen en el manejo por defecto de Spring, y esa es justo la pieza que te toca disenar.
 *
 * Nota: el cuerpo de error es ErrorResource, un modelo generado desde resources.yaml. El
 * error que devuelve el codigo y el que promete la documentacion son la misma clase, asi
 * que no pueden desincronizarse.
 */
@Slf4j
@RestControllerAdvice(assignableTypes = PropinaApiController.class)
public class EjemploExceptionHandler {

    /**
     * 422 y no 400, con este criterio:
     *
     *   400 -> el request esta mal FORMADO: JSON roto, campo obligatorio ausente, valor
     *          fuera del rango del contrato, una moneda que no esta en el enum.
     *   422 -> se entendio perfectamente, pero incumple una regla de NEGOCIO. Cada campo
     *          es valido por separado; lo que no se puede procesar es la combinacion.
     *
     * Es opinable, y hay equipos que usan 400 para ambos. Lo que si esta mal es no tener
     * criterio y que cada endpoint responda distinto. Elige uno, aplicalo parejo y
     * escribelo en tu README.
     *
     * Lo que nunca es aceptable: un 200 OK con {"success": false} en el cuerpo.
     */
    @ExceptionHandler(MontoPorPersonaInsuficienteException.class)
    public ResponseEntity<ErrorResource> manejarMontoInsuficiente(
            final MontoPorPersonaInsuficienteException ex) {

        // Se registran los datos del caso, no la excepcion completa: un stack trace por
        // cada request mal formado llena los logs de ruido.
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
