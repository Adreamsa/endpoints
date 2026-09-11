package com.teletubies.endpoints.cotizacion.dto;

import com.teletubies.endpoints.cotizacion.enums.Zona;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CotizacionesRequest {
    @NotBlank
    private String nombre;

    @NotNull
    private Zona zona;

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Zona getZona() {
        return zona;
    }

    public void setZona(Zona zona) {
        this.zona = zona;
    }
    private double pesoKG;

        public double getpesoKG() {
        return pesoKG;
    }

    public void setpesoKG(double pesoKG) {
        this.pesoKG = pesoKG;
    }
}