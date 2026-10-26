# Guia de Configuração Definitiva do Firebase App Check e Certificados SHA-256

Este documento orienta a configuração completa do **Firebase App Check** e o cadastro dos certificados digitais (**SHA-1** e **SHA-256**) no **Firebase Console**, eliminando permanentemente erros de **reCAPTCHA**, desafios via WebView e falhas de credenciais (`ERROR_CAPTCHA_CHECK_FAILED`, `ERROR_WEB_CONTEXT_CANCELED`, `ERROR_APP_NOT_AUTHORIZED`, `ERROR_INVALID_CREDENTIAL`, `The supplied auth credential is incorrect, malformed or has expired`).

---

## 1. Identificadores e Certificados do Projeto

Os seguintes valores representam os identificadores exatos utilizados pelo aplicativo:

| Parâmetro | Valor Configurado |
|---|---|
| **Package Name (Application ID)** | `com.aistudio.growthdiet.kxmfzq` |
| **Secondary Package** | `com.tecpatrick.virtus` |
| **Firebase Project ID** | `gen-lang-client-0338831300` |
| **Firebase Project Number** | `204365877775` |
| **Firebase App ID (Android)** | `1:204365877775:android:f77650214127790642497a` |
| **Debug Keystore** | `debug.keystore` (alias: `androiddebugkey`) |
| **SHA-1 Fingerprint** | `5C:F2:E9:92:D0:DC:08:2F:95:4A:26:14:55:AE:9A:0F:78:5F:44:D3` |
| **SHA-256 Fingerprint** | `79:67:6D:0E:34:49:21:D3:0C:34:4D:B7:7A:F0:9A:0B:93:0A:BE:DA:36:4D:AF:3F:7D:44:FC:33:F8:B2:94:FF` |

> **Nota:** Para sistemas que exigem o formato hexadecimal contínuo (sem dois-pontos):  
> `79676D0E344921D30C344DB77AF09A0B930ABEDA364DAF3F7D44FC33F8B294FF`

---

## 2. Por que os Erros de reCAPTCHA e Credenciais Ocorrem?

O Firebase Authentication e o Firebase App Check implementam mecanismos de proteção contra abuso e bots:

1. **Ausência da Fingerprint SHA-256 no Console:**  
   Quando o aplicativo tenta autenticar um usuário (e-mail/senha, Google Sign-In ou verificação de telefone) e a assinatura digital da APK (SHA-256) não está cadastrada no Firebase Console, o Firebase não consegue validar a autenticidade do aplicativo via Play Integrity. Como contingência, ele tenta abrir um navegador ou WebView para exibir um desafio reCAPTCHA. Se o reCAPTCHA falhar ou for fechado pelo usuário, ocorre o erro `ERROR_CAPTCHA_CHECK_FAILED`, `ERROR_WEB_CONTEXT_CANCELED` ou a mensagem genérica **"The supplied auth credential is incorrect, malformed or has expired"**.
2. **App Check Ativado sem Token de Debug:**  
   Em builds locais ou de emulador (`debug`), o aplicativo utiliza o provedor `DebugAppCheckProviderFactory`. Se o token de debug gerado pelo dispositivo não estiver na lista de permissões (*allowlist*) do Firebase Console, as requisições para os serviços protegidos (Authentication, Firestore, AI Logic) são rejeitadas.

---

## 3. Passo a Passo no Firebase Console

### Passo 1: Cadastrar a Impressão Digital SHA-256 no Projeto

