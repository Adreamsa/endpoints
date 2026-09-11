# Ejercicio: API de Cotización de Envíos

## Contexto

El equipo de logística necesita un servicio backend que permita a los clientes obtener una cotización de envío antes de confirmar un pedido. El sistema debe recibir la información del paquete, validarla, y devolver el costo estimado junto con el tiempo de entrega. Si algo no es válido, el cliente debe recibir una respuesta clara indicando qué salió mal.

Además, el equipo de frontend necesita poder mostrar en un selector las zonas de destino que el sistema soporta actualmente.

## Requerimientos funcionales

> Las dos rutas ya están declaradas en `open-api/openapi.yaml`, junto con la forma de sus
> peticiones y respuestas. Lo que falta es completarlas (ver el punto 3) e implementarlas.

### 1. Cotizar envío

El cliente envía una solicitud con:

- Origen
- Destino
- Peso del paquete (en kg)

El sistema debe:

- Validar que los datos vengan completos y sean coherentes.
- Calcular un costo estimado y un tiempo de entrega según el destino y el peso.
- Si el destino no está soportado, o el paquete excede el peso máximo permitido, informar al cliente de forma clara.

**Reglas de negocio:**

- El peso máximo permitido para cualquier envío es de **50 kg**.
- Para paquetes de hasta **70 kg**, se aplica una tarifa especial de sobrepeso en lugar de rechazar el envío.
- El costo base depende de la zona de destino (tú defines la tabla de tarifas; documenta tus valores y el criterio usado).

### 2. Consultar zonas soportadas

El sistema debe exponer las zonas de destino válidas actualmente, para que puedan mostrarse al cliente antes de que intente cotizar.

### 3. Completar el contrato

El contrato OpenAPI del proyecto viene **deliberadamente incompleto**. Antes de poder resolver
los dos puntos anteriores tendrás que cerrarlo:

- No declara ninguna respuesta para cuando se incumple una regla de negocio (destino no
  soportado, exceso de peso). Hoy esos casos no tienen contrato.
- `pesoKg` no tiene un tope declarado.
- El enum de zonas se llama `ZonaEjemplo` a propósito y apunta a una clase de demostración
  del paquete `ejemplo`: está ahí para que el proyecto arranque entero, **no son las zonas
  del ejercicio**. Defínelas tú junto con tu tabla de tarifas, renombra el schema y escribe
  tu propio enum. Si dónde poner la tarifa y el tiempo de entrega —en el enum o en el
  servicio— es decisión tuya; documéntala.

Decidir qué falta, con qué código HTTP, y si el rechazo corresponde al contrato o a tu lógica
de negocio, es parte del ejercicio.

## Restricciones técnicas

- Java + Spring Boot.
- **Contrato-primero.** Los controllers y los modelos los genera `openapi-generator` a partir
  de `open-api/`. **No escribas `@RestController` ni DTOs a mano**, y no edites nada de
  `target/generated-sources/`: se sobrescribe en cada compilación. Lo que quieras cambiar del
  contrato, cámbialo en el YAML y regenera.
- El contrato está separado en cinco archivos —`openapi`, `requests`, `resources`,
  `responses`, `enum`— y así debe quedar. Respeta esa separación al agregar lo tuyo.
- Un enum de `enum.yaml` se puede dejar generado o mapear a una clase Java escrita a mano,
  con `importMappings`/`schemaMappings` en el `pom.xml`. `Moneda` está mapeada y sirve de
  ejemplo, igual que `ZonaEjemplo`. Si mapeas un enum, el YAML y la clase tienen que decir lo mismo:
  nadie lo verifica por ti.
- Máximo **2 endpoints**. No se requiere ni se espera persistencia de ningún tipo (sin base de datos, sin estado en memoria entre requests).
- Las validaciones de entrada se declaran en el contrato (`required`, `minimum`, `maxLength`,
  los valores de `enum.yaml`...), no con anotaciones escritas a mano: el generador las
  convierte en Bean Validation. Lo que el contrato no pueda expresar va en tu `service`. No
  se requieren capas de abstracción adicionales (no repository, no service con estado) —
  mantén la solución simple y directa.
- Usa códigos de estado HTTP semánticamente correctos (no todo debe responder `200`).
- La respuesta exitosa y el `ErrorResource` ya vienen declarados. Puedes cambiarlos si tienes
  un motivo, pero entonces el YAML y el código tienen que seguir diciendo lo mismo. Documenta
  las decisiones que tomaste.

## Entregable

- Código fuente del proyecto Spring Boot, con el contrato ya completado en `open-api/`.
- Un `README.md` breve donde documentes:
  - Qué le agregaste al contrato y por qué.
  - Cualquier supuesto que hayas tenido que asumir por falta de información en este enunciado.
  - Tu tabla de tarifas y el criterio usado para definirla.
  - Cualquier inconsistencia que hayas notado en este documento y cómo decidiste resolverla.

## Criterios de evaluación

- Corrección de las validaciones y manejo de errores.
- Uso adecuado de códigos HTTP.
- **Criterio al repartir responsabilidades entre el contrato y el código**: qué decidiste
  declarar en el YAML, qué dejaste para la capa de negocio, y por qué.
- Coherencia entre lo que el contrato promete y lo que la API realmente devuelve.
- Calidad de las decisiones documentadas ante la ambigüedad — **no se penaliza interpretar el requerimiento de una forma u otra, se penaliza no declarar que hubo una interpretación.**
