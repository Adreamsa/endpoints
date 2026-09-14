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
> a `ejemplo/enums/ZonaEjemplo.java`, una clase de demostración: gracias a ella el proyecto
> arranca entero desde el primer `make run`, con descripciones incluidas.
>
> **Esas tres zonas no son las del ejercicio.** El enunciado te pide tu propia tabla de
> tarifas, y eso empieza por decidir cuáles son tus zonas. Son cuatro pasos: renombra el
> schema a `Zona` en `enum.yaml` con tus valores, actualiza los tres `$ref` que apuntan ahí,
> escribe tu `cotizacion/enums/Zona.java` copiando la forma de `ZonaEjemplo`, y reapunta las
> dos líneas del `pom.xml`.

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
├── ejemplo/                    ← EJEMPLO DE REFERENCIA. Se queda: consúltalo.
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

> **Déjalo donde está**: es material de consulta y se queda en la entrega. El límite de
> **2 endpoints** aplica sólo a tu API de cotización; los del ejemplo no cuentan.
>
> Lo único suyo que sí tienes que sustituir es `ZonaEjemplo`, porque tu contrato de
> cotización lo está usando y esas no son tus zonas.

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

| Archivo | Qué agregaste | Por qué |
|---|---|---|
| `enum.yaml` | Schema `Zona` con valores `LOCAL`, `NACIONAL`, `EXPRESS`, `INTERNACIONAL` | Reemplaza el schema de demostración `ZonaEjemplo`. Las zonas definen los destinos válidos del sistema. |
| `requests.yaml` | `$ref` de `destino` actualizado a `Zona` | El campo `destino` del request debe apuntar al nuevo enum real. |
| `resources.yaml` | `$ref` de `destino` y `codigo` actualizados a `Zona` | La respuesta devuelve la misma zona, debe ser el mismo tipo. |
| `openapi.yaml` | Respuesta `"422"` en `POST /api/v1/cotizaciones` | Cubre el caso de sobrepeso (> 70 kg): el request está bien formado pero incumple una regla de negocio. Ya existía `UnprocessableEntity` en `responses.yaml`. |
| `pom.xml` | `importMapping` y `schemaMapping` para `Zona` | Le indica al generador que no genere `Zona.java` sino que importe la clase manual, igual que `Moneda`. |

Las zonas se definieron en `enum.yaml` con cuatro valores. `ZonaEjemplo` se renombró a `Zona` y se mapeó en `pom.xml` a `cotizacion/enums/Zona.java`, siguiendo exactamente el mismo mecanismo que usa `Moneda`.

### Decisiones de diseño

**Tarifas en el enum, no en el service.** Cada valor de `Zona` carga su `costoBase` y `diasEntrega`. La alternativa era un `Map<Zona, BigDecimal>` dentro del service, pero eso separa el valor de sus datos: agregar una zona nueva obliga a actualizar el enum *y* acordarse de actualizar el Map. Poniéndolo en el enum, el compilador lo detecta: si no declaras los campos en el constructor, no compila.

**`origen` como `String` libre.** El contrato valida `minLength: 1` y `maxLength: 60`, pero no restringe los valores a un catálogo. El enunciado no especifica una lista de orígenes válidos, y sería imposible cubrir todas las ciudades posibles. Si se quisiera restringir, se agregaría otro enum; por ahora es texto libre.

**`200` y no `201` en el POST de cotizaciones.** Este endpoint calcula, no crea ningún recurso persistente. Un `201 Created` obligaría a devolver un `Location` header apuntando al recurso creado, y aquí no hay nada a qué apuntar.

### Supuestos asumidos

