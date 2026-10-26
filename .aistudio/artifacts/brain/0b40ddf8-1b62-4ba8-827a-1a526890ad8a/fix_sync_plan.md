# Plano de Correção: Sincronização Firestore (Exercícios)

Diagnóstico: O erro de persistência ocorre porque a coleção `workoutExercises` não possui regras de segurança configuradas no `firestore.rules`, resultando em "Permission Denied".

## 1. Ajuste nas Regras de Segurança (`firestore.rules`)
- Adicionar o match para `match /workoutExercises/{exerciseId}` dentro de `match /users/{userId}`.
- Permitir `read` e `write` para o usuário proprietário.

## 2. Implementação de Logs no `VirtusViewModel`
- Adicionar `Log.d` ou `Log.e` na função `saveFullRoutineWithExercises` para registrar o estado do objeto `WorkoutExercise` antes da chamada `firestoreSync.syncWorkoutExerciseToCloud`.
- Verificar se o `id` retornado pelo repositório local está sendo utilizado corretamente na chamada de sincronização.

## 3. Validação e Verificação
- Implantação das novas regras de segurança.
- Compilação e teste da funcionalidade de adição de exercícios.
