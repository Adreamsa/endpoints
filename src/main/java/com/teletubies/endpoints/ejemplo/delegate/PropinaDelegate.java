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
 * Capa web del ejemplo. Sustituye al @RestController de toda la vida.
 *
 * PropinaApiDelegate no existe en el repositorio: lo genera openapi-generator en
 * target/generated-sources/openapi. Por cada tag del contrato salen tres archivos; para
 * el tag `propina`:
 *
 *   PropinaApi            la interfaz con @RequestMapping y @Valid
 *   PropinaApiController  el @RestController de verdad, que solo delega
 *   PropinaApiDelegate    la interfaz que implementas TU, justo esta clase
 *
 * Ese rodeo permite regenerar el contrato sin pisar tu codigo. Los metodos del delegate
 * traen una implementacion `default` que responde 501, asi que un contrato sin
 * implementar compila y arranca.
 *
 * Aqui SI se decide el codigo de estado y se delega. Aqui NO se calcula (eso es del
 * service) ni se valida el formato (eso ya lo hizo el contrato).
 *
 * Es un `record`, asi que el constructor que Spring usa para inyectar lo escribe el
 * lenguaje y no hace falta @RequiredArgsConstructor. @Slf4j si, porque el logger no lo
 * genera el record.
 *
 * Convencion del proyecto: parametros y variables locales van `final`. No cambia lo que
 * hace el codigo; declara que ese nombre apunta a lo mismo de principio a fin.
 */
@Slf4j
@Service
public record PropinaDelegate(PropinaService propinaService) implements PropinaApiDelegate {

    /**
     * 200 con la lista. Una lista vacia seguiria siendo 200: el 404 se reserva para
     * cuando el RECURSO pedido no existe, no para una coleccion vacia.
     */
    @Override
    public ResponseEntity<List<MonedaResource>> listarMonedas() {
        final List<MonedaResource> monedas = propinaService.obtenerMonedasSoportadas();

        // Las llaves {} son marcadores de SLF4J: el mensaje solo se arma si el nivel
        // esta activo. Con concatenacion el String se construye siempre.
        log.debug("Catalogo de monedas solicitado, {} elementos", monedas.size());

        return ResponseEntity.ok(monedas);
    }

    /**
     * 200 y no 201 a proposito: este POST no crea ningun recurso, solo calcula. Un 201
     * obligaria a devolver un Location, y aqui no hay a que apuntar.
     */
    @Override
    public ResponseEntity<PropinaResource> calcularPropina(final CalcularPropinaRequest request) {
        log.debug("Calculando propina para moneda={} personas={}",
                request.getMoneda(), request.getNumeroPersonas());

        return ResponseEntity.status(HttpStatus.OK).body(propinaService.calcular(request));
    }
}
