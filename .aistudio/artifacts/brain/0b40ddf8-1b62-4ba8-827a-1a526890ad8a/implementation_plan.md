# Implementation Plan: Photo Diary & Auto Rest Timer

## 1. Overview
This plan outlines the implementation of a weekly Photo Diary for physical progress tracking and an automated, flexible Rest Timer for workout sessions.

## 2. Feature: Photo Diary
- **Data Model**: `PhotoEntry` (id, date, goalPhase, imagePath/uri, note).
- **Persistence**: Room for local storage, Firestore for cloud backup/sync.
- **Permissions**: `CAMERA` (runtime permission) + `READ_MEDIA_IMAGES` (if picking older photos) or just `TakePicture` via camera.
- **UI**:
    - **Photo Capture Screen**: Simple UI to take a new photo.
    - **Comparison Screen**: List/Gallery view with a date range picker to select two dates for side-by-side comparison.

## 3. Feature: Auto Rest Timer
- **Logic**:
    - `VirtusViewModel` will manage `restTimerState` (isRunning, remainingTime, duration).
    - `lastSetCompletedTimestamp` in `ActiveWorkoutSession` triggers countdown.
- **Configuration**:
    - **Global**: App-wide default rest time in User Profile/Settings.
    - **Per-Exercise**: Optional override in the workout routine definition.
    - **Active Session**: In-session UI element to adjust/skip the timer.
- **UI**:
    - Integration into `ExerciseExecutionCard` / `WorkoutScreen` as an overlay or dedicated area during the rest interval.

## 4. UI/UX Considerations
- **Material 3**: Use `DatePickerDialog` for date range selection, `FloatingActionButton` for photo taking.
- **Timer UI**: Clear, prominent countdown display during rest, with options to "Skip" or "Add 30s".
- **Accessibility**: All buttons > 48dp.
- **Growth App Aesthetic**: `GrowthOrange` for primary actions/timers, `CarbonDark` for backgrounds.

## 5. Verification Plan
1. **Compilation**: `compile_applet`.
2. **Feature Tests**:
    - Capture a photo, verify it saves and syncs.
    - Select two photos via range picker and verify comparison UI.
    - Complete a set, verify timer starts with the correct duration.
    - Adjust timer during active session and verify responsiveness.
