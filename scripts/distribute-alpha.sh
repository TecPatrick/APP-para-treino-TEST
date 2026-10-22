#!/usr/bin/env bash
# =============================================================================
# 🚀 Firebase App Distribution - Deploy Script para o Grupo Alpha
# =============================================================================
# Este script automatiza a compilação e o envio do aplicativo para os testadores
# do grupo 'alpha' no Firebase App Distribution via Gradle ou Firebase CLI.
#
# Uso:
#   ./scripts/distribute-alpha.sh [release|debug] ["Notas da Release"]
#
# Exemplos:
#   ./scripts/distribute-alpha.sh release "Versão 1.0 Alpha - Gráficos de Carga e App Check"
#   ./scripts/distribute-alpha.sh debug "Build rápida de testes Alpha"
# =============================================================================

set -e

BUILD_TYPE="${1:-release}"
RELEASE_NOTES="${2:-"Build Alpha automática de testes - Virtus Growth ($(date +'%d/%m/%Y %H:%M'))"}"
APP_ID="1:204365877775:android:f77650214127790642497a"
GROUP_NAME="alpha"

echo "=========================================================="
echo "🚀 Iniciando Distribuição para Firebase App Distribution"
echo "📦 Tipo de Build: ${BUILD_TYPE}"
echo "👥 Grupo de Testadores: ${GROUP_NAME}"
echo "🆔 App ID: ${APP_ID}"
echo "📝 Notas: ${RELEASE_NOTES}"
echo "=========================================================="

export FIREBASE_RELEASE_NOTES="${RELEASE_NOTES}"

if [ "${BUILD_TYPE}" = "release" ]; then
    echo "🔨 Executando tarefa Gradle :app:distributeAlpha..."
    gradle :app:distributeAlpha
    echo "✅ Release APK compilada e enviada para o grupo '${GROUP_NAME}' com sucesso!"
else
    echo "🔨 Executando tarefa Gradle :app:distributeAlphaDebug..."
    gradle :app:distributeAlphaDebug
    echo "✅ Debug APK compilada e enviada para o grupo '${GROUP_NAME}' com sucesso!"
fi
