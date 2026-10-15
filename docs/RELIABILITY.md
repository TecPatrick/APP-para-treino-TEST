# Reliability changes

## Authentication

Email/password errors no longer produce a successful local session, in either the Firebase service or UI controller. Registration does not persist a profile on authentication failure. Guest access remains an explicit action and signs out of Firebase. Biometric unlock requires an existing matching Firebase session or an explicit guest profile. Plaintext passwords are no longer written; the legacy column is cleared when opening the production database and when migrating.

Authentication UI logic lives in `ui/auth/AuthController.kt`; synchronization UI logic lives in `ui/sync/SyncController.kt`. The existing ViewModel delegates to them to preserve screen contracts. Workout cards and dialogs have been extracted into separate files without changing their implementations. This is an incremental extraction, not a complete rewrite of all feature ViewModels.

## Database

The model files at commit `8aecd36ba06757ed02c7fb7e4ae5ebdb9efc8e24` (last v1) are identical to the v2 models. The explicit 1→2 migration preserves these tables and clears the legacy password field. Room validates the resulting schema. There is no destructive fallback.

Older historical builds reused version 1 while changing tables. Their schema is not silently guessed: if incompatible, Room refuses to open it instead of erasing it. Back up and inspect such a database before adding a specific migration. Deleted data from an already-executed destructive migration cannot be recovered by this change.

Schema export is enabled in `app/schemas`. Commit generated schemas with future schema changes and provide a tested migration for every version increment.

## Synchronization semantics

Room remains the local store. Firestore's existing persistent write queue handles offline retries; this change does not introduce a second Room outbox or a background worker. A write is counted as confirmed only when Firebase acknowledges it. After five seconds without confirmation it is reported as pending, not successful; the Firebase task is not cancelled. Rejected writes are failures and can be retried with the sync button after correcting the problem. Repeating the same locally identified record uses the same cloud document ID.

The sync summary is a snapshot of that attempt, not a live count of every outstanding write. Re-run synchronization to refresh confirmation. It includes profile, goals, workout history, progress and GPS logs, not all application tables. Goals and their profile summary use one atomic batch. Local data is read directly from Room rather than possibly uncollected screen StateFlows. Simultaneous sync button presses are blocked. Guest sessions cannot upload data.

## Firebase configuration

Firebase AI Logic with the Gemini Developer API, Email/Password and Google Sign-In are enabled in project `gen-lang-client-0338831300`. The Android configuration for `com.aistudio.growthdiet.kxmfzq` is stored at `app/google-services.json`. The app uses the current no-Blaze model `gemini-3.8-flash`.

Before a release build:

1. Register debug App Check tokens only for local development. Release builds use Play Integrity; register the release signing certificate and configure the appropriate attestation policy for the distribution channel.
2. Confirm App Check enforcement and configure quotas/usage monitoring for AI Logic in the Firebase console.
3. If a real Gemini key was shipped in an old APK, revoke/rotate it in the provider console. Removing the code does not revoke old keys.

References:
- https://firebase.google.com/docs/ai-logic/get-started?platform=android
- https://firebase.google.com/docs/app-check/android/play-integrity-provider

## Validation

The PR workflow runs `gradle --no-daemon testDebugUnitTest` followed by `assembleDebug`.
Regression tests cover invalid login, failed registration/reset, confirmation versus pending/rejected writes, cancellation, and v1→v2 profile preservation.
Manual device checks still required: guest/login/logout flow, migration from an installed APK, offline save → process restart → reconnect, Firebase/App Check integration, and workout screen navigation.

APKs belong in Actions artifacts or Releases, not in source control. Removing APK files from the current tree does not remove them from Git history.
