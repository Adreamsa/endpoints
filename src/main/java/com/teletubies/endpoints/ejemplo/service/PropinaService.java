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
 * Capa de servicio del ejemplo: aqui vive la logica de negocio.
 *
 * Reglas de la casa para esta capa:
 *  - SIN ESTADO. No hay atributos mutables, no se guarda nada entre llamadas. La clase es
 *    un singleton compartido por todos los hilos; un atributo mutable seria un bug de
 *    concurrencia. Las unicas constantes son static final.
 *  - No conoce HTTP. Aqui no se importa ResponseEntity ni HttpStatus. Si el negocio falla,
 *    se lanza una excepcion de dominio y alguien mas decide el status.
 *  - Recibe y devuelve los modelos que genero el contrato, ya validados. Las validaciones
 *    de FORMATO las resolvio Bean Validation antes de llegar aqui, a partir de lo que
 *    declaraste en requests.yaml.
 *
 * ---------------------------------------------------------------------------------
 * QUE PUEDE Y QUE NO PUEDE VALIDAR EL CONTRATO
 *
 * Lee esto, porque es exactamente la distincion que se evalua en tu ejercicio.
 *
 * El contrato valida un campo a la vez, contra un limite fijo:
 *   "montoCuenta debe ser >= 0.01"            -> minimum, en requests.yaml
 *   "moneda debe ser MXN, USD o EUR"          -> enum, en enum.yaml
 *
 * Fijate en la segunda: al declarar `moneda` como enum en el contrato, un "XYZ" ya ni
 * siquiera llega hasta aqui. Jackson lo rechaza al deserializar y sale un 400. Modelar
 * el catalogo en el contrato te da esa validacion gratis, y de paso documenta los
 * valores validos en Swagger UI.
 *
 * Lo que el contrato NO puede expresar es una regla que dependa de VARIOS campos a la
 * vez, o del resultado de un calculo. Esa es la que se queda aqui abajo, y es la que
 * justifica que exista una capa de servicio.
 * ---------------------------------------------------------------------------------
 */
@Slf4j
@Service
public class PropinaService {

    /**
     * Escala y modo de redondeo fijos para todo el dinero que produce el servicio.
     * Es una decision que vale la pena documentar: sin esto, dos endpoints del mismo
     * sistema pueden redondear distinto y no cuadrar por un centavo.
     */
    private static final int ESCALA_MONETARIA = 2;
    private static final RoundingMode REDONDEO = RoundingMode.HALF_UP;

    private static final BigDecimal CIEN = new BigDecimal("100");

    /** La unidad minima que tiene sentido cobrarle a una persona. */
    private static final BigDecimal MINIMO_POR_PERSONA = new BigDecimal("0.01");

    /**
     * Devuelve el catalogo de monedas soportadas.
     *
     * Este metodo es la razon de existir del endpoint GET: el cliente necesita saber que
     * valores puede mandar ANTES de intentar el POST y comerse un error.
     *
     * Fijate en que no hay ninguna lista de monedas escrita aqui: se recorre el enum, y
     * cada valor ya trae su descripcion. Si agregas una moneda a Moneda.java (y a
     * enum.yaml), aparece en este catalogo sola. Si la etiqueta viviera en un Map aparte,
     * tendrias dos sitios que mantener sincronizados en vez de uno.
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
     * Calcula la propina.
     *
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

        // ESTA es la regla que ninguna anotacion puede expresar.
        //
        // Cada campo por separado es perfectamente valido: 0.01 es un monto legal y 100
        // es un numero de personas legal. Lo invalido es la COMBINACION, y solo se sabe
        // despues de dividir. El contrato no tiene forma de declararlo; el servicio si.
        //
        // Cuando en tu ejercicio te preguntes "esto va en el YAML o en el service?", la
        // pregunta util es: se puede decidir mirando un solo campo contra un limite fijo?
        if (totalPorPersona.compareTo(MINIMO_POR_PERSONA) < 0) {
            // WARN y no ERROR: que un cliente pida un reparto imposible es un uso
            // incorrecto de la API, no una falla del sistema. Reservar ERROR para lo que
            // de verdad esta roto es lo que hace que las alertas sirvan.
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
