# Virtus Growth — Dieta, Treino & Performance (Android)

Aplicativo Android completo de alta performance voltado para atletas e praticantes de musculação e corrida, integrando **planejamento nutricional**, **diário de treinos com progressão de carga**, **rastreamento de corrida GPS ao ar livre com treinador por voz (TTS)**, **lista de compras inteligente por corredores de macronutrientes**, **controle de inflação/notas fiscais** e **sincronização em nuvem (Delta Sync)** com Firebase.

---

## Principais Funcionalidades

- **Autenticação & Segurança (`AuthViewModel`)**:
  - Login com Google via Jetpack Credential Manager + Firebase Authentication e modo Convidado offline.
  - Bloqueio opcional por biometria (impressão digital / reconhecimento facial) para proteção dos diários e conformidade com a LGPD.
- **Treinos, Fichas & Progressão de Carga (`WorkoutViewModel`)**:
  - Divisões de treino configuráveis (ABCDE, Push/Pull/Legs, Upper/Lower, Full Body e fichas personalizadas).
  - Execução de treino em tempo real com cronômetro de descanso, alertas sonoros/táteis e registro de séries/repetições.
  - Histórico de evolução de cargas (1RM estimado, PRs e gráficos de progressão) e mapa de calor muscular.
  - Catálogo biomecânico com guias visuais carregados sob demanda (`assets/canonical_exercise_guides.json`) e cache em memória.
- **Dieta, Macros & Hidratação (`DietViewModel`)**:
  - Cálculo de Taxa Metabólica Basal (TMB), gasto energético diário dinâmico (repouso + treinos do dia) e metas de macronutrientes por biotipo.
  - Diário alimentar diário, metas semanais, rastreador de ingestão de água e sugestões de refeições assistidas por IA.
- **Mercado, Compra Conjunta & Notas Fiscais (`ShoppingViewModel` / `ShoppingListViewModel`)**:
  - Geração automática de lista de compras escalada por período (7, 15 ou 30 dias) com conversão inteligente de alimentos cozidos/grelhados para peso cru.
  - Organização por corredores de macronutrientes (Proteínas, Carboidratos e Gorduras) e modo de Compra Conjunta via código compartilhável.
  - Registro de Notas Fiscais e análise de inflação pessoal por produto.
- **Corrida GPS & Cardio (`CardioViewModel` / `WorkoutTrackerViewModel`)**:
  - Rastreamento de percursos ao ar livre com serviço em primeiro plano (`RunningForegroundService`), mapa interativo MapLibre e feedback de ritmo por voz (TTS).
- **Sincronização Offline-First (`SyncViewModel`)**:
  - Persistência local em Room Database combinada com sincronização incremental (*Delta Sync* e resolução de conflitos *Last-Write-Wins*) no Cloud Firestore, incluindo subcoleções aninhadas (`workoutRoutines/{id}/workoutExercises` e `workouts/{id}/exercises`).

---

## Stack Tecnológica

- **Linguagem**: Kotlin 2.x (Coroutines & StateFlow)
- **Interface (UI)**: Jetpack Compose + Material Design 3
- **Persistência Local**: Room Database (SQLite + KSP)
- **Backend & Segurança**:
  - Firebase Authentication + Jetpack Credential Manager (`GoogleId`)
  - Cloud Firestore (Offline Persistence + Delta Sync)
  - Firebase App Check (`DebugAppCheckProviderFactory` em debug / `PlayIntegrityAppCheckProviderFactory` em release)
- **Inteligência Artificial**: Firebase AI Logic / Gemini SDK (`GeminiRepository`)
- **Mapas & Geolocalização**: MapLibre Android SDK + Google Play Services Location
- **Imagens & Mídia**: Coil (`coil-compose`, `coil-gif`) + Ativos otimizados em WebP

---

## Arquitetura de Pacotes e ViewModels

O estado de sessão compartilhado (`userProfile`, `selectedDateMillis`, `toastMessage`, `dailyEnergyExpenditure` e fluxos base do Room) fica centralizado em `VirtusSharedState`, evitando duplicação entre os ViewModels de domínio:

