# Plano: segurança e performance do app Virtus (Android)

> Prompt para o AI Studio. Execute **uma fase por vez**. Ao final de cada fase, o projeto deve compilar (`./gradlew assembleDebug assembleRelease`) e os testes devem passar (`./gradlew testDebugUnitTest`). **Não altere funcionalidades nem a experiência do usuário.**

## Contexto (reanálise de 08/11/2026)
O que já foi corrigido: `my-upload-key.jks`, `TestAuthSettings.kt` e `app/applet/` foram removidos, as imagens foram convertidas para WebP, o README foi criado, o `VirtusViewModel` foi dividido, os backups estão desativados e a UI usa `collectAsStateWithLifecycle`.

Problemas que ainda existem:

| # | Área | Problema | Severidade |
|---|------|----------|-----------|
| S1 | Segurança | `firebase-appcheck-debug` declarado como `implementation`, ou seja, também vai no APK de release | 🔴 Alta |
| S2 | Segurança | `AuthLogger` registra **UID e e-mail** com `Log.i`/`Log.d`, e `FirestoreSyncManager`/`WorkoutMigrationService` registram o UID sem checar `BuildConfig.DEBUG` | 🔴 Alta |
| S3 | Segurança/Dados | `fallbackToDestructiveMigration()` em `AppDatabase.kt`: uma migração que falhar **apaga os dados locais** do usuário | 🔴 Alta |
| S4 | Segurança | Regras do Firestore: `isValidSubcollectionDoc` só valida o `userId`, sem limite de tamanho ou de campos (risco de abuso de cota e custo) | 🟠 Média |
| S5 | Segurança | Coleções duplicadas nas regras (`workout_logs`/`workoutLogs`, `cardio_logs`/`cardioLogs`, `shoppingItems`/`shoppingList`, `workoutExercises` em três lugares): superfície de ataque maior | 🟠 Média |
| S6 | Segurança | Não existe `network_security_config.xml` (bloqueio explícito de cleartext) | 🟡 Baixa |
| S7 | Segurança | `ACCESS_BACKGROUND_LOCATION` declarada, mas o GPS roda em foreground service: permissão desnecessária, que gera revisão rigorosa na Play Store | 🟠 Média |
| S8 | Segurança | `logging-interceptor` incluído no release | 🟡 Baixa |
| S9 | Segurança | `applicationId = com.aistudio.growthdiet.kxmfzq` e `namespace = com.example` | 🟡 Baixa |
| S10 | Segurança | 19 chamadas a `e.printStackTrace()` (vazam detalhes no logcat em release) | 🟡 Baixa |
| P1 | Performance | `proguard-rules.pro` mantém pacotes inteiros (`com.example.ui.**`, `util.**`, `com.google.firebase.**`, `com.google.android.gms.**`, `coil.**`): **o R8 quase não reduz nem ofusca nada** | 🔴 Alta |
| P2 | Performance | Dependências duplicadas: `okhttp` e `retrofit` declarados duas vezes, e Moshi + kotlinx.serialization usados juntos | 🟠 Média |
| P3 | Performance | `material-icons-extended` (biblioteca enorme) | 🟠 Média |
| P4 | Performance | 8 `addSnapshotListener(MetadataChanges.INCLUDE)`: callbacks extras a cada mudança de metadados | 🟠 Média |
| P5 | Performance | Compose: 22 `items()` sem `key`, ordenação dentro de `items(...sortedByDescending{})`, nenhum uso de `derivedStateOf` | 🟠 Média |
| P6 | Performance | Arquivos gigantes que causam recomposição ampla: `DietScreen` (94 KB), `UserProfileScreen` (81 KB), `GpsTrackerScreen` (80 KB), `FirestoreSyncManager` (61 KB), `MuscleHeatmapView` (48 KB) | 🟠 Média |
| P7 | Performance | `fitnessprogramer_exercises.json` (230 KB) carregado em runtime; é preciso garantir que o parse acontece fora da main thread e uma única vez | 🟡 Baixa |
| P8 | Performance | Sem Baseline Profile; Compose BOM 2024.09 e Kotlin 2.0.20 desatualizados | 🟡 Baixa |
| P9 | Performance | ViewModels duplicados: `ShoppingViewModel` e `ShoppingListViewModel` | 🟡 Baixa |

---

## FASE 1: Segurança crítica

### 1.1 App Check debug só no build de debug (S1)
Em `app/build.gradle.kts`:
```kotlin
debugImplementation(libs.firebase.appcheck.debug)
```
Remova o `implementation(libs.firebase.appcheck.debug)`. Como `AppCheckInitializer` referencia `DebugAppCheckProviderFactory`, crie dois source sets:
- `app/src/debug/java/com/example/AppCheckProvider.kt` → retorna `DebugAppCheckProviderFactory.getInstance()`
- `app/src/release/java/com/example/AppCheckProvider.kt` → retorna `PlayIntegrityAppCheckProviderFactory.getInstance()`

O `AppCheckInitializer` passa a chamar só `AppCheckProvider.factory()`.

