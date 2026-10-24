# Security Audit & Hardening Plan

## 1. Overview
Perform a comprehensive security audit of the current Android project to identify and mitigate credential exposure, improve configuration management, and ensure robust Firebase/Firestore security.

## 2. Audit Tasks
- **Credential Scanning**: Search the entire project for hardcoded secrets (API keys, service account keys, tokens).
- **Configuration Review**: Audit `build.gradle.kts`, `AndroidManifest.xml`, `google-services.json`, and any `.properties` or `.env` files.
- **Log Sanitation**: Remove sensitive logs or debug files (`firebase-debug.log`).
- **Secrets Management**: Refactor any hardcoded secrets found to use `BuildConfig` via secure injection (Secrets panel).
- **Firestore Rules Review**: Audit `firestore.rules` to ensure zero-trust access control.

## 3. Verification & Build
- **Dependency Audit**: Ensure no credentials are in project files.
- **Security Validation**: Run `lint_applet` for potential security-related lint warnings.
- **Build Verification**: Run `compile_applet`.
- **APK Generation**: Run `gradle assembleDebug` to confirm buildability.

## 4. Reporting
Upon completion, report:
- Affected files.
- Any secrets found and mitigation actions taken (rotation/revocation).
- Build/Test results.
- Status of APK generation.
