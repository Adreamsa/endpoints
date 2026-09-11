package com.teletubies.endpoints.cotizacion.exception;

import com.teletubies.endpoints.api.CotizacionApiController;
import com.teletubies.endpoints.model.ErrorResource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.OffsetDateTime;

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
}
