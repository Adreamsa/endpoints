# API de Cotización de Envíos

Proyecto base para el ejercicio. El enunciado completo está en [`docs/ENUNCIADO.md`](docs/ENUNCIADO.md).

---

## Cómo correr

Requiere **JDK 21**.

```bash
make build   # compila y empaqueta el jar en target/ (incluye tests)
make run     # levanta la aplicación en http://localhost:8080 (Ctrl+C para detener)
make test    # corre la suite de tests
make clean   # borra target/
make         # muestra la ayuda
```

**¿No tienes `make`?** Cada target es una sola línea de Maven, así que puedes correr el
equivalente directo:

| Target | Linux / macOS / Git Bash | Windows (CMD / PowerShell) |
|---|---|---|
| `make build` | `./mvnw clean install` | `mvnw.cmd clean install` |
| `make run` | `./mvnw spring-boot:run` | `mvnw.cmd spring-boot:run` |
| `make test` | `./mvnw test` | `mvnw.cmd test` |
| `make clean` | `./mvnw clean` | `mvnw.cmd clean` |

El proyecto incluye el **Maven wrapper** (`mvnw` / `mvnw.cmd`): esos comandos funcionan sin
tener Maven instalado, porque el wrapper descarga la versión correcta la primera vez.
En Windows el `Makefile` sí usa el `mvn` del sistema, así que si vas por la vía de `make`
necesitas Maven instalado; si no lo tienes, usa la columna del wrapper.

Para probar los endpoints sin cliente HTTP externo, hay peticiones listas en
[`docs/peticiones.http`](docs/peticiones.http) (IntelliJ IDEA y VS Code con la extensión
REST Client las ejecutan con un clic).

---

## Estructura del proyecto

```
src/main/java/com/teletubies/endpoints/
├── EndpointsApplication.java
│
├── ejemplo/                    ← EJEMPLO DE REFERENCIA. Léelo, y luego BÓRRALO.
│   ├── Moneda.java                 catálogo modelado como enum
│   ├── controller/                 capa web: rutas, @Valid, códigos HTTP
│   ├── service/                    capa de negocio: cálculo y reglas, SIN estado
│   ├── dto/                        contratos de entrada y salida
│   └── exception/                  excepción de negocio + su traducción a HTTP
│
└── cotizacion/                 ← AQUÍ VA TU SOLUCIÓN
    ├── controller/
    ├── service/
    ├── dto/
    └── exception/
```

El paquete `ejemplo` implementa una **calculadora de propinas**: un `POST` que calcula y un
`GET` que expone un catálogo. Es deliberadamente la misma *forma* que el ejercicio pero de
otro dominio, para que veas cómo se reparten las responsabilidades entre capas sin darte
resuelta ninguna decisión de las que se evalúan.

> **Bórralo antes de entregar** (el paquete `ejemplo` y su carpeta de tests). El límite de
> **2 endpoints** aplica a tu API de cotización; los del ejemplo no cuentan, pero tampoco
> deben quedarse en la entrega.

### Qué mirar en el ejemplo

| Archivo | Qué te enseña |
|---|---|
| `dto/CalcularPropinaRequest.java` | Bean Validation más allá de `@NotNull`: rangos, escala, mensajes propios, y por qué los numéricos van como objeto y no como primitivo |
| `service/PropinaService.java` | Dónde vive la lógica de negocio, por qué la capa no conoce HTTP, y la diferencia entre validar *formato* y validar *negocio* |
| `controller/PropinaController.java` | `@Valid`, inyección por constructor, y por qué el `POST` responde `200` y no `201` |
| `exception/EjemploExceptionHandler.java` | El mecanismo `@RestControllerAdvice` y el criterio `400` vs `422`. **Lee la advertencia del inicio del archivo** |
| `controller/PropinaController.java` (bloque LOMBOK) | Qué genera realmente `@RequiredArgsConstructor` y `@Slf4j`, y por qué `final` no es opcional |
| `PropinaServiceTest` / `PropinaControllerTest` | Probar negocio sin Spring, y probar el contrato REST con `@WebMvcTest` |

---

---

## Lombok

