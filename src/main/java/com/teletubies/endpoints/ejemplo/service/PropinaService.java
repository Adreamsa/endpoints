package com.teletubies.endpoints.ejemplo.service;

import com.teletubies.endpoints.ejemplo.enums.Moneda;
import com.teletubies.endpoints.ejemplo.exception.MontoPorPersonaInsuficienteException;
import com.teletubies.endpoints.model.CalcularPropinaRequest;
import com.teletubies.endpoints.model.MonedaResource;
import com.teletubies.endpoints.model.PropinaResource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;

/**
 * Capa de negocio del ejemplo.
 *
 * Reglas de la casa:
 *  - SIN ESTADO. Es un singleton compartido por todos los hilos; un atributo mutable
 *    seria un bug de concurrencia. Solo constantes static final.
 *  - No conoce HTTP. Aqui no se importa ResponseEntity ni HttpStatus: si el negocio
 *    falla, se lanza una excepcion de dominio y otro decide el status.
 *
 * QUE VALIDA EL CONTRATO Y QUE VALIDA ESTA CAPA (la distincion que se evalua):
 *
 * El contrato valida un campo a la vez contra un limite fijo. "montoCuenta >= 0.01" es
 * un `minimum` en requests.yaml; "moneda debe ser MXN, USD o EUR" es un enum en
 * enum.yaml, y por eso un "XYZ" ni siquiera llega hasta aqui: lo rechaza Jackson con un
 * 400. Modelar el catalogo en el contrato da esa validacion gratis.
 *
 * Lo que el contrato no puede expresar es una regla que dependa de varios campos a la
 * vez, o del resultado de un calculo. Esa se queda aqui, y es lo que justifica la capa.
 */
@Slf4j
@Service
public class PropinaService {

    /**
     * Escala y redondeo fijos para todo el dinero del servicio. Sin esto, dos endpoints
     * del mismo sistema pueden redondear distinto y no cuadrar por un centavo.
     */
    private static final int ESCALA_MONETARIA = 2;
    private static final RoundingMode REDONDEO = RoundingMode.HALF_UP;

    private static final BigDecimal CIEN = new BigDecimal("100");

    /** La unidad minima que tiene sentido cobrarle a una persona. */
    private static final BigDecimal MINIMO_POR_PERSONA = new BigDecimal("0.01");

    /**
     * Catalogo de monedas. No hay ninguna lista escrita aqui: se recorre el enum, y cada
     * valor ya trae su descripcion. Agregar una moneda a Moneda.java (y a enum.yaml) la
     * hace aparecer sola.
     */
    public List<MonedaResource> obtenerMonedasSoportadas() {
        return Arrays.stream(Moneda.values())
                .map(moneda -> MonedaResource.builder()
                        .codigo(moneda)
                        .descripcion(moneda.getDescripcion())
                        .build())
                .toList();
    }

    /**
     * @throws MontoPorPersonaInsuficienteException si el reparto deja a cada persona por
     *         debajo de la unidad minima de la moneda.
     */
    public PropinaResource calcular(final CalcularPropinaRequest request) {
        final BigDecimal montoCuenta = request.getMontoCuenta().setScale(ESCALA_MONETARIA, REDONDEO);

        final BigDecimal montoPropina = montoCuenta
                .multiply(BigDecimal.valueOf(request.getPorcentajePropina()))
                .divide(CIEN, ESCALA_MONETARIA, REDONDEO);

        final BigDecimal total = montoCuenta.add(montoPropina);

        final BigDecimal totalPorPersona = total
                .divide(BigDecimal.valueOf(request.getNumeroPersonas()), ESCALA_MONETARIA, REDONDEO);

        // La regla que ninguna anotacion puede expresar: 0.01 es un monto valido y 100 un
        // numero de personas valido; lo invalido es la combinacion, y solo se sabe tras
        // dividir. Cuando dudes entre YAML y service, preguntate: se puede decidir
        // mirando un solo campo contra un limite fijo?
        if (totalPorPersona.compareTo(MINIMO_POR_PERSONA) < 0) {
            // WARN y no ERROR: es un uso incorrecto de la API, no una falla del sistema.
            log.warn("Reparto imposible: total={} entre {} personas", total, request.getNumeroPersonas());
            throw new MontoPorPersonaInsuficienteException(total, request.getNumeroPersonas());
        }

        return PropinaResource.builder()
                .moneda(request.getMoneda())
                .montoCuenta(montoCuenta)
                .porcentajePropina(request.getPorcentajePropina())
                .montoPropina(montoPropina)
                .total(total)
                .numeroPersonas(request.getNumeroPersonas())
                .totalPorPersona(totalPorPersona)
                .build();
    }
}
