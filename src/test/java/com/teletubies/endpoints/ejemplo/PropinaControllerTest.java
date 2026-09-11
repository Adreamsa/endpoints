package com.teletubies.endpoints.ejemplo;

import com.teletubies.endpoints.ejemplo.controller.PropinaController;
import com.teletubies.endpoints.ejemplo.exception.EjemploExceptionHandler;
import com.teletubies.endpoints.ejemplo.service.PropinaService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test de la capa web: verifica el CONTRATO REST, no la aritmetica.
 *
 * @WebMvcTest levanta solo la porcion web del contexto (controllers, validacion,
 * serializacion JSON), no la aplicacion completa. Con @Import se agregan las piezas
 * colaboradoras que si se necesitan.
 *
 * Se usa el servicio real y no un mock porque no tiene dependencias y es deterministico.
 * Mockear aqui solo agregaria ceremonia sin ganar aislamiento.
 *
 * Lo que se prueba en este nivel: status codes, forma del JSON, y que las validaciones
 * realmente se disparen. Justo lo que tu ejercicio tiene que demostrar.
 */
@WebMvcTest(PropinaController.class)
@Import({PropinaService.class, EjemploExceptionHandler.class})
class PropinaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("GET del catalogo responde 200 con la lista de monedas")
    void catalogoResponde200() throws Exception {
        mockMvc.perform(get("/api/v1/ejemplo/monedas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].codigo").exists())
                .andExpect(jsonPath("$[0].descripcion").exists());
    }

    @Test
    @DisplayName("POST valido responde 200 con el desglose del calculo")
    void calculoValidoResponde200() throws Exception {
        String cuerpo = """
                {
                  "montoCuenta": 200.00,
                  "porcentajePropina": 10,
                  "numeroPersonas": 4,
                  "moneda": "MXN"
                }
                """;

        mockMvc.perform(post("/api/v1/ejemplo/propinas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.montoPropina").value(20.00))
                .andExpect(jsonPath("$.total").value(220.00))
                .andExpect(jsonPath("$.totalPorPersona").value(55.00));
    }

    @Test
    @DisplayName("POST con campo obligatorio ausente responde 400 (falla @Valid)")
    void campoAusenteResponde400() throws Exception {
        // Falta montoCuenta: lo detiene Bean Validation, nunca llega al servicio.
        String cuerpo = """
                {
                  "porcentajePropina": 10,
                  "numeroPersonas": 4,
                  "moneda": "MXN"
                }
                """;

        mockMvc.perform(post("/api/v1/ejemplo/propinas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST con valor fuera de rango responde 400")
    void valorFueraDeRangoResponde400() throws Exception {
        // El dato viene completo y es un numero: lo que falla es el RANGO.
        // Si tu validacion solo cubriera @NotNull, este caso se colaria.
        String cuerpo = """
                {
                  "montoCuenta": 200.00,
                  "porcentajePropina": 250,
                  "numeroPersonas": 0,
                  "moneda": "MXN"
                }
                """;

        mockMvc.perform(post("/api/v1/ejemplo/propinas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST con moneda inexistente responde 422 (regla de negocio)")
    void monedaNoSoportadaResponde422() throws Exception {
        // Sintacticamente impecable, semanticamente invalido: por eso 422 y no 400.
        String cuerpo = """
                {
                  "montoCuenta": 200.00,
                  "porcentajePropina": 10,
                  "numeroPersonas": 4,
                  "moneda": "XYZ"
                }
                """;

        mockMvc.perform(post("/api/v1/ejemplo/propinas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.codigo").value("MONEDA_NO_SOPORTADA"));
    }
}
