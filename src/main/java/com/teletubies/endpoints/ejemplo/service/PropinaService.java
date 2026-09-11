package com.teletubies.endpoints.ejemplo.service;

import com.teletubies.endpoints.ejemplo.enums.Moneda;
import com.teletubies.endpoints.ejemplo.dto.CalcularPropinaRequest;
import com.teletubies.endpoints.ejemplo.dto.CalcularPropinaResponse;
import com.teletubies.endpoints.ejemplo.dto.MonedaSoportadaResponse;
import com.teletubies.endpoints.ejemplo.exception.MonedaNoSoportadaException;
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
 *  - Recibe y devuelve DTOs ya validados. Las validaciones de FORMATO (no nulo, rango,
 *    tipo) las resolvio Bean Validation antes de llegar aqui. Lo que se valida en este
 *    punto son las reglas que requieren conocer el negocio: "esta moneda existe?".
 *
 * Esa division es justo lo que se evalua. Un @NotNull no es una regla de negocio, y una
 * regla de negocio no se puede expresar con una anotacion en el DTO.
 *
 * Nota sobre Lombok: aqui hay @Slf4j pero NO @RequiredArgsConstructor, porque esta clase
 * no tiene dependencias que inyectar. Lombok se pone donde ahorra codigo real, no como
 * decoracion: anotar por costumbre una clase sin campos final no genera nada y solo
 * confunde a quien la lee esperando encontrar un constructor.
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

    /**
     * Devuelve el catalogo de monedas soportadas.
     *
     * Este metodo es la razon de existir del endpoint GET: el cliente necesita saber que
     * valores puede mandar ANTES de intentar el POST y comerse un error.
     */
    public List<MonedaSoportadaResponse> obtenerMonedasSoportadas() {
        return Arrays.stream(Moneda.values())
                .map(moneda -> new MonedaSoportadaResponse(moneda.name(), moneda.getDescripcion()))
                .toList();
    }

    /**
     * Calcula la propina.
     *
     * @throws MonedaNoSoportadaException si el codigo de moneda no esta en el catalogo.
     */
    public CalcularPropinaResponse calcular(CalcularPropinaRequest request) {
        // Regla de negocio: la moneda debe existir en el catalogo.
        // Bean Validation solo pudo garantizar que el string no venia vacio; saber si
        // "XYZ" es una moneda valida requiere consultar el dominio, y eso es trabajo
        // de esta capa.
        Moneda moneda = Moneda.desdeCodigo(request.moneda())
                .orElseThrow(() -> {
                    // WARN y no ERROR: que un cliente mande una moneda invalida es un uso
                    // incorrecto de la API, no una falla del sistema. Reservar ERROR para
                    // lo que de verdad esta roto es lo que hace que las alertas sirvan.
                    log.warn("Moneda no soportada recibida: '{}'", request.moneda());
                    return new MonedaNoSoportadaException(request.moneda());
                });

        BigDecimal montoCuenta = request.montoCuenta().setScale(ESCALA_MONETARIA, REDONDEO);

        BigDecimal montoPropina = montoCuenta
                .multiply(BigDecimal.valueOf(request.porcentajePropina()))
                .divide(CIEN, ESCALA_MONETARIA, REDONDEO);

        BigDecimal total = montoCuenta.add(montoPropina);

        BigDecimal totalPorPersona = total
                .divide(BigDecimal.valueOf(request.numeroPersonas()), ESCALA_MONETARIA, REDONDEO);

        return new CalcularPropinaResponse(
                moneda.name(),
                montoCuenta,
                request.porcentajePropina(),
                montoPropina,
                total,
                request.numeroPersonas(),
                totalPorPersona
        );
    }
}
