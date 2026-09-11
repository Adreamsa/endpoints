# API de Cotización de Envíos

Proyecto base para el ejercicio. El enunciado completo está en [`docs/ENUNCIADO.md`](docs/ENUNCIADO.md).

---

## Cómo correr

Requiere **JDK 21**.

```bash
make build              # compila y empaqueta el jar en target/ (incluye tests)
make run                # levanta la aplicación en http://localhost:8080 (Ctrl+C para detener)
make test               # corre la suite de tests
make openapi-generate   # regenera el código desde open-api/ sin compilar
make clean              # borra target/
make                    # muestra la ayuda
```

`make build`, `make run` y `make test` ya regeneran el código del contrato por su cuenta.
`make openapi-generate` es para cuando editaste el YAML y quieres ver lo generado de
inmediato, sin esperar a una compilación completa.

**¿No tienes `make`?** Cada target es una sola línea de Maven, así que puedes correr el
equivalente directo:

| Target | Linux / macOS / Git Bash | Windows (CMD / PowerShell) |
|---|---|---|
| `make build` | `./mvnw clean install` | `mvnw.cmd clean install` |
| `make run` | `./mvnw spring-boot:run` | `mvnw.cmd spring-boot:run` |
| `make test` | `./mvnw test` | `mvnw.cmd test` |
| `make openapi-generate` | `./mvnw org.openapitools:openapi-generator-maven-plugin:generate@openapi` | `mvnw.cmd org.openapitools:openapi-generator-maven-plugin:generate@openapi` |
| `make clean` | `./mvnw clean` | `mvnw.cmd clean` |

El proyecto incluye el **Maven wrapper** (`mvnw` / `mvnw.cmd`): esos comandos funcionan sin
tener Maven instalado, porque el wrapper descarga la versión correcta la primera vez.
En Windows el `Makefile` sí usa el `mvn` del sistema, así que si vas por la vía de `make`
necesitas Maven instalado; si no lo tienes, usa la columna del wrapper.

---

## El contrato manda

Este proyecto trabaja **contrato-primero**. No escribes controllers ni DTOs: los declaras
en OpenAPI y los genera Maven.

```
open-api/
├── openapi.yaml     las rutas: verbo, path, tag, y a qué schema apunta cada una
├── requests.yaml    lo que ENTRA
├── resources.yaml   lo que SALE, y el ErrorResource
├── responses.yaml   el envoltorio HTTP de cada respuesta
└── enum.yaml        los catálogos de valores fijos, que comparten entrada y salida
```

Los cinco archivos se enlazan con `$ref`, así que `openapi.yaml` se lee como el índice de
la API y ningún schema se define dos veces.

`enum.yaml` está aparte porque un enum no es ni entrada ni salida: es de los dos lados.
`destino` viaja en la petición y también vuelve en la respuesta, y tiene que ser el mismo
conjunto de valores en ambos casos.

### El ciclo de trabajo

1. Editas el YAML en `open-api/`.
2. Regeneras: `make openapi-generate` (o simplemente `make build`).
3. Implementas el **delegate**, que es la interfaz que el generador dejó para que la llenes.

Por cada `tag` del contrato salen tres archivos en `target/generated-sources/openapi`.
Para el tag `cotizacion`:

| Clase generada | Qué es | ¿La tocas? |
|---|---|---|
| `CotizacionApi` | La interfaz con `@RequestMapping`, `@Valid` y las anotaciones de validación | No |
| `CotizacionApiController` | El `@RestController` de verdad, que sólo delega | No |
| `CotizacionApiDelegate` | La interfaz que implementas **tú** | Es tu punto de entrada |

Ese rodeo es justo lo que hace que el contrato se pueda regenerar cuantas veces quieras sin
pisar tu código: lo generado y lo escrito a mano viven en archivos distintos.

> **Nunca edites lo que está en `target/`.** Se sobrescribe en cada compilación. Si algo del
> código generado no te gusta, se cambia en el YAML.

