package com.iwa.curso.controller;

import com.iwa.curso.model.QuoteRequest;
import com.iwa.curso.model.QuoteResponse;
import com.iwa.curso.service.ShipmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ShipmentsController implements ShipmentsApi {

    private final ShipmentService shipmentService;

    public ShipmentsController(ShipmentService shipmentService) {
        this.shipmentService = shipmentService;
    }

    @Override
    public ResponseEntity<QuoteResponse> quoteShipment(
            QuoteRequest quoteRequest) {

        QuoteResponse response =
                shipmentService.calculateQuote(quoteRequest);

        return ResponseEntity.ok(response);
    }
}