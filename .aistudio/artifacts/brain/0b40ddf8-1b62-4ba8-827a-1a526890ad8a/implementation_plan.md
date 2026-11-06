# Plano de Implementação — Remoção do Heatmap do Treino, Seletor de Vista no Perfil & Build Alpha

## 1. Remoção Completa do Heatmap na Área de Treino
- **Arquivo**: `app/src/main/java/com/example/ui/screens/workout/WorkoutCards.kt`
- **Ação**:
  - Remover a chamada ao componente `RoutineMuscleSummaryCard` dentro de `RoutineDetailView` (linhas 697–705) e o respectivo import.
  - Dessa forma, a tela de detalhes/execução da ficha de treino exibirá diretamente o cabeçalho da ficha e a lista de exercícios, mantendo o Heatmap Muscular **exclusivamente na tela de Perfil**.

## 2. Heatmap Anatômico no Perfil com Alternância de Vista (Frontal, Posterior e Lado a Lado)
- **Arquivos**:
  - `app/src/main/java/com/example/ui/components/MuscleHeatmapView.kt`
  - `app/src/main/java/com/example/ui/screens/profile/UserProfileScreen.kt`
  - `app/src/main/java/com/example/ui/VirtusViewModel.kt`
- **Ação**:
  - Adicionar um seletor de modo de visualização (`HeatmapViewMode`: **Lado a Lado**, **Frontal**, **Posterior**) no `MuscleHeatmapView`:
    - No modo **Lado a Lado**, exibe as silhuetas **Anterior** e **Posterior** simultaneamente.
    - Nos modos **Frontal** ou **Posterior**, amplia a silhueta selecionada em destaque central com visualização detalhada das fibras musculares e toque interativo.
  - Garantir na `UserProfileScreen` que o cálculo de volume muscular (`getMuscleVolume`) observe reativamente `workoutLogs`, `workoutRoutines` e `allWorkoutExercises`, aplicando a escala térmica **Ciano** (baixa intensidade) e **Laranja** (alta intensidade).

## 3. Atualização e Subida da Versão Alpha
- **Arquivo**: `app/build.gradle.kts`
- **Ação**:
  - Incrementar `versionCode` e atualizar `versionName` / `releaseNotes` do canal `alpha` no `firebaseAppDistribution`.
  - Compilar o APK (`compile_applet`), sincronizar regras (`deploy_firebase`) e executar a tarefa de publicação do canal Alpha (`appDistributionUploadDebug` / geração do APK Alpha).
