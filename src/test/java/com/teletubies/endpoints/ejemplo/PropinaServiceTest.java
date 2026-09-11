package com.teletubies.endpoints.ejemplo;

import com.teletubies.endpoints.ejemplo.exception.MontoPorPersonaInsuficienteException;
import com.teletubies.endpoints.ejemplo.service.PropinaService;
import com.teletubies.endpoints.model.CalcularPropinaRequest;
import com.teletubies.endpoints.ejemplo.enums.Moneda;
import com.teletubies.endpoints.model.PropinaResource;
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
 * Aqui se prueba la LOGICA DE NEGOCIO, no los codigos HTTP. Que los modelos vengan del
 * contrato no cambia nada en este nivel: se construyen con su builder y se prueban igual.
 */
class PropinaServiceTest {

    private final PropinaService servicio = new PropinaService();

    @Test
    @DisplayName("Calcula propina, total y reparto entre personas")
    void calculaPropinaYReparto() {
        final CalcularPropinaRequest request = CalcularPropinaRequest.builder()
                .montoCuenta(new BigDecimal("100.00"))
                .porcentajePropina(15)
                .numeroPersonas(2)
                .moneda(Moneda.MXN)
                .build();

        final PropinaResource respuesta = servicio.calcular(request);

        // isEqualByComparingTo en vez de isEqualTo: para BigDecimal, 15.00 y 15.0 son
        // "iguales" en valor pero distintos en escala. Comparar por valor evita falsos
        // negativos.
        assertThat(respuesta.getMontoPropina()).isEqualByComparingTo("15.00");
        assertThat(respuesta.getTotal()).isEqualByComparingTo("115.00");
        assertThat(respuesta.getTotalPorPersona()).isEqualByComparingTo("57.50");
        assertThat(respuesta.getMoneda()).isEqualTo(Moneda.MXN);
    }

    @Test
    @DisplayName("Rechaza un reparto que deja a cada persona bajo la unidad minima")
    void rechazaRepartoImposible() {
        // Cada campo es valido por separado; lo invalido es la combinacion. Por eso esta
        // regla no se puede declarar en el contrato y vive aqui.
        final CalcularPropinaRequest request = CalcularPropinaRequest.builder()
                .montoCuenta(new BigDecimal("0.01"))
                .porcentajePropina(0)
                .numeroPersonas(100)
                .moneda(Moneda.MXN)
                .build();

        assertThatThrownBy(() -> servicio.calcular(request))
                .isInstanceOf(MontoPorPersonaInsuficienteException.class);
    }

    @Test
    @DisplayName("El catalogo expone todas las monedas del contrato con descripcion")
    void exponeElCatalogoCompleto() {
        // Se compara contra Moneda.values(), que genera el contrato: si alguien agrega una
        // moneda en enum.yaml y olvida su descripcion, este test lo detecta.
        assertThat(servicio.obtenerMonedasSoportadas())
                .hasSize(Moneda.values().length)
                .allSatisfy(moneda -> {
                    assertThat(moneda.getCodigo()).isNotNull();
                    assertThat(moneda.getDescripcion()).isNotBlank();
                });
    }
}
