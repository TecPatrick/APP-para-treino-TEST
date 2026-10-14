# Plano de Refatoração e Melhorias (Layout, GPS e Áudio)

## Objetivo
Resolver problemas de layout em dispositivos como o S21 FE e adicionar funcionalidades de GPS e Feedback por Áudio.

## Estratégia Técnica
1.  **Layout Responsivo**:
    *   Substituir larguras fixas por `Modifier.fillMaxWidth()` ou `Modifier.weight()`.
    *   Utilizar `Modifier.widthIn(max = ...)` para limitar a expansão.
    *   Revisar `PaddingValues` e espaçamentos (`Spacer`) para valores proporcionais.
2.  **GPS & Mapa**:
    *   Refinar a interatividade da `GpsInteractiveMapView`.
3.  **Feedback por Áudio (TTS)**:
    *   Implementar `TextToSpeechManager` para fornecer telemetria em tempo real (km, ritmo, tempo).

## Escopo por Tela

### 1. DashboardScreen
*   Revisar o espaçamento dos cards essenciais.
*   Garantir que a `LazyColumn` não force larguras excessivas.

### 2. WorkoutScreen
*   Revisar alinhamento de ícones e `FilterChip`.

### 3. GpsTrackerScreen (Revisada)
*   Refinar a `GpsInteractiveMapView` para melhor interatividade.
*   Integrar `TextToSpeechManager` para anunciar telemetria (km, ritmo, tempo) durante o treino.
*   Ajustar a altura dos componentes de mapa (`GpsInteractiveMapView` e `RoutePathCanvas`) para serem dinâmicos.
*   Revisar `MetricBox` para ocupar espaço proporcionalmente.

### 4. ShoppingListScreen
*   Revisar layout dos itens da lista de compras para evitar colisão.

## Verificação
*   Compilação do APK para validação.
*   Testes funcionais de áudio e interatividade no mapa.
