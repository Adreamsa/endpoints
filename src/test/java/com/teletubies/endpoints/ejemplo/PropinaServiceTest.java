package com.teletubies.endpoints.ejemplo;

import com.teletubies.endpoints.ejemplo.dto.CalcularPropinaRequest;
import com.teletubies.endpoints.ejemplo.enums.Moneda;
import com.teletubies.endpoints.ejemplo.dto.CalcularPropinaResponse;
import com.teletubies.endpoints.ejemplo.exception.MonedaNoSoportadaException;
import com.teletubies.endpoints.ejemplo.service.PropinaService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Test de la capa de servicio: JUnit puro, sin Spring.
 *
 * Fijate que se instancia con `new`. No hace falta levantar el contexto porque el servicio
 * no tiene estado ni dependencias: esa es la recompensa de haberlo mantenido limpio de HTTP
 * y de infraestructura. Corre en milisegundos.
 *
 * Aqui se prueba la LOGICA DE NEGOCIO, no los codigos HTTP.
 */
class PropinaServiceTest {

    private final PropinaService servicio = new PropinaService();

    @Test
    @DisplayName("Calcula propina, total y reparto entre personas")
    void calculaPropinaYReparto() {
        var request = new CalcularPropinaRequest(
                new BigDecimal("100.00"), 15, 2, "MXN");

        CalcularPropinaResponse respuesta = servicio.calcular(request);

        // compareTo en vez de isEqualTo: para BigDecimal, 15.00 y 15.0 son "iguales" en
        // valor pero distintos en escala. Comparar por valor evita falsos negativos.
        assertThat(respuesta.montoPropina()).isEqualByComparingTo("15.00");
        assertThat(respuesta.total()).isEqualByComparingTo("115.00");
        assertThat(respuesta.totalPorPersona()).isEqualByComparingTo("57.50");
        assertThat(respuesta.moneda()).isEqualTo("MXN");
    }

    @Test
    @DisplayName("Acepta el codigo de moneda sin importar mayusculas ni espacios")
    void normalizaElCodigoDeMoneda() {
        var request = new CalcularPropinaRequest(
                new BigDecimal("50.00"), 10, 1, "  usd ");

        assertThat(servicio.calcular(request).moneda()).isEqualTo("USD");
    }

    @Test
    @DisplayName("Rechaza una moneda fuera del catalogo con excepcion de negocio")
    void rechazaMonedaNoSoportada() {
        var request = new CalcularPropinaRequest(
                new BigDecimal("50.00"), 10, 1, "XYZ");

        assertThatThrownBy(() -> servicio.calcular(request))
                .isInstanceOf(MonedaNoSoportadaException.class)
                .hasMessageContaining("XYZ");
    }

    @Test
    @DisplayName("El catalogo expone las monedas con codigo y descripcion")
    void exponeElCatalogoCompleto() {
        assertThat(servicio.obtenerMonedasSoportadas())
                .hasSize(Moneda.values().length)
                .allSatisfy(moneda -> {
                    assertThat(moneda.codigo()).isNotBlank();
                    assertThat(moneda.descripcion()).isNotBlank();
                });
    }
}
