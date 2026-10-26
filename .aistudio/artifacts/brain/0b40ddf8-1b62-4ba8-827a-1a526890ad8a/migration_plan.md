# Plano de Migração e Reestruturação: Exercícios no Firestore

Para atender à nova estrutura de segurança e isolamento por usuário, migraremos a coleção de exercícios para uma estrutura aninhada sob os treinos.

## 1. Regras de Segurança (`firestore.rules`)
- Alterar o `match` de `/workoutExercises/{exerciseId}` para:
  `/users/{userId}/workouts/{workoutId}/exercises/{exerciseId}`.
- Garantir a validação `request.auth.uid == userId`.

## 2. Lógica de Sincronização (`FirestoreSyncManager`)
- Atualizar `syncWorkoutExerciseToCloud` para aceitar `workoutId` e construir o caminho `/users/{uid}/workouts/{workoutId}/exercises/{exerciseId}`.
- Usar 'free' como `workoutId` padrão se não houver um ID de treino específico.

## 3. Atualização e Logs no `VirtusViewModel`
- Adicionar logs detalhados do objeto `WorkoutExercise` antes da chamada ao `FirestoreSyncManager`.
- Ajustar a chamada para incluir o `workoutId` apropriado (mapeado de `routineId` ou 'free').

## 4. Migração de Dados
- Criar e executar um script de migração para mover os documentos existentes de `/users/{userId}/workoutExercises/{exerciseId}` para `/users/{userId}/workouts/{workoutId}/exercises/{exerciseId}`.
