# Preparação de Lançamento para Trilhas Alpha e Beta (Google Play & APK)

Plano de configuração e auditoria técnica para publicar e distribuir o aplicativo nas faixas de teste fechado (Alpha) e aberto (Beta), com suporte simultâneo a envio via Google Play Console e geração de APK/AAB instalável para testadores.

### User Review & Critical Decisions

> [!IMPORTANT]
> **Decisões confirmadas com o usuário na Fase 1:**
> - **Numeração de Versão:** `versionName = "0.9.0"` | `versionCode = 1` (versão Beta inicial pronta para testes).
> - **Canais de Distribuição:** Ambos (Google Play Console Trilha Fechada/Aberta + APK direto para grupos de testadores).
> - **Nome Público Oficial:** `"Virtus - Dieta & Treino"` (23 caracteres — respeita com folga o limite máximo de 30 caracteres do Google Play sem emojis ou termos promocionais).

---

### 1. Overview & Core Concept

- **O que faz:** Ajusta o manifesto, gradle e recursos de identidade do aplicativo para o estágio de testes Alpha/Beta, garantindo conformidade com as políticas do Google Play (Google Play Developer Policy), sincronização de nomes com a plataforma AI Studio e compatibilidade de assinatura para geração de pacotes release (`assembleRelease` / `bundleRelease`).
- **Público-alvo:** Grupo de testadores internos (Alpha), beta-testers convidados e usuários de pré-lançamento.
- **Resultado:** Build versionado, limpo, seguro e assinado, pronto para ser submetido ao Google Play Console ou baixado via menu de configurações do AI Studio.

---

### 2. User Experience & Visual Identity

- **Nome no Launcher / Tela Inicial:** `Virtus - Dieta & Treino` (consistente na tela inicial do Android, gaveta de apps e cabeçalhos).
- **Nome na Plataforma AI Studio:** Sincronizado em `metadata.json` como `Virtus - Dieta & Treino`.
- **Tema:** Dark Carbon & M3 Growth Orange/Green, com suporte completo a safe-areas, modo edge-to-edge e alvos de toque de no mínimo 48dp.
- **Sobre o App / Versão:** Indicador visual de versão `v0.9.0-beta` na tela de Perfil / Configurações para facilitar o reporte de bugs pelos testadores.

---

### 3. Checklist de Conformidade com Políticas do Google Play

1. **Título do App:** `<= 30 caracteres`, sem emojis, sem palavras-chave apelativas ("Free", "Melhor", "#1").
2. **Permissões de Mídia e Armazenamento:** Zero solicitação de permissões perigosas (`READ_EXTERNAL_STORAGE`). Compartilhamento via `FileProvider` seguro.
3. **Serviços em Primeiro Plano:** `RunningForegroundService` declarado especificamente com `foregroundServiceType="location"`.
4. **Ofuscação e Otimização:** ProGuard / R8 ativo no build de release (`isMinifyEnabled = true`, `isShrinkResources = true`), com regras de integridade para classes de dados Room/JSON preservadas.
5. **Preservação de Keystore:** Conforme as diretrizes da plataforma, mantidas as configurações de assinatura do projeto.

---

### 4. Technical Architecture & Modifications

```
┌──────────────────────────────────────────────────────────────────┐
│                   Ajustes de Build e Lançamento                  │
├──────────────────────────┬───────────────────────────────────────┤
│ build.gradle.kts         │ versionName = "0.9.0"                 │
│                          │ versionCode = 1                       │
│                          │ releaseNotes = "Virtus Beta v0.9.0"   │
├──────────────────────────┼───────────────────────────────────────┤
│ strings.xml              │ app_name = "Virtus - Dieta & Treino"  │
│                          │ app_full_name = "Virtus..."           │
├──────────────────────────┼───────────────────────────────────────┤
│ metadata.json            │ name = "Virtus - Dieta & Treino"      │
│                          │ (preservando Server-Side Gemini API)  │
├──────────────────────────┼───────────────────────────────────────┤
│ settings.gradle.kts      │ rootProject.name = "Virtus..."        │
├──────────────────────────┼───────────────────────────────────────┤
│ UserProfileScreen.kt     │ Exibição do badge "Versão 0.9.0-beta" │
└──────────────────────────┴───────────────────────────────────────┘
```

#### Etapas de Execução:
1. Atualizar `app/build.gradle.kts` com `versionName = "0.9.0"`, `versionCode = 1` e notas de release.
2. Atualizar `strings.xml`, `metadata.json` e `settings.gradle.kts` mantendo estrita paridade de nomes.
3. Adicionar crachá com indicação de versão no rodapé da tela de Perfil (`v0.9.0 (Beta)`).
4. Executar verificação de build (`compile_applet`) e rodar a suíte completa de testes unitários.