Los métodos del delegate vienen con una implementación por defecto que responde **501 Not
Implemented**. O sea: un contrato sin implementar compila y arranca. Si pides
`GET /api/v1/zonas` antes de escribir nada, te responde 501 — esa es la señal de que el
contrato está bien y falta tu parte.

### La excepción: un enum puede no generarse

Los schemas de `enum.yaml` se generan como todo lo demás… salvo que le digas al generador
que use una clase **tuya** en su lugar. Eso se hace mapeándolo en el `pom.xml`:

```xml
<importMapping>Moneda=com.teletubies.endpoints.ejemplo.enums.Moneda</importMapping>
<schemaMapping>Moneda=com.teletubies.endpoints.ejemplo.enums.Moneda</schemaMapping>
```

`importMapping` hace que el código generado importe tu clase; `schemaMapping` hace que
además no genere una propia. Hacen falta las dos.

**¿Por qué el rodeo?** Un enum generado sólo puede tener sus constantes. Uno escrito a mano
puede llevar comportamiento. Mira `ejemplo/enums/Moneda.java`: cada valor carga su
descripción legible, que es justo lo que necesita el endpoint de catálogo. Si ese enum fuera
generado, el texto tendría que vivir en un `Map` dentro del servicio, lejos del valor al que
pertenece.

**El precio:** cuando mapeas un enum, `enum.yaml` y la clase Java tienen que decir lo mismo,
y nadie lo verifica por ti. Si agregas un valor en un solo lado, el contrato y el código se
contradicen. Ése es el trueque, y es la razón de que todo lo demás sí se genere.

> **El enum de zonas se llama `ZonaEjemplo`, y el nombre es una advertencia.** Está mapeado
> a `ejemplo/enums/ZonaEjemplo.java`, una clase de demostración que vive en el paquete del
> ejemplo y desaparece cuando lo borres. Gracias a ella el proyecto arranca entero desde el
> primer `make run`, con descripciones incluidas.
>
> **Esas tres zonas no son las del ejercicio.** El enunciado te pide tu propia tabla de
> tarifas, y eso empieza por decidir cuáles son tus zonas. El cambio son cuatro pasos:
> renombra el schema a `Zona` en `enum.yaml` con tus valores, actualiza los tres `$ref` que
> apuntan ahí, escribe tu `cotizacion/enums/Zona.java` copiando la forma de `ZonaEjemplo`, y
> reapunta las dos líneas del `pom.xml`.

### Dónde se valida qué

Las restricciones que escribes en `requests.yaml` se convierten solas en anotaciones de
Bean Validation sobre el modelo generado:

| En el YAML | En el Java generado |
|---|---|
| `required` | `@NotNull` |
| `minLength` / `maxLength` | `@Size` |
| `minimum` / `maximum` | `@DecimalMin` / `@DecimalMax` |
| `pattern` | `@Pattern` |
| `enum` | un `enum` de Java; un valor fuera de la lista ni siquiera deserializa |

El `@Valid` también lo pone el generador. **Si una restricción no está en el YAML, no existe
en tiempo de ejecución.**

Lo que el contrato **no** puede expresar es una regla que dependa de varios campos a la vez,
o del resultado de un cálculo. Ésa vive en tu `service`, y es la que justifica que exista la
capa. Mira `PropinaService` para ver la distinción aplicada.

### Ver el contrato renderizado

Con la aplicación corriendo (`make run`):

- **Swagger UI:** <http://localhost:8080/swagger-ui/index.html>
- **El contrato en JSON:** <http://localhost:8080/v3/api-docs>

No necesitas instalar nada más: sólo JDK 21, igual que antes.

---

## Cómo probar los endpoints

Con la aplicación corriendo (`make run`), los ejemplos de abajo usan los endpoints del
paquete `ejemplo`. Cambia rutas y cuerpos por los tuyos cuando los tengas.

La vía más cómoda es el archivo [`docs/peticiones.http`](docs/peticiones.http): IntelliJ IDEA
y VS Code (con la extensión *REST Client*) ejecutan cada petición con un clic, sin pelearte
con comillas. Si prefieres la terminal, sigue leyendo.

### Trampas de Windows (léelas antes de reportar que "no funciona")

