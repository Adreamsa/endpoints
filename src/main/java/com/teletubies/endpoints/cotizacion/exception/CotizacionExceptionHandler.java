package com.teletubies.endpoints.cotizacion.exception;

import com.teletubies.endpoints.api.CotizacionApiController;
import com.teletubies.endpoints.model.ErrorDetalleResource;
import com.teletubies.endpoints.model.ErrorResource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.OffsetDateTime;
import java.util.List;

@Slf4j
@RestControllerAdvice(assignableTypes = CotizacionApiController.class)
public class CotizacionExceptionHandler {

    @ExceptionHandler(SobrepesoException.class)
    public ResponseEntity<ErrorResource> manejarSobrepeso(final SobrepesoException ex) {
        log.warn("Envio rechazado por sobrepeso: {} kg", ex.getPesoKg());

        final ErrorResource cuerpo = ErrorResource.builder()
                .timestamp(OffsetDateTime.now())
                .status(HttpStatus.UNPROCESSABLE_ENTITY.value())
                .codigo("SOBREPESO")
                .mensaje(ex.getMessage())
                .build();

        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(cuerpo);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResource> manejarValidacion(final MethodArgumentNotValidException ex) {
        final List<ErrorDetalleResource> detalles = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fe -> ErrorDetalleResource.builder()
                        .campo(fe.getField())
                        .mensaje(fe.getDefaultMessage())
                        .build())
                .toList();

        log.warn("Peticion rechazada por validacion: {} error(es)", detalles.size());

        final ErrorResource cuerpo = ErrorResource.builder()
                .timestamp(OffsetDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .codigo("VALIDACION_FALLIDA")
                .mensaje("La peticion tiene campos invalidos")
                .detalles(detalles)
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(cuerpo);
    }
}
