package com.teletubies.endpoints.cotizacion.controller;

import com.teletubies.endpoints.cotizacion.service.CotizacionesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CotizacionesController {
    @Autowired
    private CotizacionesService cotizacionesService;

    @GetMapping("/cotizacion")
    public Double obtenerCotizacion(@RequestParam String tipoEnvio) {
        return cotizacionesService.obtenerTarifa(tipoEnvio);
    }
}