- `AuthViewModel` — Login Google, onboarding, biometria e gestão de conta/LGPD
- `WorkoutViewModel` — Fichas, execução de treino ativo, timer de descanso e progressão de cargas
- `DietViewModel` — Metas calóricas/macros, refeições, hidratação e sugestões de dieta com IA
- `ShoppingViewModel` / `ShoppingListViewModel` — Lista de compras, compra conjunta, notas fiscais e inflação
- `CardioViewModel` / `WorkoutTrackerViewModel` — Corrida GPS, rotas e registros de cardio
- `SyncViewModel` — Sincronização Delta com o Firestore e migração de subcoleções

> **Nota sobre `applicationId` e Pacote**: O `applicationId` (`com.aistudio.growthdiet.kxmfzq`) e o `namespace` (`com.example`) são mantidos compatíveis com o aplicativo registrado no `google-services.json` do projeto Firebase atual. Para migrar o `applicationId` para `com.tecpatrick.virtus` na publicação na Google Play Store, basta registrar o novo `applicationId` no Console do Firebase, baixar o novo `app/google-services.json` e atualizar `app/build.gradle.kts`.

---

## Pré-requisitos

1. **JDK 17** configurado no ambiente (`JAVA_HOME`).
2. **Android Studio Ladybug (ou superior)** com Android SDK 35 / Build Tools 35.0.0.
3. **Configuração do Firebase (`app/google-services.json`)**:
   - Baixe o arquivo `google-services.json` no Console do Firebase para o projeto configurado e coloque-o em `app/google-services.json` (este arquivo é ignorado pelo Git por segurança).

---

## Variáveis de Ambiente de Assinatura (Release)

A assinatura de release em `app/build.gradle.kts` lê **exclusivamente** variáveis de ambiente e só aplica o `signingConfig` de release quando todas estão definidas (garantindo que builds locais funcionem sem arquivo `.jks` versionado):

| Variável | Descrição |
|---|---|
| `STORE_FILE` | Caminho para o arquivo keystore (`.jks` / `.keystore`) |
| `STORE_PASSWORD` | Senha do keystore |
| `KEY_ALIAS` | Alias da chave de assinatura |
| `KEY_PASSWORD` | Senha da chave de assinatura |

---

## GitHub Secrets Necessários para o CI/CD

Configure os seguintes segredos em **Settings → Secrets and variables → Actions** no repositório do GitHub:

### Assinatura do APK (`.github/workflows/android_build.yml`)
- `KEYSTORE_BASE64`: Conteúdo do arquivo `.jks` de release codificado em Base64 (`base64 -w 0 release.jks`).
- `STORE_PASSWORD`: Senha do keystore de release.
- `KEY_ALIAS`: Alias da chave de release.
- `KEY_PASSWORD`: Senha da chave de release.
- `DEBUG_KEYSTORE_BASE64` *(opcional)*: Keystore de debug em Base64 para manter a mesma assinatura SHA-1 entre execuções de CI.

### Distribuição e Regras do Firebase (`.github/workflows/android_build.yml` & `.github/workflows/firebase-app-distribution.yml`)
- `FIREBASE_APP_ID`: ID do aplicativo Android no Firebase (ex.: `1:...:android:...`).
- `FIREBASE_APP_DISTRIBUTION_CREDENTIALS`: JSON completo da Service Account do Google Cloud/Firebase com permissão de **Firebase App Distribution Admin** (substitui o antigo `FIREBASE_TOKEN`).
- `FIREBASE_SERVICE_ACCOUNT_JSON` *(opcional)*: JSON da Service Account para deploy automatizado de regras ou validações administrativas.

---

## Como Compilar e Rodar os Testes

Para compilar o APK de Debug:

```bash
./gradlew assembleDebug
```

Para executar a suíte completa de testes unitários e Robolectric (incluindo regressão de segurança, cálculo de macros, sincronização Delta e validação do catálogo JSON de exercícios):

```bash
./gradlew testDebugUnitTest
```

*(Em ambientes onde o wrapper não estiver habilitado, utilize `gradle assembleDebug` e `gradle :app:testDebugUnitTest`).*

---

## Como Fazer Deploy das Regras do Firestore

O único arquivo oficial de regras de segurança do banco de dados é o `firestore.rules` localizado na **raiz** do projeto (referenciado pelo `firebase.json`).

Para validar e publicar as regras no seu projeto Firebase:

```bash
firebase deploy --only firestore:rules
```

Para executar os testes locais das regras de segurança com o Firebase Emulator:

```bash
firebase emulators:exec --only firestore "node --test firestore.rules.test.mjs"
```
