# Auditoria e Tradução da Nomenclatura de Exercícios para Português (Padrão Bodybuilding)

## Resumo Executivo
Este plano detalha a auditoria e tradução completa de todos os nomes de exercícios, descrições biomecânicas e agrupamentos musculares para o português do Brasil no padrão utilizado no fisiculturismo (Bodybuilding), abrangendo a Biblioteca de Exercícios, Fichas de Treino e o Histórico/Logs na nuvem e local.

---

## 1. Visão Geral & Escopo
- **O que será feito**: Revisão de todas as fontes de dados de exercícios (`ExerciseGifMapper`, `PrepopulatedData`, `WorkoutExercise`, rotinas e histórico de treinos salvos no Firestore/Room) para garantir 100% de conformidade com a terminologia brasileira de musculação.
- **Padrão de Nomenclatura**: Nomes claros e técnicos de fisiculturismo (ex: *Supino Reto com Barra*, *Puxada Alta na Polia*, *Elevação Lateral com Halteres*).
- **Cobertura**: 
  1. Mapeamento de GIFs e guias biomecânicos (`ExerciseGifMapper.kt`).
  2. Fichas padrão e pré-populadas (`PrepopulatedData.kt` / `WorkoutRoutine`).
  3. Conversão de histórico de treinos e progression logs no Firestore.

---

## 2. Arquitetura & Mapeamento de Tradução

```
┌──────────────────────────────────────────┐
│        ExerciseGifMapper.kt              │
│  (Mapeamento de Nomes, Dicas & GIFs)     │
└────────────────────┬─────────────────────┘
                     │
                     ▼
┌──────────────────────────────────────────┐
│      PrepopulatedData.kt / Routines      │
│  (Fichas A, B, C padronizadas em PT-BR)  │
└────────────────────┬─────────────────────┘
                     │
                     ▼
┌──────────────────────────────────────────┐
│   VirtusRepository & Firestore Sync      │
│  (Atualização e Normalização do Histórico│
│   e Cargas dos Compostos em PT-BR)       │
└──────────────────────────────────────────┘
```

---

## 3. Passos de Implementação
1. **Auditoria de `ExerciseGifMapper.kt`**: Garantir que todos os 50+ exercícios cadastrados tenham nome em português brasileiro e dicas biomecânicas nativas.
2. **Atualização de `PrepopulatedData.kt` e Fichas**: Revisar rotinas iniciais para que todas as fichas venham com nomenclaturas oficiais.
3. **Migração e Normalização de Histórico**: Assegurar que registros antigos no Firestore/Room mapeiem corretamente para os novos nomes.
4. **Verificação**: Executar `compile_applet` para validar integridade de compilação.
