package com.teletubies.endpoints.cotizacion.controller;

import com.teletubies.endpoints.cotizacion.dto.CotizacionesRequest;
import com.teletubies.endpoints.cotizacion.dto.CotizacionesResponso;
import com.teletubies.endpoints.cotizacion.service.CotizacionesService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class CotizacionesController {
    private final CotizacionesService cotizacionesService;
    public CotizacionesController(CotizacionesService cotizacionesService) {
        this.cotizacionesService = cotizacionesService;
    }
    @PostMapping("/cotizacion")
    public CotizacionesResponso obtenerCotizacion(@Valid @RequestBody CotizacionesRequest request)
    { return cotizacionesService.cotizar(request); }
}