### 1.2 Logs sem dados pessoais (S2, S10)
- Crie `util/AppLog.kt`, um wrapper que **só registra logs quando `BuildConfig.DEBUG` for verdadeiro** e que mascara UIDs (mostra só os 4 primeiros caracteres) e e-mails (`p***@dominio.com`).
- Substitua todos os `Log.d/i/v/w/e` do projeto por `AppLog`.
- Em `AuthLogger.kt`, **nunca** registre e-mail nem UID completo, nem em debug.
- Substitua os 19 `e.printStackTrace()` por `AppLog.e(TAG, "mensagem", e)`.
- Mantenha o `-assumenosideeffects class android.util.Log` no ProGuard como segunda camada de proteção.

### 1.3 Migrações do Room sem perda de dados (S3)
- Remova `.fallbackToDestructiveMigration()` e `.fallbackToDestructiveMigrationOnDowngrade()` de `AppDatabase.kt`.
- Garanta que existem `Migration`s explícitas para cada salto de versão (com base nos schemas em `app/schemas/`), ou use `@AutoMigration`.
- Adicione testes de migração com `MigrationTestHelper` para cada versão (pode ampliar o `DatabaseMigrationTest.kt`).

### 1.4 Remover a localização em segundo plano (S7)
- Remova `ACCESS_BACKGROUND_LOCATION` do `AndroidManifest.xml`. O `RunningForegroundService` com `foregroundServiceType="location"` já é suficiente.
- Confirme que nenhum código pede essa permissão.

---

## FASE 2: Regras do Firestore

### 2.1 Unificar coleções (S5)
- Defina **um nome por coleção**, sempre em camelCase: `workoutLogs`, `cardioLogs`, `shoppingItems`, `workoutRoutines/{id}/exercises`.
- Atualize `FirestoreSyncManager`, `DietShoppingSyncService` e `WorkoutMigrationService` para usar só esses nomes. A leitura dos nomes antigos fica restrita à migração, que deve ser só de leitura e apagar o legado depois de migrar.
- Nas regras, deixe os caminhos legados apenas com `allow read, delete: if isOwner(userId);` (sem `create`/`update`), para remoção futura.

### 2.2 Validação mais rígida (S4)
Para cada coleção, crie um validador específico com:
- `request.resource.data.keys().hasOnly([...])` listando os campos permitidos
- tipos e limites (`string.size() <= N`, números com faixa, `list.size() <= N`)
- `request.resource.size() < 50 * 1024` (cerca de 50 KB por documento)
- `gpsRouteLogs`: limite o tamanho da lista de pontos (ex.: `<= 5000`)

Escreva testes das regras com `@firebase/rules-unit-testing` no emulador (`firebase emulators:exec`) e adicione um job no CI para rodá-los.

---

## FASE 3: Endurecimento da rede e do build

### 3.1 Network security config (S6)
Crie `res/xml/network_security_config.xml`:
```xml
<network-security-config>
  <base-config cleartextTrafficPermitted="false" />
</network-security-config>
```
E referencie no `<application>`: `android:networkSecurityConfig="@xml/network_security_config"`.

### 3.2 Dependências (S8, P2)
- `logging-interceptor` → `debugImplementation`, e adicione o interceptor só quando `BuildConfig.DEBUG` for verdadeiro.
- Remova as declarações duplicadas de `okhttp` e `retrofit`.
- Escolha **um** serializador. A recomendação é kotlinx.serialization: remova `moshi-kotlin`, `moshi-kotlin-codegen` (ksp) e `converter-moshi`, e migre os modelos que ainda usam Moshi.
- Apague as dependências comentadas que não são usadas.

### 3.3 Atualizações
- Kotlin para a versão 2.1.x estável mais recente (e o KSP correspondente), Compose BOM para a mais recente, Lifecycle 2.9.x.
- Mantenha o Dependabot ativo.

---

## FASE 4: R8 e tamanho do APK (P1, P3)

### 4.1 Regras de ProGuard enxutas
Reescreva `app/proguard-rules.pro`, **removendo**:
```
-keep class com.example.ui.** { *; }
-keep class com.example.service.** { *; }
-keep class com.example.util.** { *; }
-keep class com.example.data.local.** { *; }
-keep class com.example.data.repository.** { *; }
-keep class com.example.MainActivity { *; }
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }
-keep class coil.** { *; }
-keep class kotlinx.serialization.** { *; }
```
Mantenha só o necessário:
- Modelos serializados pelo Firestore (`toObject`/`set` com classes) → `-keep class com.example.data.model.** { <init>(); <fields>; }`, ou anote com `@Keep` apenas as classes enviadas ao Firestore.
- As regras oficiais do kotlinx.serialization (`@Serializable` / `Companion` / `serializer()`).
- Room, Firebase e Coil já trazem consumer rules próprias: **não adicione nada para eles**.

Valide com:
```
./gradlew assembleRelease
```
Depois instale o release e teste login, sync, treino, dieta, GPS, PDF e Gemini. Compare o tamanho do APK antes e depois e registre no PR.

### 4.2 Ícones
Troque `material-icons-extended` por `material-icons-core` e copie como `ImageVector` (em `ui/icons/`) apenas os ícones extended que são de fato usados. Se a troca for grande, mantenha a biblioteca, porque o R8 com regras corretas (4.1) já remove os ícones não usados.