Estas cuatro cosas están verificadas en Windows 11, y son la causa del 90% de los tropiezos:

| Situación | Qué pasa |
|---|---|
| `curl` en **Windows PowerShell 5.1** | Es un **alias de `Invoke-WebRequest`**, no es curl. Tus flags (`-X`, `-d`, `-H`) no significan lo mismo y el comando falla. Escribe `curl.exe` para forzar el curl real |
| `curl` en **PowerShell 7+** | Ahí sí es el curl real (`C:\Windows\System32\curl.exe`). Aun así, escribir `curl.exe` te hace inmune a la versión |
| Comillas del JSON | `cmd.exe` necesita `\"` escapado. **PowerShell no**: ahí el escape `\"` se rompe y el servidor te responde `400`. En PowerShell usa comillas simples envolviendo el JSON |
| `Invoke-RestMethod` con un `4xx` | **Lanza una excepción y nunca ves el cuerpo del error.** Justo lo que necesitas inspeccionar en este ejercicio. Usa `Invoke-WebRequest ... -SkipHttpErrorCheck` |

### Linux / macOS / Git Bash

```bash
# GET del catálogo -> 200
curl -s http://localhost:8080/api/v1/ejemplo/monedas

# POST válido -> 200   (-s silencia la barra de progreso, -i muestra headers y código de estado)
curl -s -i -X POST http://localhost:8080/api/v1/ejemplo/propinas   -H "Content-Type: application/json"   -d '{"montoCuenta":200.00,"porcentajePropina":10,"numeroPersonas":4,"moneda":"MXN"}'

# Fuera de rango -> 400   (lo rechaza el minimum/maximum declarado en requests.yaml)
curl -s -i -X POST http://localhost:8080/api/v1/ejemplo/propinas   -H "Content-Type: application/json"   -d '{"montoCuenta":200.00,"porcentajePropina":250,"numeroPersonas":0,"moneda":"MXN"}'

# Fuera del enum -> 400   (ni siquiera llega a deserializarse)
curl -s -i -X POST http://localhost:8080/api/v1/ejemplo/propinas   -H "Content-Type: application/json"   -d '{"montoCuenta":200.00,"porcentajePropina":10,"numeroPersonas":4,"moneda":"XYZ"}'

# Regla de negocio -> 422   (cada campo es valido; lo invalido es la combinacion)
curl -s -i -X POST http://localhost:8080/api/v1/ejemplo/propinas   -H "Content-Type: application/json"   -d '{"montoCuenta":0.01,"porcentajePropina":0,"numeroPersonas":100,"moneda":"MXN"}'

# Tu endpoint, aun sin implementar -> 501
curl -s -i http://localhost:8080/api/v1/zonas
```

### Windows — CMD

El JSON va entre comillas dobles, con las de dentro escapadas como `\"`:

```bat
curl -s http://localhost:8080/api/v1/ejemplo/monedas

curl -s -i -X POST http://localhost:8080/api/v1/ejemplo/propinas -H "Content-Type: application/json" -d "{\"montoCuenta\":200.00,\"porcentajePropina\":10,\"numeroPersonas\":4,\"moneda\":\"MXN\"}"
```

### Windows — PowerShell (con curl.exe)

Aquí el JSON va entre **comillas simples**, sin escapar nada:

```powershell
curl.exe -s http://localhost:8080/api/v1/ejemplo/monedas

curl.exe -s -i -X POST http://localhost:8080/api/v1/ejemplo/propinas `
  -H "Content-Type: application/json" `
  -d '{"montoCuenta":200.00,"porcentajePropina":10,"numeroPersonas":4,"moneda":"MXN"}'
```

### Windows — PowerShell nativo

`Invoke-RestMethod` es cómodo porque te devuelve el JSON ya convertido en objeto:

```powershell
# GET -> objeto de PowerShell, formateado como tabla
Invoke-RestMethod -Uri http://localhost:8080/api/v1/ejemplo/monedas

# POST válido
$body = '{"montoCuenta":200.00,"porcentajePropina":10,"numeroPersonas":4,"moneda":"MXN"}'
Invoke-RestMethod -Uri http://localhost:8080/api/v1/ejemplo/propinas `
  -Method Post -ContentType "application/json" -Body $body
