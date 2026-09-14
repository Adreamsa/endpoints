package com.teletubies.endpoints.cotizacion.delegate;

import com.teletubies.endpoints.api.CotizacionApiDelegate;
import com.teletubies.endpoints.cotizacion.service.CotizacionService;
import com.teletubies.endpoints.model.CotizacionResource;
import com.teletubies.endpoints.model.CotizarEnvioRequest;
import com.teletubies.endpoints.model.ZonaResource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public record CotizacionDelegate(CotizacionService cotizacionService) implements CotizacionApiDelegate {

    @Override
    public ResponseEntity<List<ZonaResource>> listarZonas() {
        final List<ZonaResource> zonas = cotizacionService.obtenerZonasSoportadas();
        log.debug("Catalogo de zonas solicitado, {} elementos", zonas.size());
        return ResponseEntity.ok(zonas);
    }

    @Override
    public ResponseEntity<CotizacionResource> cotizarEnvio(final CotizarEnvioRequest request) {
        log.debug("Cotizando envio: origen={} destino={} pesoKg={}",
                request.getOrigen(), request.getDestino(), request.getPesoKg());
        return ResponseEntity.ok(cotizacionService.cotizar(request));
    }
}