1. Acesse o **Firebase Console**: [https://console.firebase.google.com/](https://console.firebase.google.com/).
2. Selecione o projeto: **`gen-lang-client-0338831300`**.
3. No menu lateral esquerdo, clique no ícone de engrenagem ⚙️ ao lado de **Visão geral do projeto** e selecione **Configurações do projeto** (*Project Settings*).
4. Na aba **Geral** (*General*), role a página até a seção **Seus aplicativos** (*Your apps*).
5. Selecione o aplicativo Android: **`com.aistudio.growthdiet.kxmfzq`** (App ID: `1:204365877775:android:f77650214127790642497a`).
6. Na seção **Certificados de assinatura SHA** (*SHA certificate fingerprints*), clique em **Adicionar impressão digital** (*Add fingerprint*).
7. Cole o SHA-256 atual:
   ```
   79:67:6D:0E:34:49:21:D3:0C:34:4D:B7:7A:F0:9A:0B:93:0A:BE:DA:36:4D:AF:3F:7D:44:FC:33:F8:B2:94:FF
   ```
8. Verifique se o SHA-1 também está presente:
   ```
   5C:F2:E9:92:D0:DC:08:2F:95:4A:26:14:55:AE:9A:0F:78:5F:44:D3
   ```
9. Clique em **Salvar**.
10. *(Opcional)* Se novos clientes OAuth forem atualizados, faça o download do arquivo `google-services.json` atualizado e substitua em `app/google-services.json`.

---

### Passo 2: Configurar o Provedor Play Integrity no App Check

1. No menu lateral do Firebase Console, clique em **App Check** (sob o menu **Build**).
2. Na aba **Apps**, localize o aplicativo **`com.aistudio.growthdiet.kxmfzq`**.
3. Clique em **Play Integrity** (ou **Registrar**).
4. Vincule ao seu projeto do Google Play Console ou confirme a integração com o Play Integrity.
5. Defina o **TTL do token** (Tempo de vida recomendado: `1 hora` a `7 dias`).
6. Clique em **Salvar**.

---

### Passo 3: Cadastrar Token de Debug para Testes Locais e Emuladores

Em builds de desenvolvimento (`BuildConfig.DEBUG = true`), o código do aplicativo (`AppCheckInitializer.kt` e `MainApplication.kt`) instala automaticamente o `DebugAppCheckProviderFactory`.

Para autorizar seu ambiente de desenvolvimento:

1. Execute o aplicativo em seu dispositivo ou emulador.
2. Abra o **Logcat** no Android Studio e filtre por:
   ```text
   tag:FirebaseAppCheck
   ```
3. Você verá uma mensagem semelhante a esta:
   ```text
   D/FirebaseAppCheck: Enter this debug secret into the allow list in the Firebase Console for your project:
   a1b2c3d4-e5f6-7890-abcd-ef1234567890
   ```
4. Copie o UUID gerado.
5. No Firebase Console, vá em **App Check** -> aba **Apps**.
6. Clique nos três pontinhos verticais (`⋮`) ao lado do app `com.aistudio.growthdiet.kxmfzq` e selecione **Gerenciar tokens de depuração** (*Manage debug tokens*).
7. Clique em **Adicionar token de depuração** (*Add debug token*).
8. Dê um nome descritivo (ex.: `Emulador-Dev-Virtus`) e cole o UUID copiado.
9. Clique em **Salvar**.

> **Dica:** Uma vez registrado o token de depuração, o emulador terá acesso direto sem nunca acionar reCAPTCHA ou bloqueios de segurança.

---

### Passo 4: Habilitar a API Google Play Integrity no Google Cloud

Certifique-se de que a API está ativa no projeto do Google Cloud associado:

1. Acesse o [Google Cloud Console - APIs & Services](https://console.cloud.google.com/apis/dashboard?project=gen-lang-client-0338831300).
2. Verifique se **Google Play Integrity API** está listada em **APIs e serviços ativados**.
3. Caso não esteja, pesquise por "Google Play Integrity API" e clique em **Ativar** (*Enable*).

---

## 4. Como Diagnosticar Falhas com o `AuthLogger`

Para facilitar a identificação imediata da causa raiz de qualquer erro de autenticação, o aplicativo possui a classe utilitária **`AuthLogger`** (`com.example.util.AuthLogger`), que intercepta todos os eventos de login e mudanças de estado.

### Filtrando no Logcat:
```bash
adb logcat -s VirtusAuthLogger:V AppCheckInitializer:V
```

### Distinção Automática no Log:

#### Exemplo 1: Senha Incorreta (`WRONG_PASSWORD`)
O `AuthLogger` detecta que os parâmetros eram válidos, a requisição chegou ao servidor do Firebase, mas a credencial fornecida não confere:
```text
======================================================================
❌ [AUTH FAILURE] Method: EMAIL_PASSWORD
├─ Target: p***k@gmail.com
├─ Scenario Diagnosis: WRONG_PASSWORD
├─ Is Wrong Password? YES (Check user credentials)
├─ Is Invalid Request? NO
├─ Is App Check / Recaptcha Error? NO
├─ Firebase Error Code: ERROR_WRONG_PASSWORD
├─ Exception Class: com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
├─ Message: The password is invalid or the user does not have a password.
├─ Summary: The request was syntactically valid and reached Firebase, but the password provided was incorrect for this user.
└─ Actionable Hint: Prompt user to verify password or trigger the sendPasswordReset flow.
======================================================================
```

#### Exemplo 2: Requisição Inválida (`INVALID_REQUEST`)
O `AuthLogger` detecta parâmetros malformados antes do envio ou rejeitados pela validação cliente/servidor:
```text
======================================================================
❌ [AUTH FAILURE] Method: EMAIL_PASSWORD
├─ Target: formato_invalido
├─ Scenario Diagnosis: INVALID_REQUEST
├─ Is Wrong Password? NO
├─ Is Invalid Request? YES (Client validation or malformed payload)
├─ Is App Check / Recaptcha Error? NO
├─ Firebase Error Code: CLIENT_INVALID_REQUEST
├─ Exception Class: java.lang.IllegalArgumentException
├─ Message: Digite um e-mail válido.
├─ Summary: The login request was malformed, incomplete, or failed parameter validation before/at server dispatch.
└─ Actionable Hint: Verify email format, non-empty fields, and parameter encodings before calling Firebase Auth APIs.
======================================================================
```

#### Exemplo 3: Falha de App Check ou reCAPTCHA (`APP_CHECK_OR_RECAPTCHA`)
O `AuthLogger` detecta explicitamente a falha de atestação e instrui o desenvolvedor:
```text
======================================================================
❌ [AUTH FAILURE] Method: EMAIL_PASSWORD
├─ Target: p***k@gmail.com
├─ Scenario Diagnosis: APP_CHECK_OR_RECAPTCHA
├─ Is Wrong Password? NO
├─ Is Invalid Request? NO
├─ Is App Check / Recaptcha Error? YES (Verify SHA-256 in Firebase Console!)
├─ Firebase Error Code: ERROR_CAPTCHA_CHECK_FAILED
├─ Summary: Firebase App Check or reCAPTCHA verification failed.
└─ Actionable Hint: Add SHA-256 fingerprint (79:67:6D:0E:34:49:21:D3:0C:34:4D:B7:7A:F0:9A:0B:93:0A:BE:DA:36:4D:AF:3F:7D:44:FC:33:F8:B2:94:FF) to Firebase Console -> Project Settings -> Android Apps, or whitelist App Check debug token in Firebase Console.
======================================================================
```

---

## 5. Transição para Build de Produção (Release)

Ao gerar o APK ou Android App Bundle (AAB) para a Google Play Store:

1. Se você utilizar o **Google Play App Signing**, o Google Play re-assina seu aplicativo com uma chave de produção gerenciada pelo Google.
2. Acesse o **Google Play Console** -> Seu Aplicativo -> **Configuração** (*Setup*) -> **Integridade do aplicativo** (*App integrity*).
3. Na seção **Certificado da chave de assinatura do app** (*App signing key certificate*), copie o **SHA-256**.
4. Cole esse novo SHA-256 no Firebase Console seguindo o mesmo procedimento do [Passo 1](#passo-1-cadastrar-a-impressão-digital-sha-256-no-projeto).
5. O Play Integrity em produção validará o aplicativo sem necessidade de tokens de depuração.
