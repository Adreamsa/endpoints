package com.teletubies.endpoints.cotizacion.dto;

import com.teletubies.endpoints.cotizacion.enums.Zona;
import jakarta.validation.constraints.*;

public record CotizacionesRequest(
        @NotBlank String nombre,
        @NotNull(message = "La zona es obligatoria") Zona zona,
        @DecimalMin(value = "0.01", message = "El peso debe ser mayor a 0")
        @Max(value = 70, message = "El peso no puede ser mayor a 70 kg")
        double pesoKg
){
}