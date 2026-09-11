package com.teletubies.endpoints.ejemplo;

import com.teletubies.endpoints.api.PropinaApiController;
import com.teletubies.endpoints.ejemplo.delegate.PropinaDelegate;
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
 * Test de la capa web: verifica el CONTRATO REST, no la aritmetica. Status codes, forma
 * del JSON, y que las validaciones se disparen.
 *
 * @WebMvcTest levanta solo la porcion web del contexto. Fijate en QUE clase se prueba:
 * PropinaApiController es codigo generado y es el que publica las rutas; tu
 * PropinaDelegate va en el @Import, porque sin el el controller no sirve de nada.
 *
 * Delegate y servicio reales, no mocks: no tienen dependencias y son deterministicos.
 */
@WebMvcTest(PropinaApiController.class)
@Import({PropinaDelegate.class, PropinaService.class, EjemploExceptionHandler.class})
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
        final String cuerpo = """
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
    @DisplayName("POST con campo obligatorio ausente responde 400")
    void campoAusenteResponde400() throws Exception {
        // Falta montoCuenta: lo detiene el @NotNull que salio del `required` del YAML.
        final String cuerpo = """
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
        // Viene completo y es un numero: lo que falla es el RANGO (minimum/maximum).
        // Con solo `required`, este caso se colaria hasta el servicio.
        final String cuerpo = """
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
    @DisplayName("POST con moneda fuera del enum del contrato responde 400")
    void monedaFueraDelEnumResponde400() throws Exception {
        // Al ser un enum del contrato, un valor inventado ni siquiera deserializa.
        final String cuerpo = """
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
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST que incumple una regla de negocio responde 422")
    void reglaDeNegocioResponde422() throws Exception {
        // Cada campo respeta el contrato; lo que no se puede procesar es la COMBINACION,
        // y eso solo lo sabe el servicio. Por eso 422 y no 400.
        final String cuerpo = """
                {
                  "montoCuenta": 0.01,
                  "porcentajePropina": 0,
                  "numeroPersonas": 100,
                  "moneda": "MXN"
                }
                """;

        mockMvc.perform(post("/api/v1/ejemplo/propinas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.codigo").value("MONTO_POR_PERSONA_INSUFICIENTE"));
    }
}
