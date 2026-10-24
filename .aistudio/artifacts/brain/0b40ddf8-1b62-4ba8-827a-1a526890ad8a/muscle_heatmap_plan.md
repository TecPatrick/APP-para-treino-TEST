# Plano de Implementação: Visualização de Muscle Heatmap no Perfil

Implementação de um visualizador interativo de músculos treinados (estilo "Hevy") na aba de perfil.

---

## 1. Mapeamento de Dados e Lógica de Cálculo
- **Cálculo de Volume:** Utilizar `WorkoutLog` e `WorkoutExercise` para calcular o volume de treino por grupamento muscular (`muscleGroup`).
- **Agregação:** Criar um método no `VirtusViewModel` para agrupar o volume de treino total por músculo em diferentes intervalos de tempo (7d, 30d, Total).

## 2. Interface (UI)
- **Componente `MuscleHeatmapView`:**
  - Sobrepor uma imagem de modelo humano (mapa de calor) com `Canvas` ou `Box` para renderizar camadas coloridas baseadas na intensidade do treino (maior volume = cor mais intensa).
- **Interatividade:**
  - Adicionar filtros (Chips) para alternar entre "7 dias", "30 dias" e "Histórico".
  - A sobreposição de cores será dinâmica, alterando a opacidade/cor do grupamento muscular conforme o volume calculado.

## 3. Integração
- Atualizar a tela `ProfileScreen` para incluir o novo card de "Foco Muscular".
- Acessar os dados de treino através do `VirtusViewModel` (já existente).