```

**Para los casos de error usa `Invoke-WebRequest -SkipHttpErrorCheck`**, o la excepción te
ocultará exactamente lo que querías leer:

```powershell
$body = '{"montoCuenta":200.00,"porcentajePropina":10,"numeroPersonas":4,"moneda":"XYZ"}'
$r = Invoke-WebRequest -Uri http://localhost:8080/api/v1/ejemplo/propinas `
  -Method Post -ContentType "application/json" -Body $body -SkipHttpErrorCheck

$r.StatusCode   # 422
$r.Content      # el cuerpo del error
```

> Verifica siempre el **código de estado**, no solo el cuerpo. Un endpoint que devuelve el
> mensaje de error correcto con un `200 OK` está mal, y es de las cosas que se revisan.
> Por eso los ejemplos usan `-i` (curl) y `$r.StatusCode` (PowerShell).

### El puerto ya está ocupado

Si al arrancar ves *Port 8080 was already in use*, levanta la app en otro puerto:

```bash
./mvnw spring-boot:run -Dspring-boot.run.arguments=--server.port=18080
```

---

## Estructura del proyecto

```
open-api/                       ← EL CONTRATO. De aquí salen controllers y modelos.
├── openapi.yaml
├── requests.yaml
├── resources.yaml
├── responses.yaml
└── enum.yaml                       (lo único que NO se genera: ver más arriba)

src/main/java/com/teletubies/endpoints/
├── EndpointsApplication.java
│
├── ejemplo/                    ← EJEMPLO DE REFERENCIA. Léelo, y luego BÓRRALO.
│   ├── delegate/                   capa web: implementa la interfaz generada, elige el status
│   ├── service/                    capa de negocio: cálculo y reglas, SIN estado
│   ├── enums/                      los catálogos escritos a mano (Moneda, ZonaEjemplo)
│   └── exception/                  excepción de negocio + su traducción a HTTP
│
└── cotizacion/                 ← AQUÍ VA TU SOLUCIÓN
    ├── delegate/
    ├── service/
    ├── enums/                      vacío: aquí va tu Zona, que sustituye a ZonaEjemplo
    └── exception/

target/generated-sources/openapi/    ← generado en cada build. NO lo edites.
└── com/teletubies/endpoints/
    ├── api/                        interfaces, controllers y delegates
    └── model/                      los modelos de entrada y salida
