# Muscle Heatmap & Workout Volume Aggregation Implementation Plan

Implement an interactive muscle heatmap in the Profile tab (Hevy-style) with timeframe filtering (7 days, 30 days, all-time) using a Blue to Orange intensity scale rendered via Jetpack Compose Canvas, backed by volume aggregation logic in `VirtusViewModel`.

## User Review & Critical Decisions

> [!IMPORTANT]
> - **Color Palette**: Blue to Orange intensity scale.
> - **Timeframe Selector**: Filter chips above the heatmap.

## 1. Overview & Core Concept

- **What It Does**: Aggregates completed workout logs and routine exercise data to calculate training volume per muscle group across 7 days, 30 days, and all-time, displaying the intensity visually on an interactive human body silhouette in the Profile screen.
- **Target Audience**: Fitness enthusiasts looking for visual feedback on muscle group training frequency and volume.
- **Key Value**: Gamified muscle recovery and training frequency tracking directly in the user profile.

## 2. User Experience & Visual Design

- **Aesthetic Direction**: Premium fitness dark theme (`CarbonCard`, `GrowthOrange`, `GrowthCyan`, deep blue-to-orange gradient highlights).
- **Component Styling**:
  - **Filter Chips**: Horizontal scrollable chips for "7 Dias", "30 Dias", and "Histórico".
  - **Heatmap Canvas**: Clean vector-style muscle group silhouettes (Chest, Back, Shoulders, Biceps, Triceps, Legs, Abs) colored with dynamic opacity based on volume relative to max volume, transitioning from Cool Blue (low) to Warm Orange (high).

## 3. Key Product Decisions & Trade-Offs

- **Decision 1**: Volume Distribution across Target Muscle Groups.
  - *Chosen Approach*: Distribute `WorkoutLog` total volume evenly across the `targetMuscleGroups` defined in the associated `WorkoutRoutine`.
  - *Why*: Ensures robustness even when individual exercise sets aren't stored in isolated historical records.

## 4. Technical Architecture & Data Strategy

```
┌────────────────────────────────────────────────────────┐
│               UserProfileScreen (UI)                   │
│  - Filter Chips (7d, 30d, All)                         │
│  - MuscleHeatmapView (Canvas Silhouette & Overlays)    │
└───────────────────────────▲────────────────────────────┘
                            │ observes volume map
┌───────────────────────────┴────────────────────────────┐
│                 VirtusViewModel                        │
│  - getMuscleVolume(days: Int): Map<String, Float>      │
└───────────────────────────▲────────────────────────────┘
                            │ queries
┌───────────────────────────┴────────────────────────────┐
│          VirtusRepository / Room Database              │
└────────────────────────────────────────────────────────┘
```
