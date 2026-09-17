package com.teletubies.endpoints.cotizacion.service;


import com.teletubies.endpoints.cotizacion.dto.CotizacionesRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


@Slf4j
@Service
public class CotizacionesService {

    private static final double COSTO_FIJO = 50.0;
    private static final double COSTO_ADICIONAL = 20.0;
    private static final double PESO_MAXIMO = 70.0;
    private static final double PESO_LIMITE = 50.0;

    public Double cotizar(CotizacionesRequest request) {

        double peso = request.pesoKg();
        double tarifa = request.zona().getTarifa();

        if (peso <= 0) {
            throw new IllegalArgumentException("El peso debe ser mayor a 0");
        }

        if (peso > PESO_MAXIMO) {
            throw new IllegalArgumentException(
                    "El peso no puede ser mayor a " + PESO_MAXIMO + " kg"
            );
        }

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
                costoTotal
        );

        return costoTotal;
    }




}
