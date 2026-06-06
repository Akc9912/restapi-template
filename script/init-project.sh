#!/usr/bin/env bash
set -euo pipefail

OLD_GROUP_ID="com.project"
OLD_ARTIFACT_ID="restapi"
OLD_BASE_PACKAGE="${OLD_GROUP_ID}.${OLD_ARTIFACT_ID}"

echo "========================================"
echo "  Inicializar proyecto"
echo "========================================"
echo ""
echo "Este script renombra el template para tu proyecto."
echo "Valores actuales:"
echo "  groupId:    ${OLD_GROUP_ID}"
echo "  artifactId: ${OLD_ARTIFACT_ID}"
echo "  package:    ${OLD_BASE_PACKAGE}"
echo ""

read -r -p "Nuevo groupId (ej: com.miempresa): " NEW_GROUP_ID

while [ -z "$NEW_GROUP_ID" ]; do
    read -r -p "Nuevo groupId (ej: com.miempresa): " NEW_GROUP_ID
done

read -r -p "Nuevo artifactId (ej: miapp): " NEW_ARTIFACT_ID

while [ -z "$NEW_ARTIFACT_ID" ]; do
    read -r -p "Nuevo artifactId (ej: miapp): " NEW_ARTIFACT_ID
done

if echo "$NEW_ARTIFACT_ID" | grep -q "\.\|[[:space:]]"; then
    echo ""
    echo "Error: artifactId no debe contener puntos ni espacios."
    exit 1
fi

NEW_BASE_PACKAGE="${NEW_GROUP_ID}.${NEW_ARTIFACT_ID}"

echo ""
echo "========================================"
echo "  Resumen de cambios"
echo "========================================"
echo ""
echo "  groupId:     ${OLD_GROUP_ID} -> ${NEW_GROUP_ID}"
echo "  artifactId:  ${OLD_ARTIFACT_ID} -> ${NEW_ARTIFACT_ID}"
echo "  package:     ${OLD_BASE_PACKAGE} -> ${NEW_BASE_PACKAGE}"
echo ""

read -r -p "Confirmar cambios? (s/N): " CONFIRM
if [ "$CONFIRM" != "s" ] && [ "$CONFIRM" != "S" ]; then
    echo "Cancelado."
    exit 0
fi

echo ""
echo "Aplicando cambios..."

OLD_MAIN_PATH="src/main/java/$(echo "${OLD_BASE_PACKAGE}" | tr '.' '/')"
NEW_MAIN_PATH="src/main/java/$(echo "${NEW_BASE_PACKAGE}" | tr '.' '/')"
OLD_TEST_PATH="src/test/java/$(echo "${OLD_BASE_PACKAGE}" | tr '.' '/')"
NEW_TEST_PATH="src/test/java/$(echo "${NEW_BASE_PACKAGE}" | tr '.' '/')"

echo "  1. Reemplazando packages en archivos Java..."
find src -name "*.java" -exec sed -i "s/${OLD_BASE_PACKAGE}/${NEW_BASE_PACKAGE}/g" {} +

echo "  2. Actualizando pom.xml..."
sed -i "s|<groupId>${OLD_GROUP_ID}</groupId>|<groupId>${NEW_GROUP_ID}</groupId>|" pom.xml
sed -i "s|<artifactId>${OLD_ARTIFACT_ID}</artifactId>|<artifactId>${NEW_ARTIFACT_ID}</artifactId>|" pom.xml
sed -i "s|<name>${OLD_ARTIFACT_ID}</name>|<name>${NEW_ARTIFACT_ID}</name>|" pom.xml

echo "  3. Actualizando application.properties..."
sed -i "s/spring.application.name=${OLD_ARTIFACT_ID}/spring.application.name=${NEW_ARTIFACT_ID}/" src/main/resources/application.properties

echo "  4. Moviendo directorios main..."
if [ -d "${OLD_MAIN_PATH}" ]; then
    mkdir -p "$(dirname "${NEW_MAIN_PATH}")"
    mv "${OLD_MAIN_PATH}" "${NEW_MAIN_PATH}"
fi

echo "  5. Moviendo directorios test..."
if [ -d "${OLD_TEST_PATH}" ]; then
    mkdir -p "$(dirname "${NEW_TEST_PATH}")"
    mv "${OLD_TEST_PATH}" "${NEW_TEST_PATH}"
fi

echo "  6. Limpiando directorios vacios..."
find src -type d -empty -delete 2>/dev/null || true

echo "  7. Limpiando target/..."
rm -rf target/ 2>/dev/null || true

echo ""
echo "========================================"
echo "  Proyecto inicializado!"
echo "========================================"
echo ""
echo "  Proximo paso: verifica el proyecto con:"
echo "    ./mvnw compile"
echo ""
