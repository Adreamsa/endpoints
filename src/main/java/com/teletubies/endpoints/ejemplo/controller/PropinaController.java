package com.teletubies.endpoints.ejemplo.controller;

import com.teletubies.endpoints.ejemplo.dto.CalcularPropinaRequest;
import com.teletubies.endpoints.ejemplo.dto.CalcularPropinaResponse;
import com.teletubies.endpoints.ejemplo.dto.MonedaSoportadaResponse;
import com.teletubies.endpoints.ejemplo.service.PropinaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Capa de controlador del ejemplo: unico punto que sabe de HTTP.
 *
 * Responsabilidades que SI le tocan:
 *  - Mapear ruta y verbo.
 *  - Disparar la validacion del cuerpo con @Valid.
 *  - Elegir el codigo de estado de la respuesta exitosa.
 *
 * Responsabilidades que NO le tocan:
 *  - Calcular. Si ves aritmetica o un if de negocio en un controller, va en el servicio.
 *
 * Este controller es deliberadamente delgado: recibe, delega, responde. Esa delgadez es
 * lo que hace que el servicio se pueda testear sin levantar el contexto web.
 *
 * ---------------------------------------------------------------------------------
 * LOMBOK
 *
 * @RequiredArgsConstructor genera, en tiempo de compilacion, un constructor con todos
 * los campos `final` (y los `@NonNull`) de la clase. Es decir, escribe por ti esto:
 *
 *     public PropinaController(PropinaService propinaService) {
 *         this.propinaService = propinaService;
 *     }
 *
 * Y ese constructor es exactamente el que Spring usa para inyectar. La inyeccion por
 * constructor sigue siendo la forma correcta; Lombok solo te ahorra teclearla.
 *
 * Por que por constructor y no con @Autowired sobre el campo:
 *  - La dependencia queda `final`: inmutable, imposible de reasignar por accidente.
 *  - La clase no se puede construir en un estado invalido.
 *  - En un test unitario le pasas un doble con `new`, sin necesitar Spring.
 * Con un solo constructor, Spring lo detecta solo: no hace falta anotarlo.
 *
 * Cuidado con el orden: @RequiredArgsConstructor usa el ORDEN DE DECLARACION de los
 * campos. Si algun dia tienes dos dependencias del mismo tipo y reordenas los campos,
 * cambias silenciosamente el significado del constructor.
 *
 * @Slf4j genera el logger estatico:
 *
 *     private static final Logger log = LoggerFactory.getLogger(PropinaController.class);
 *
 * Te da el campo `log` sin copiar/pegar mal el nombre de la clase, que es el error
 * clasico cuando se declara a mano (y que hace que los logs salgan atribuidos a otra
 * clase, volviendolos inutiles justo cuando los necesitas).
 * ---------------------------------------------------------------------------------
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/ejemplo")
public class PropinaController {

    /**
     * `final` no es decoracion: es lo que hace que @RequiredArgsConstructor lo incluya
     * en el constructor generado. Un campo sin `final` no se inyecta y te llega null.
     */
    private final PropinaService propinaService;

    /**
     * GET del catalogo.
     *
     * 200 OK con la lista. Una lista vacia seguiria siendo 200: "no hay monedas" es una
     * respuesta valida a una pregunta bien hecha, no un error. El 404 se reserva para
     * cuando el RECURSO pedido no existe, no para cuando la coleccion viene vacia.
     */
    @GetMapping("/monedas")
    public ResponseEntity<List<MonedaSoportadaResponse>> listarMonedas() {
        List<MonedaSoportadaResponse> monedas = propinaService.obtenerMonedasSoportadas();

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
     * @Valid es lo que dispara Bean Validation sobre el request. Sin esa anotacion las
     * restricciones del DTO se ignoran en silencio y el cuerpo invalido llega al servicio.
     * Es el error mas comun del ejercicio: anotar el DTO y olvidar el @Valid.
     *
     * Se responde 200 OK y no 201 Created a proposito: este POST no crea ningun recurso,
     * solo calcula sobre lo que le mandaron. 201 obligaria a devolver un Location
     * apuntando al recurso creado, y aqui no hay ninguno que apuntar.
     */
    @PostMapping("/propinas")
    public ResponseEntity<CalcularPropinaResponse> calcularPropina(
            @Valid @RequestBody CalcularPropinaRequest request) {

        // Si esta linea se ejecuta, el request YA paso Bean Validation.
        log.debug("Calculando propina para moneda={} personas={}",
                request.moneda(), request.numeroPersonas());

        CalcularPropinaResponse respuesta = propinaService.calcular(request);
        return ResponseEntity.status(HttpStatus.OK).body(respuesta);
    }
}
