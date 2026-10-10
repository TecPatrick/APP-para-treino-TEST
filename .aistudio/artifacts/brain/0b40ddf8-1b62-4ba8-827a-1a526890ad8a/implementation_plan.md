# Plano de Distribuição Gratuita do Aplicativo Android

Este plano detalha as melhores plataformas 100% gratuitas para hospedar e distribuir o seu aplicativo Android, com foco especial na opção escolhida pelo usuário: **GitHub Releases**.

---

## 1. Plataformas Gratuitas para Hospedar seu App Android

1. **GitHub Releases (Recomendado & Escolhido)**
   - **Custo:** R$ 0,00 (Gratuito para repositórios públicos e privados).
   - **Vantagens:** Controle total de versões, histórico de atualizações, links diretos permanentes para download do APK (`app-release.apk`), e integração com controle de versão.
   - **Como funciona:** O usuário acessa a página de "Releases" do seu repositório no GitHub e baixa o APK diretamente no celular com um clique.

2. **F-Droid (Para Apps de Código Aberto / Open Source)**
   - **Custo:** R$ 0,00.
   - **Vantagens:** Repositório oficial focado em aplicativos livres e de código aberto. Excelente para alcance público.
   - **Requisito:** O código-fonte precisa ser aberto (licença livre) e atender aos critérios estritos da comunidade F-Droid.

3. **Hospedagem Web Estática (Netlify, Vercel, GitHub Pages) + Landing Page**
   - **Custo:** R$ 0,00.
   - **Vantagens:** Você cria uma página web simples (Landing Page) apresentando o app e disponibilizando o botão de download direto do APK hospedado na mesma plataforma ou no próprio GitHub.

4. **Armazenamento em Nuvem (Google Drive, OneDrive, Mega)**
   - **Custo:** R$ 0,00 (dentro dos limites gratuitos, ex: 15 GB no Google Drive).
   - **Vantagens:** Muito simples para envio rápido para amigos e familiares.
   - **Desvantagens:** Pouco profissional para distribuição em larga escala e pode gerar avisos de link direto ou limite de downloads.

---

## 2. Passo a Passo para Distribuir via GitHub Releases

Como você escolheu o **GitHub Releases**, siga o procedimento abaixo para publicar seu aplicativo de forma profissional e gratuita:

### Passo 1: Gerar o APK de Release no Android Studio / AI Studio
No AI Studio, você pode exportar o projeto como ZIP ou utilizar o APK gerado pelo build system. (O APK padrão de debug ou release gerado pelo Gradle pode ser encontrado em `app/build/outputs/apk/release/app-release.apk` ou `debug/app-release.apk`).

### Passo 2: Criar um Repositório no GitHub
1. Acesse [github.com](https://github.com) e faça login.
2. Clique em **New repository** (Novo repositório).
3. Defina um nome (ex: `meu-app-fitness`), deixe como **Public** (Público) para facilitar o acesso aos downloads, e clique em **Create repository**.
4. Faça o upload dos arquivos do seu projeto (ou use o Git local para dar `git push`).

### Passo 3: Criar uma Release no GitHub
1. Na página principal do seu repositório no GitHub, clique na opção **Releases** na barra lateral direita (ou acesse `https://github.com/seu-username/seu-repositorio/releases`).
2. Clique em **Create a new release** (Criar uma nova release).
3. No campo **Tag version**, digite a versão do app (ex: `v1.0.0`).
4. No título e na descrição, escreva as novidades da versão (Release Notes), como:
   - "Versão inicial do aplicativo de treino com diário, cronômetro e calculadora de água!"
5. Na seção **Attach binaries**, arraste e solte o arquivo `.apk` do seu aplicativo.
6. Clique em **Publish release**.

### Passo 4: Compartilhar com os Usuários
Pronto! O GitHub fornecerá um link direto permanente para o seu APK (ex: `https://github.com/seu-username/seu-repositorio/releases/download/v1.0.0/app-release.apk`). Você pode enviar esse link para qualquer pessoa baixar e instalar o app no Android.