El proyecto incluye [Lombok](https://projectlombok.org/), que genera código repetitivo
**en tiempo de compilación**. Las dos anotaciones que vas a usar:

| Anotación | Qué genera | Dónde va |
|---|---|---|
| `@RequiredArgsConstructor` | Un constructor con todos los campos `final` — el que Spring usa para inyectar | Clases con dependencias: controllers, services |
| `@Slf4j` | El campo `private static final Logger log` ya apuntando a esa clase | Cualquier clase donde vayas a registrar algo |

```java
@Slf4j
@RestController
@RequiredArgsConstructor
public class MiController {

    private final MiService miService;   // <- el `final` es lo que lo mete al constructor

    @GetMapping("/algo")
    public ResponseEntity<?> algo() {
        log.debug("parametro recibido: {}", valor);   // {} en vez de concatenar con +
        return ResponseEntity.ok(miService.hacerAlgo());
    }
}
```

**Antes de reportar que "Lombok no funciona":** tu IDE necesita *annotation processing*
habilitado, o marcará como error cosas que sí compilan desde Maven.

- **IntelliJ IDEA** — `Settings → Build, Execution, Deployment → Compiler → Annotation Processors`
  → marcar *Enable annotation processing*. Con el plugin de Lombok instalado.
- **VS Code** — la extensión *Lombok Annotations Support for VS Code*, o el Extension Pack for Java reciente.
- **Eclipse** — ejecutar el instalador del jar de Lombok una vez.

Si `./mvnw test` pasa pero tu IDE muestra errores rojos, el problema es el IDE, no el código.

### Dónde NO ponerla

Los DTOs del ejemplo son `record` y **no llevan Lombok a propósito**: un record ya trae
constructor, getters, `equals`, `hashCode` y `toString` generados por el lenguaje.
`@Data` encima de un record ni siquiera compila. Lombok cubre el boilerplate que Java no
resuelve solo; donde el lenguaje ya lo cubre, gana el lenguaje.

Tampoco anotes por costumbre: `@RequiredArgsConstructor` en una clase sin campos `final`
no genera nada y confunde a quien la lee.

---

## Tu entrega empieza aquí

Todo lo que sigue está en blanco **a propósito**. No es relleno: es la parte del ejercicio
donde se mide tu criterio. Una solución que funciona pero no documenta sus decisiones vale
menos que una que elige distinto y explica por qué.

Borra las instrucciones en cursiva conforme las vayas contestando.

### Decisiones de diseño

*¿Cómo estructuraste la respuesta exitosa de cada endpoint? ¿Por qué esos campos?*

### Supuestos asumidos

*El enunciado deja cosas sin especificar a propósito. Lista lo que tuviste que decidir tú y
con qué criterio. Por ejemplo: ¿origen y destino son texto libre o códigos de un catálogo?
¿el peso admite decimales? ¿qué pasa con un peso de 0 o negativo? ¿origen y destino pueden
ser iguales?*

### Tabla de tarifas

*Tus zonas, tus costos base, tus tiempos de entrega, y el criterio con el que elegiste esos
números. Los valores son libres; lo que se evalúa es que exista un criterio y esté escrito.*

| Zona | Costo base | Tiempo de entrega | Criterio |
|---|---|---|---|
|  |  |  |  |

### Contrato de errores

*¿Qué forma tiene el cuerpo de un error? ¿Es la misma en los dos endpoints? ¿Cómo reportas
varios errores de validación a la vez? ¿Qué código HTTP usaste para cada situación y por qué?*

> **Dato para arrancar:** si no haces nada, un `@Valid` que falla responde `400` con un cuerpo
> por defecto de Spring que **no dice qué campo falló**:
> `{"timestamp":"...","status":400,"error":"Bad Request","path":"/..."}`.
> Ese cuerpo es técnicamente correcto e inútil para quien consume tu API. Qué hacer al
> respecto es tuyo: puedes dejarlo así y justificarlo, o diseñar algo mejor.

| Situación | Código HTTP | Por qué |
|---|---|---|
|  |  |  |

### Inconsistencias detectadas en el enunciado

*¿Encontraste algo en `docs/ENUNCIADO.md` que se contradiga, o reglas que no puedan cumplirse
las dos a la vez? Descríbelo, di qué interpretación elegiste y por qué.*

*Esta sección se evalúa. Dejarla vacía porque "no vi nada" es una respuesta válida sólo si de
verdad revisaste; pero si había algo, el silencio cuenta en contra más que haber elegido la
interpretación "equivocada". No se penaliza interpretar de una forma u otra: se penaliza no
declarar que hubo una interpretación.*

### Endpoints implementados

| Método | Ruta | Descripción |
|---|---|---|
|  |  |  |