---

## FASE 5: Firestore e sync (P4, P7)
- Troque `MetadataChanges.INCLUDE` por `MetadataChanges.EXCLUDE` nos listeners que não precisam saber de `hasPendingWrites`. Mantenha `INCLUDE` só onde a UI mostra o status "pendente".
- Garanta que **todos** os listeners são removidos (`ListenerRegistration.remove()`) no logout e no `onCleared`. Uma boa forma é encapsular cada um em um `callbackFlow` com `awaitClose { registration.remove() }`.
- Escopo dos listeners: ative só para as coleções da tela atual, ou use `limit()` com ordenação por data nos logs (`workoutLogs`, `cardioLogs`, `progressLogs`, `gpsRouteLogs`): por exemplo, os últimos 90 dias, com paginação para o histórico.
- Faça escritas em lote com `WriteBatch` (máximo de 500 operações). Confira que o `limit(450)` existente segue esse padrão.
- `fitnessprogramer_exercises.json` e `canonical_exercise_guides.json`: faça o parse em `Dispatchers.IO`, **uma vez só**, e guarde em cache em memória (ou pré-popule no Room na primeira execução).

---

## FASE 6: Compose (P5, P6, P9)
- Em todo `items(...)`, use `key = { it.id }` e, quando houver tipos diferentes, `contentType`.
- Tire ordenações e filtros de dentro do composable: calcule no ViewModel (`StateFlow` derivado com `map`/`combine`) ou use `remember(lista) { lista.sortedBy... }`.
- Use `derivedStateOf` para estados derivados de scroll ou entrada de texto (ex.: mostrar o FAB, filtros de busca em `DietScreen`).
- Use lambdas estáveis e marque as classes de UI State como `@Immutable` ou `@Stable`.
- Divida os arquivos grandes em composables menores e sem estado próprio (state hoisting), com no máximo ~600 linhas por arquivo:
  - `DietScreen.kt`, `UserProfileScreen.kt`, `GpsTrackerScreen.kt`, `AnalyticsScreen.kt`, `AuthAndOnboardingScreen.kt`
- `MuscleHeatmapView.kt`: guarde os `Path`s em cache com `remember` e desenhe em `Canvas`, sem recriar paths a cada recomposição.
- `GpsTrackerScreen`: limite as atualizações do mapa (ex.: no máximo 1 por segundo) e simplifique a polyline (Douglas-Peucker) quando houver mais de 1000 pontos.
- Junte `ShoppingViewModel` e `ShoppingListViewModel` em um só.
- Ative os Compose Compiler Reports (`composeCompiler { reportsDestination = ... }`) e corrija as classes instáveis que aparecerem.

---

## FASE 7: Startup e medição (P8)
- Crie o módulo `:baselineprofile` com o Macrobenchmark e gere um **Baseline Profile** cobrindo o startup, o dashboard e a tela de treino. Adicione `androidx.profileinstaller`.
- Tire inicializações pesadas do `MainApplication.onCreate` (migração, parse de JSON, sync): use inicialização lazy ou rode em background.
- Adicione o **StrictMode** no build de debug (`detectDiskReads`, `detectNetwork`, `penaltyLog`) e corrija o que ele apontar.
- Adicione o **Firebase Crashlytics** e o **Firebase Performance Monitoring** (opcionais) para acompanhar crashes e ANRs em produção.

---

## Critérios de aceite
- [ ] O APK de release **não contém** `DebugAppCheckProviderFactory` nem `HttpLoggingInterceptor` (verificar com o APK Analyzer)
- [ ] Nenhum log de release contém UID ou e-mail
- [ ] Sem `fallbackToDestructiveMigration`, com testes de migração passando
- [ ] Regras do Firestore com `hasOnly`, limites de tamanho e testes no emulador
- [ ] `ACCESS_BACKGROUND_LOCATION` removida
- [ ] `proguard-rules.pro` enxuto e APK de release funcionando em todos os fluxos
- [ ] Tamanho do APK registrado (antes → depois)
- [ ] `items()` com `key` e sem ordenação dentro dos composables
- [ ] Baseline Profile gerado
- [ ] `assembleRelease` e `testDebugUnitTest` passando

---

## Ações manuais (dono do projeto, NÃO a IA)
1. **Firebase Console → App Check:** ativar o **Enforce** para Firestore, Authentication e **Firebase AI Logic (Gemini)**. Sem isso, qualquer pessoa pode usar sua cota do Gemini.
2. **Firebase AI Logic:** definir limite de requisições por usuário e alerta de orçamento no Google Cloud Billing.
3. **Firebase Auth:** ativar a proteção contra enumeração de e-mails e definir políticas de senha.
4. **Google Cloud → APIs & Credentials:** restringir a API key do Android (presente no `google-services.json`) ao pacote e ao SHA-1 do app, e liberar só as APIs usadas.
5. **GitHub:** ativar Secret scanning e Push protection, e proteger a branch `main` (exigir PR e CI verde).
6. Fazer o deploy das regras novas: `firebase deploy --only firestore:rules`.
