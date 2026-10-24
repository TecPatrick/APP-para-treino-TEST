# Muscle Heatmap Redesign, Firestore Subcollection Migration & Logging Implementation Plan

Redesign the muscle heatmap with polished anatomical silhouettes (matching user reference), implement Firestore subcollection migration to `/users/{userId}/workouts/{workoutId}/exercises/{exerciseId}`, add structured logging for Firestore writes, and refactor volume aggregation.

## User Review & Critical Decisions

> [!IMPORTANT]
> - **Heatmap UI**: Clean anatomical body silhouettes (anterior & posterior) with smooth gradient intensity coloring (Blue to Orange), without breakdown lists.
> - **Firestore Migration**: Migrate existing exercises to nested subcollections under `/users/{userId}/workouts/{workoutId}/exercises/{exerciseId}` with `'free'` as default ID for unassigned workouts.
> - **Logging**: Centralized Firestore operation logger capturing auth state, document paths, and data structures before writing.

## 1. Overview & Core Concept

- **What It Does**: Provides a polished Hevy-style muscle heatmap UI in the profile, ensures bulletproof Firestore subcollection synchronization, and logs all database operations.
- **Key Value**: Professional visual polish and reliable cloud data structure.

## 2. User Experience & Visual Design

- **Heatmap Redesign**: Elegant dark theme card housing anterior and posterior anatomical body models, with interactive muscle selection and smooth intensity gradients.

## 3. Technical Architecture & Data Strategy

```
┌────────────────────────────────────────────────────────┐
│               UserProfileScreen (UI)                   │
│  - Redesigned MuscleHeatmapView (Body Silhouettes)     │
└───────────────────────────▲────────────────────────────┘
                            │
┌───────────────────────────┴────────────────────────────┐
│                 VirtusViewModel                        │
│  - getMuscleVolume(days) [Map-Reduce aggregation]      │
└───────────────────────────▲────────────────────────────┘
                            │
┌───────────────────────────┴────────────────────────────┐
│          VirtusRepository / FirestoreSyncManager       │
│  - migrateExercisesToSubcollection()                   │
│  - Centralized Firestore Logger                        │
└────────────────────────────────────────────────────────┘
```
