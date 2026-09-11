package com.iwa.curso.service;

import com.iwa.curso.model.QuoteRequest;
import com.iwa.curso.model.QuoteResponse;
import org.springframework.stereotype.Service;

@Service
public class ShipmentService {

    public QuoteResponse calculateQuote(QuoteRequest request) {

        double weight = request.getWeight();

        double estimatedCost;

        if (weight <= 50) {
            estimatedCost = 120.0;
        } else {
            estimatedCost = 180.0;
        }

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