package com.teletubies.endpoints.cotizacion.dto;

import com.teletubies.endpoints.cotizacion.enums.Zona;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CotizacionesRequest(
        @NotBlank String nombre,
        @NotNull Zona zona,
        @Min(value = 0, message = "El peso debe ser mayor a 0")
        @Max(value = 70, message = "El peso no puede ser mayor a 70 kg")
        double pesoKg
){
    public CotizacionesRequest {
        if (zona == null || zona.getValue() == null) {
            throw new IllegalArgumentException("La zona especificada es obligatoria y debe ser válida.");
        }
    }
}