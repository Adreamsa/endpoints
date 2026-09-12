package com.teletubies.endpoints.cotizacion.service;

import com.teletubies.endpoints.model.QuoteRequest;
import com.teletubies.endpoints.model.QuoteResponse;
import org.springframework.stereotype.Service;

@Service
public class ShipmentService {

    public QuoteResponse calculateQuote(QuoteRequest request) {

        double weight = request.getWeight();

        double estimatedCost;

        if (weight <= 50) {
            estimatedCost = 120.0;
        } else if (weight <= 70){
            estimatedCost = 180.0;
        }else
            throw new IllegalArgumentException("El peso máximo permitido es de 70 kg");


        QuoteResponse response = new QuoteResponse();

        response.setOrigin(request.getOrigin());
        response.setDestination(request.getDestination());
        response.setWeight(weight);
        response.setEstimatedCost(estimatedCost);
        response.setEstimatedDeliveryDays(2);
        response.setZone("REGIONAL");

        return response;
    }
}