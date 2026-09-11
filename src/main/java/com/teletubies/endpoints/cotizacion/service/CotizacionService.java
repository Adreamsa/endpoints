package com.teletubies.endpoints.cotizacion.service;

import com.teletubies.endpoints.cotizacion.enums.Zona;
import com.teletubies.endpoints.cotizacion.exception.SobrepesoException;
import com.teletubies.endpoints.model.CotizacionResource;
import com.teletubies.endpoints.model.CotizarEnvioRequest;
import com.teletubies.endpoints.model.ZonaResource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
public class CotizacionService {

        private static final int ESCALA_MONETARIA = 2;
        private static final RoundingMode REDONDEO = RoundingMode.HALF_UP;
        private static final BigDecimal UMBRAL_SOBREPESO = new BigDecimal("50");
        private static final BigDecimal PESO_MAXIMO = new BigDecimal("70");
        private static final BigDecimal CARGO_POR_KG_EXTRA = new BigDecimal("15.00");

        public CotizacionResource cotizar(final CotizarEnvioRequest request) {
                final BigDecimal pesoKg = request.getPesoKg()
                                .setScale(ESCALA_MONETARIA, REDONDEO);

                if (pesoKg.compareTo(PESO_MAXIMO) > 0) {
                        log.warn("Envio rechazado por sobrepeso: {} kg", pesoKg);
                        throw new SobrepesoException(pesoKg);
                }

                final Zona zona = request.getDestino();

                final BigDecimal cargoSobrepeso = calcularCargoSobrepeso(pesoKg);
                final BigDecimal costoBase = zona.getCostoBase();
                final BigDecimal costoTotal = costoBase.add(cargoSobrepeso)
                                .setScale(ESCALA_MONETARIA, REDONDEO);

                log.debug("Cotizacion: zona={} pesoKg={} costoBase={} cargoSobrepeso={} costoTotal={}",
                                zona, pesoKg, costoBase, cargoSobrepeso, costoTotal);

                return CotizacionResource.builder()
                                .origen(request.getOrigen())
                                .destino(zona)
                                .pesoKg(pesoKg)
                                .costoBase(costoBase)
                                .cargoSobrepeso(cargoSobrepeso)
                                .costoTotal(costoTotal)
                                .diasEntrega(zona.getDiasEntrega())
                                .build();
        }

        public List<ZonaResource> obtenerZonasSoportadas() {
                return Arrays.stream(Zona.values())
                                .map(zona -> ZonaResource.builder()
                                                .codigo(zona)
                                                .descripcion(zona.getDescripcion())
                                                .build())
                                .toList();
        }

        private BigDecimal calcularCargoSobrepeso(final BigDecimal pesoKg) {
                if (pesoKg.compareTo(UMBRAL_SOBREPESO) <= 0) {
                        return BigDecimal.ZERO.setScale(ESCALA_MONETARIA, REDONDEO);
                }
                return pesoKg.subtract(UMBRAL_SOBREPESO)
                                .multiply(CARGO_POR_KG_EXTRA)
                                .setScale(ESCALA_MONETARIA, REDONDEO);
        }
}