```

Fíjate en lo que **ya no está**: no hay `controller/` ni `dto/`. Esas dos cosas las produce
ahora el contrato. Lo que escribes a mano es la lógica (`service`), su traducción a HTTP
(`delegate`), tus errores de negocio (`exception`) y los catálogos (`enums`).

El paquete `ejemplo` implementa una **calculadora de propinas**: un `POST` que calcula y un
`GET` que expone un catálogo. Es deliberadamente la misma *forma* que el ejercicio pero de
otro dominio, para que veas cómo se reparten las responsabilidades entre capas sin darte
resuelta ninguna decisión de las que se evalúan.

> **Bórralo antes de entregar.** Son cuatro lugares, no uno:
>
> 1. El paquete `ejemplo/` y su carpeta de tests.
> 2. Sus dos rutas y sus schemas en `open-api/` — están marcados con un encabezado que dice
>    `EJEMPLO DE REFERENCIA`, incluido el enum `Moneda` en `enum.yaml`.
> 3. Los mapeos de `Moneda` en el `pom.xml` (`importMapping` y `schemaMapping`).
> 4. **Antes que nada, tu `Zona` tiene que haber sustituido a `ZonaEjemplo`**, porque esa
>    clase también se va con el paquete y el contrato de cotización la está usando.
>
> Si borras el código pero dejas el contrato, Maven regenera un `PropinaApiController` sin
> delegate que lo implemente y tu API publica dos rutas que responden 501. Y si dejas el
> mapeo apuntando a una clase que ya borraste, **la compilación falla**. El límite de
> **2 endpoints** aplica a tu API de cotización; los del ejemplo no cuentan, pero tampoco
> deben quedarse en la entrega.

### Qué mirar en el ejemplo

| Archivo | Qué te enseña |
|---|---|
| `open-api/openapi.yaml` | Cómo se declara una ruta sin definir ni un schema: todo por `$ref`. Y cómo el `tag` decide el nombre de la interfaz que vas a implementar |
| `open-api/requests.yaml` | Bean Validation declarada en el contrato: rangos, longitudes, `required`. Y hasta dónde llega lo que el contrato puede validar |
| `open-api/resources.yaml` | Cómo se declara lo que sale, y la forma del `ErrorResource` |
| `open-api/responses.yaml` | Que el contrato de errores es parte del contrato, no una ocurrencia tardía |
| `open-api/enum.yaml` + `ejemplo/enums/Moneda.java` | Por qué el catálogo vive en un solo archivo y lo referencian los dos lados, y qué se gana escribiendo el enum a mano en vez de generarlo. **Es la plantilla de lo que tendrás que decidir con `Zona`** |
| `service/PropinaService.java` | Dónde vive la lógica de negocio, por qué la capa no conoce HTTP, y la diferencia entre validar *formato* (el YAML) y validar *negocio* (aquí) |
| `delegate/PropinaDelegate.java` | Qué es un delegate y de dónde sale, por qué el `POST` responde `200` y no `201`, y las convenciones de Lombok y `final` |
| `exception/EjemploExceptionHandler.java` | El mecanismo `@RestControllerAdvice` y el criterio `400` vs `422`. **Lee la advertencia del inicio del archivo** |
| `PropinaServiceTest` / `PropinaControllerTest` | Probar negocio sin Spring, y probar el contrato REST con `@WebMvcTest` sobre el controller generado |

---

## Lombok

El proyecto incluye [Lombok](https://projectlombok.org/), que genera código repetitivo
**en tiempo de compilación**. Las dos anotaciones que vas a usar:

| Anotación | Qué genera | Dónde va |
|---|---|---|
| `@RequiredArgsConstructor` | Un constructor con todos los campos `final` — el que Spring usa para inyectar | Clases con dependencias que no sean `record`; también los enums con campos |
| `@Slf4j` | El campo `private static final Logger log` ya apuntando a esa clase | Cualquier clase donde vayas a registrar algo |
| `@Getter` | Un getter por cada campo | Donde necesites leer campos desde fuera, como los enums de `enums/` |

El par `@Getter @RequiredArgsConstructor` sobre un enum con campos es un patrón que vas a
ver mucho: te ahorra escribir a mano el constructor y los getters. Míralo en `Moneda.java` y
en `ZonaEjemplo.java`, y cópialo en tu propio `Zona.java`.

```java
@Slf4j
@Service
@RequiredArgsConstructor
public class MiServicio {

    private final OtroServicio otroServicio;   // <- el `final` es lo que lo mete al constructor

