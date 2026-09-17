package com.teletubies.endpoints.cotizacion.service;


import com.teletubies.endpoints.cotizacion.dto.CotizacionesRequest;
import com.teletubies.endpoints.cotizacion.dto.CotizacionesResponso;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


@Slf4j
@Service
public class CotizacionesService {

    private static final double COSTO_FIJO = 50.0;
    private static final double COSTO_ADICIONAL = 20.0;
    private static final double PESO_LIMITE = 50.0;
    public CotizacionesResponso cotizar(CotizacionesRequest request) {
        double peso = request.pesoKg();
        double tarifa = request.zona().getTarifa();
        double costoEstimado = tarifa * peso;
        double costoTotal = costoEstimado + COSTO_FIJO;
        if (peso > PESO_LIMITE) {
            costoTotal += COSTO_ADICIONAL;
        }
        costoTotal = Math.round(costoTotal * 100.0) / 100.0;

        log.info(
                "Cotización calculada. Zona: {}, Peso: {}, Costo: {}",
                request.zona(),
                peso,
                costoTotal );

        return new CotizacionesResponso(
                request.nombre(),
                request.zona().getValue(),
                tarifa,
                peso,
                costoEstimado,
                costoTotal );
    }



}
