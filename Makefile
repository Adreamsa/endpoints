# ---------------------------------------------------------------------------
# Atajos para las tareas comunes del proyecto.
#
#   make build   compila y empaqueta el jar (corriendo los tests)
#   make run     levanta la aplicacion en http://localhost:8080
#   make test    corre la suite de tests
#   make clean   borra target/
#   make         muestra esta ayuda
#
# Si no tienes `make` instalado, cada target es una sola linea de Maven:
# mira la receta del target que te interese y ejecutala a mano.
# ---------------------------------------------------------------------------

ifeq ($(OS),Windows_NT)
    MVNW = mvn.cmd
else
    MVNW = ./mvnw
endif

.PHONY: help build run test clean openapi-generate

# Primer target del archivo = el que corre `make` sin argumentos.
help:
	@echo "Targets disponibles:"
	@echo "  make build   - compila y empaqueta el jar en target/ (incluye tests)"
	@echo "  make run     - levanta la aplicacion en http://localhost:8080"
	@echo "  make test    - corre la suite de tests"
	@echo "  make clean   - borra target/"
	@echo "  make openapi-generate - regenera el codigo desde open-api/ sin compilar"

# Regenera los controllers y modelos a partir de open-api/openapi.yaml.
#
# No siempre hace falta: el plugin corre en la fase generate-sources, asi que
# `make build`, `make run` y `make test` ya regeneran solos. Este target es para
# cuando editaste el YAML y quieres ver el codigo generado de inmediato, sin
# esperar a una compilacion completa.
#
# Lo generado vive en target/generated-sources/openapi y se sobrescribe en cada
# corrida: no lo edites, edita el YAML.
openapi-generate:
	$(MVNW) org.openapitools:openapi-generator-maven-plugin:generate@openapi

# `clean install` recorre el ciclo de vida de Maven hasta la fase install:
#
#   validate -> compile -> test -> package -> verify -> install
#
# Es decir: limpia, compila, corre los tests, arma el jar en target/ y ademas lo
# copia a tu repositorio local (~/.m2/repository), que es de donde Maven resuelve
# las dependencias de otros proyectos.
#
# El `clean` del inicio garantiza que no queden .class viejos de una clase que
# borraste o renombraste. Y como los tests corren antes de empaquetar, si alguno
# falla no se genera jar: el mismo comportamiento que tendria un pipeline de CI.
build:
	$(MVNW) clean install

# Se detiene con Ctrl+C.
run:
	$(MVNW) spring-boot:run

test:
	$(MVNW) test

clean:
	$(MVNW) clean
