# Plano de Implementação: Regras de Segurança do Firestore e Firebase App Check (Play Integrity)

Implementação de regras de segurança estritas no Firestore para isolamento de dados por usuário e integração do Firebase App Check com o Play Integrity provider no aplicativo Android VIRTUS.

---

## 1. Regras de Segurança do Firestore (`firestore.rules`)
- **Isolamento por Usuário (`/users/{userId}`):**
  - Restringir leitura, escrita, atualização e deleção em subcoleções de `/users/{userId}` (`diets`, `workouts`, `food_records`, `progress_logs`, `cardio_logs`, `gps_routes`) exclusivamente ao proprietário do documento (`request.auth.uid == userId`).
- **Validações de Integridade dos Dados:**
  - Garantir validação dos tipos e campos obrigatórios de dieta e treino.
- **Princípio do Menor Privilégio (Zero-Trust):**
  - Negação explícita por padrão para qualquer tentativa de acesso fora do caminho do usuário.

## 2. Integração do Firebase App Check
- **Adição de Dependências Gradle:**
  - `firebase-appcheck-playintegrity` para validação de atestação do app via Google Play Services em produção.
  - `firebase-appcheck-debug` para permitir desenvolvimento e testes locais sem interrupção.
- **Inicialização em `VirtusApplication.kt`:**
  - Configurar `FirebaseAppCheck.getInstance().installAppCheckProviderFactory(...)` na inicialização do aplicativo (`onCreate`).
  - Alternar automaticamente entre `PlayIntegrityAppCheckProviderFactory` em Release e `DebugAppCheckProviderFactory` em Debug.

## 3. Validação e Verificação
- Atualizar o arquivo `firestore.rules`.
- Executar `compile_applet` para verificar que a compilação permanece limpa e funcional.
