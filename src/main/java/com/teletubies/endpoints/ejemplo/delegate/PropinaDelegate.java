package com.teletubies.endpoints.ejemplo.delegate;

import com.teletubies.endpoints.api.PropinaApiDelegate;
import com.teletubies.endpoints.ejemplo.service.PropinaService;
import com.teletubies.endpoints.model.CalcularPropinaRequest;
import com.teletubies.endpoints.model.MonedaResource;
import com.teletubies.endpoints.model.PropinaResource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Capa web del ejemplo. Este archivo sustituye al viejo @RestController.
 *
 * ---------------------------------------------------------------------------------
 * DE DONDE SALE PropinaApiDelegate
 *
 * No existe en el repositorio: lo genera openapi-generator a partir de open-api/, y
 * aparece en target/generated-sources/openapi. Por cada tag del contrato se generan
 * tres archivos; para el tag `propina`:
 *
 *   PropinaApi            la interfaz con las anotaciones @RequestMapping y @Valid
 *   PropinaApiController  el @RestController de verdad, que solo delega
 *   PropinaApiDelegate    la interfaz que implementas TU, justo esta clase
 *
 * Ese rodeo es el que hace que el contrato se pueda regenerar cuantas veces quieras
 * sin pisar tu codigo: lo generado y lo escrito a mano viven en archivos distintos.
 *
 * Los metodos del delegate vienen con una implementacion `default` que responde
 * 501 NOT_IMPLEMENTED. Es decir, un contrato sin implementar compila y arranca: puedes
 * declarar una ruta hoy y resolverla manana.
 *
 * COMO SE LLAMA TU INTERFAZ: el nombre sale del `tags` de la operacion en openapi.yaml,
 * no de la ruta. Si cambias el tag, cambia el nombre de la interfaz generada.
 * ---------------------------------------------------------------------------------
 *
 * Responsabilidades que SI le tocan a esta capa:
 *  - Elegir el codigo de estado de la respuesta exitosa.
 *  - Delegar en el servicio.
 *
 * Responsabilidades que NO le tocan:
 *  - Calcular. Si ves aritmetica o un if de negocio aqui, va en el servicio.
 *  - Validar el formato de la entrada. De eso ya se encargo el contrato: las
 *    restricciones de requests.yaml se convirtieron en anotaciones sobre el modelo, y
 *    el @Valid lo puso el generador en PropinaApi. Si este metodo se ejecuta, el
 *    request YA paso Bean Validation.
 *
 * Es un `record` y no una clase: las dependencias quedan finales y el constructor que
 * Spring usa para inyectar lo escribe el lenguaje. Por eso aqui no hace falta
 * @RequiredArgsConstructor —el patron que usarias en una clase normal— y solo queda
 * @Slf4j, que sigue siendo util porque el logger no lo genera el record.
 *
 * Convencion del proyecto: los parametros y las variables locales van marcados `final`.
 * No cambia lo que hace el codigo, declara una intencion: ese nombre apunta a lo mismo
 * de principio a fin del metodo. Quien lo lee no tiene que revisar las siguientes veinte
 * lineas para saber si alguien lo reasigno a la mitad, y si alguien lo intenta, el
 * compilador lo detiene.
 */
@Slf4j
@Service
public record PropinaDelegate(PropinaService propinaService) implements PropinaApiDelegate {

    /**
     * GET del catalogo.
     *
     * 200 OK con la lista. Una lista vacia seguiria siendo 200: "no hay monedas" es una
     * respuesta valida a una pregunta bien hecha, no un error. El 404 se reserva para
     * cuando el RECURSO pedido no existe, no para cuando la coleccion viene vacia.
     */
    @Override
    public ResponseEntity<List<MonedaResource>> listarMonedas() {
        final List<MonedaResource> monedas = propinaService.obtenerMonedasSoportadas();

        // Nivel DEBUG: util mientras desarrollas, apagado por defecto en produccion.
        // Las llaves {} son marcadores de posicion de SLF4J: el mensaje solo se arma si
        // el nivel esta activo. Con concatenacion ("total: " + x) el String se construye
        // siempre, aunque nadie lo vaya a leer.
        log.debug("Catalogo de monedas solicitado, {} elementos", monedas.size());

        return ResponseEntity.ok(monedas);
    }

    /**
     * POST del calculo.
     *
     * Se responde 200 OK y no 201 Created a proposito: este POST no crea ningun recurso,
     * solo calcula sobre lo que le mandaron. 201 obligaria a devolver un Location
     * apuntando al recurso creado, y aqui no hay ninguno que apuntar.
     */
    @Override
    public ResponseEntity<PropinaResource> calcularPropina(final CalcularPropinaRequest request) {
        log.debug("Calculando propina para moneda={} personas={}",
                request.getMoneda(), request.getNumeroPersonas());

        return ResponseEntity.status(HttpStatus.OK).body(propinaService.calcular(request));
    }
}
