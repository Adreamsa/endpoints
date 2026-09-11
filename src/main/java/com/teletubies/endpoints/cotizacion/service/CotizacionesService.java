package com.teletubies.endpoints.cotizacion.service;


import com.teletubies.endpoints.cotizacion.dto.CotizacionesRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class CotizacionesService {



    private String envios;
    private final Map<String, Double>tarifas = Map.of(
            "LOCAL", 80.0,
            "NACIONAL", 100.0,
            "INTERNACIONAL", 150.0
    );



    public Double CotizacinesRequest(CotizacionesRequest request){

        String destino = request.getZona().getValue();
        Double tarifa = tarifas.get(destino);
        return tarifa != null ? tarifa : 0.0;



        if (destino == null) {
            System.out.println("Destino no válido");
            return 0.0;
        }
        if (destino== "LOCAL") {
            return tarifas.get("LOCAL");
        } else if (destino == "NACIONAL") {
            return tarifas.get("NACIONAL");
        } else if (destino == "INTERNACIONAL") {
            return tarifas.get("INTERNACIONAL");
        } else {
            System.out.println("Destino no válido");
            return 0.0;
        }

        double peso = request.getpesoKG();
        double costoEstimado = tarifa * peso;
        double costoTotal = costoEstimado + 50; // Agregar un costo fijo de 50
        return costoTotal;

        if (peso <= 0) {
            System.out.println("Peso no válido");
            return 0.0;
        }
        if(peso > 50) {
            System.out.println("Peso valido");
            return costoTotal;

        }
        if (peso > 50 && peso < 70) {
            System.out.println("Peso valido con costo adicional");
            return costoTotal + 20; // Agregar un costo adicional de 20
        }
        if (peso > 70) {
            System.out.println("Peso no válido");
            return 0.0;
    }




}