- **Peso exactamente en el umbral:** `pesoKg = 50.00` → tarifa normal (sin sobrecargo). `pesoKg = 70.00` → aceptado con sobrecargo. El límite es exclusivo por arriba: `> 70` se rechaza.
- **`origen` no se valida contra un catálogo:** cualquier string entre 1 y 60 caracteres es válido. El sistema no conoce los orígenes posibles.
- **El tiempo de entrega no depende del peso:** solo de la zona. Un paquete con sobrecargo dentro de una zona `LOCAL` sigue tardando 1 día hábil.
- **El sobrecargo se aplica sobre los kg que exceden 50**, no sobre el total. Fórmula: `(pesoKg - 50) × $15.00 MXN`.

### Tabla de tarifas

| Zona | Costo base | Tiempo de entrega | Criterio |
|---|---|---|---|
| `LOCAL` | $80.00 MXN | 1 día hábil | Envío dentro de la misma ciudad. Distancia mínima, sin logística de larga distancia. |
| `NACIONAL` | $180.00 MXN | 3 días hábiles | Cualquier punto del país. Requiere traslado entre ciudades. |
| `EXPRESS` | $350.00 MXN | 1 día hábil | Nacional con prioridad de procesamiento. Más caro que NACIONAL porque comparte la misma red pero con preferencia de carga y ruta. |
| `INTERNACIONAL` | $950.00 MXN | 10 días hábiles | Fuera del país. Incluye trámites de aduana y logística internacional. |

**Sobrecargo por peso:** $15.00 MXN por cada kg que supere los 50 kg, hasta el máximo de 70 kg. Por encima de 70 kg el envío se rechaza.

### Contrato de errores

Se utilizó el `ErrorResource` que ya declara el contrato (en `resources.yaml`), sin modificarlo. La forma es la misma en ambos endpoints.

Los errores de validación (`@Valid`) llenan el campo `detalles` con todos los campos inválidos de una sola vez — el cliente no tiene que corregir de uno en uno.

| Situación | Código HTTP | Por qué |
|---|---|---|
| Campo faltante u obligatorio ausente | `400 Bad Request` | El request está mal formado: le falta información estructural. |
| Valor fuera del rango del contrato (`pesoKg < 0.01`) | `400 Bad Request` | El contrato declara `minimum: 0.01`; Bean Validation lo rechaza antes de llegar al service. |
| Destino con valor no reconocido | `400 Bad Request` | El enum del contrato restringe los valores. Jackson rechaza cualquier valor fuera del enum con 400 automáticamente. |
| Paquete con peso > 70 kg | `422 Unprocessable Entity` | El request está bien formado. Cada campo es válido por separado. Lo que falla es una regla de negocio. |
| Error interno del servidor | `500 Internal Server Error` | Fallo inesperado, ajeno al cliente. |

**Criterio `400` vs `422`:** `400` cuando el problema está en el *formato* del request (un campo que el contrato no acepta). `422` cuando el request es estructuralmente válido pero incumple una regla de negocio que depende del valor o de la combinación de campos.

### Inconsistencias detectadas en el enunciado

**Contradicción en el peso máximo.** El enunciado dice dos cosas que se contradicen:

> "El peso máximo permitido para cualquier envío es de **50 kg**."
> "Para paquetes de hasta **70 kg**, se aplica una tarifa especial de sobrepeso **en lugar de rechazar el envío**."

Si el máximo fuera 50 kg, no podría haber paquetes de hasta 70 kg que se acepten con sobrecargo. Se interpretó así: **50 kg es el umbral de sobrecargo** (no el máximo), y **70 kg es el límite absoluto**. Los paquetes entre 50 y 70 kg se aceptan con cargo adicional; los que superan 70 kg se rechazan con `422`.

Esta interpretación hace que las dos reglas sean coherentes entre sí. La alternativa haría que la segunda regla no tuviera ningún efecto.

### Endpoints implementados

| Método | Ruta | Descripción |
|---|---|---|
| `POST` | `/api/v1/cotizaciones` | Cotiza un envío: recibe origen, destino y peso, devuelve costo y tiempo de entrega. |
| `GET` | `/api/v1/zonas` | Lista las zonas de destino soportadas con su descripción legible. |