    public Resultado hacerAlgo(final String valor) {
        log.debug("parametro recibido: {}", valor);   // {} en vez de concatenar con +
        return otroServicio.calcular(valor);
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

**En los modelos.** Los que consumes son generados: ya traen getters, setters, `equals`,
`hashCode`, `toString` y un builder. Además viven en `target/` y se reescriben en cada
compilación, así que ni siquiera podrías anotarlos.

**En un `record`.** `PropinaDelegate` es un record y por eso no lleva
`@RequiredArgsConstructor`: el constructor con los campos finales lo escribe el lenguaje.
Sí lleva `@Slf4j`, porque el logger no lo genera el record. `@Data` encima de un record ni
siquiera compila.

La regla de fondo: Lombok cubre el boilerplate que Java no resuelve solo; donde el lenguaje
—o el generador— ya lo cubre, no lo anotes. Tampoco anotes por costumbre:
`@RequiredArgsConstructor` en una clase sin campos `final` no genera nada y confunde a quien
la lee esperando encontrar un constructor.

---

## La convención del `final`

En este proyecto los **parámetros y las variables locales van marcados `final`**:

```java
public PropinaResource calcular(final CalcularPropinaRequest request) {
    final BigDecimal montoCuenta = request.getMontoCuenta().setScale(2, HALF_UP);
    ...
}
```

No cambia lo que hace el código. Declara una intención: ese nombre apunta a lo mismo de
principio a fin del método. Quien lo lee no tiene que revisar las siguientes veinte líneas
para saber si alguien lo reasignó a la mitad, y si alguien lo intenta, el compilador lo
detiene.

Es distinto del `final` de los campos, que además de intención tiene efecto: es lo que hace
que `@RequiredArgsConstructor` los meta al constructor, y lo que permite que un `record`
exista. **Sigue esta convención en tu código**, es parte de lo que se revisa.

Tampoco uses `var`: escribe el tipo real. En una clase corta `var` se lee bien, pero en una
revisión de código a la que llegas en frío el tipo explícito te ahorra ir a buscar qué
devuelve el método.

---

## Tu entrega empieza aquí

Todo lo que sigue está en blanco **a propósito**. No es relleno: es la parte del ejercicio
donde se mide tu criterio. Una solución que funciona pero no documenta sus decisiones vale
menos que una que elige distinto y explica por qué.

Borra las instrucciones en cursiva conforme las vayas contestando.

### Qué agregaste al contrato

*El contrato viene incompleto a propósito. Falta declarar la respuesta para cuando se
incumple una regla de negocio, y `pesoKg` no tiene tope. Di qué agregaste a cada archivo
YAML, con qué código HTTP, y por qué.*

| Archivo | Qué agregaste | Por qué |
|---|---|---|
|  |  |  |

*Y una decisión aparte: ¿cuáles son tus zonas, y con qué criterio las elegiste? Describe
también cómo sustituiste `ZonaEjemplo` por tu propio enum.*

### Decisiones de diseño

*¿Cómo estructuraste la respuesta exitosa de cada endpoint? ¿Por qué esos campos? Si
cambiaste algo del contrato que ya venía dado, dilo aquí y explica qué te llevó a ello.*

### Supuestos asumidos

*El enunciado deja cosas sin especificar a propósito. Lista lo que tuviste que decidir tú y
con qué criterio. Por ejemplo: ¿qué pasa si el paquete pesa exactamente el máximo? ¿un envío
`LOCAL` con origen en otro estado tiene sentido, y si no, quién lo detecta? ¿el tiempo de
entrega depende sólo de la zona o también del peso?*

*Ojo: el contrato ya resolvió algunos de esos huecos por ti — `destino` es un enum y `origen`
es texto libre, por ejemplo. Eso también es una decisión de diseño: si no estás de acuerdo
con ella, cámbiala en el YAML y documéntalo.*

### Tabla de tarifas

*Tus zonas, tus costos base, tus tiempos de entrega, y el criterio con el que elegiste esos
números. Los valores son libres; lo que se evalúa es que exista un criterio y esté escrito.*

| Zona | Costo base | Tiempo de entrega | Criterio |
|---|---|---|---|
|  |  |  |  |

### Contrato de errores

*¿Usaste el `ErrorResource` que ya declara el contrato o definiste otra cosa? ¿Es la misma
forma en los dos endpoints? ¿Cómo reportas varios errores de validación a la vez? ¿Qué
código HTTP usaste para cada situación y por qué?*

> **Dato para arrancar:** el contrato ya declara un `ErrorResource` con un campo `detalles`
> pensado para varios errores a la vez, pero **nadie lo llena todavía**. Si no haces nada, un
> `@Valid` que falla responde `400` con el cuerpo por defecto de Spring, que **no dice qué
> campo falló**: `{"timestamp":"...","status":400,"error":"Bad Request","path":"/..."}`.
> Ese cuerpo es técnicamente correcto, inútil para quien consume tu API, y además contradice
> lo que tu propio contrato promete. Qué hacer al respecto es tuyo: puedes dejarlo así y
> justificarlo, llenar el `ErrorResource`, o irte por `ProblemDetail` y actualizar el YAML
> para que describa lo que la API realmente devuelve.

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
