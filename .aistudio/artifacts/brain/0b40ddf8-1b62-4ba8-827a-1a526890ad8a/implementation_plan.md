# Refactor Firebase and Preserve Workouts

Refactor the Firebase architecture across the application while preserving and enhancing the workout tracking system, integrating Google Sign-In via Credential Manager, Firestore with custom database ID, and robust offline local caching with Room.

## User Review & Critical Decisions

> [!IMPORTANT]
> - **Authentication**: Google Sign-In via Jetpack Credential Manager with interactive fallback (`GetSignInWithGoogleOption`).
> - **Data Persistence & Sync**: Local Room database paired with Firestore cloud sync (matching user preference).
> - **Visual Theme**: Electric Blue and Dark Steel Material 3 theme across all workout and app screens.
> - **Workout Preservation**: All workout routines, logs, history, and progression data are fully preserved and migrated to the refactored architecture.

## 1. Overview & Core Concept

- **What It Does**: Cleanly refactors the Firebase data and auth layer, ensuring strict adherence to zero-trust security rules, secure Google Sign-In, scoped user isolation (`/users/{uid}/workouts`), and seamless offline-first synchronization using Room and Firestore.
- **Target Audience / Persona**: Fitness enthusiasts and athletes looking for a robust, reliable, cloud-synced workout tracker.
- **Key Value**: Flawless sync, robust error handling (`handleFirestoreError`), zero unauthenticated leaks, and preserved workout history.

## 2. User Experience & Visual Design

- **Key User Flows**:
  1. App launch -> Auth check -> Google Sign-In (if unauthenticated) or Dashboard (if authenticated).
  2. Workout management -> Create, edit, log workouts and exercises -> Real-time local Room persistence + Firestore background sync.
  3. Analytics & Progress -> Track volume, PRs, and cardio with clean Material 3 charts.
- **Visual Identity & Theme**:
  - *Aesthetic Direction*: Modern, high-performance fitness tracker (Electric Blue primary accents `#3B82F6`, dark steel background `#121212`, card surfaces `#1E1E1E`).
  - *Typography & Hierarchy*: Material 3 typography with clear weights for workout headers, timers, and stat callouts.
  - *Component Styling*: M3 Cards, Floating Action Buttons, Bottom/Rail Navigation, and clean interactive inputs.

## 3. Key Product Decisions & Trade-Offs

- **Decision 1**: Room + Firestore Dual-Layer Persistence.
  - *Chosen Approach*: Local Room cache for instant offline responsiveness + Firestore real-time sync.
  - *Why*: Ensures workouts are never lost offline while providing seamless cross-device cloud sync.
- **Decision 2**: Credential Manager for Google Sign-In.
  - *Chosen Approach*: Jetpack Credential Manager (`GetSignInWithGoogleOption`).
  - *Why*: Standardized, secure, modern Android authentication standard.

## 4. Technical Architecture & Data Strategy

```
┌────────────────────────────────────────────────────────┐
│                      Jetpack Compose UI                 │
│   (WorkoutScreen, DashboardScreen, AuthScreen, etc.)   │
└───────────────────────────┬────────────────────────────┘
                            │ Collects State / Flows
┌───────────────────────────▼────────────────────────────┐
│                       ViewModels                       │
│              (WorkoutTrackerViewModel, etc.)           │
└───────────────────────────┬────────────────────────────┘
         ┌──────────────────┴──────────────────┐
         ▼                                     ▼
┌──────────────────┐                 ┌──────────────────┐
│   AppDatabase    │                 │  Firestore DB    │
│  (Room Local DB) │◄───────────────►│  (Cloud Sync)    │
└──────────────────┘                 └──────────────────┘
```

- **Data Models**: `WorkoutRoutine`, `ExerciseSet`, `WorkoutLog`, scoped to `userId`.
- **Security**: Strict Firestore rules (`request.auth != null && request.auth.uid == userId`) and fail-fast `requireUserId()`.
