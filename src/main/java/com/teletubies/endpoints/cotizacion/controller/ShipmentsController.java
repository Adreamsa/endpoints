package com.teletubies.endpoints.cotizacion.controller;


import com.teletubies.endpoints.controller.ShipmentsApi;
import com.teletubies.endpoints.cotizacion.service.ShipmentService;
import com.teletubies.endpoints.model.QuoteRequest;
import com.teletubies.endpoints.model.QuoteResponse;
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