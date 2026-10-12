# Implementation Plan - Fixing Vertical Text Wrapping bugs

## Goal
Identify and fix UI layout bugs where text elements wrap vertically letter-by-letter instead of displaying horizontally across Dashboard, Profile, Diet, Workout, and custom UI components.

## Proposed Changes

### 1. UI Layout Audit & Fixes
- **DashboardScreen.kt**: Inspect stat cards, header bars, and action buttons for narrow containers or missing `fillMaxWidth()` / `horizontalArrangement` that force vertical text wrapping.
- **UserProfileScreen.kt**: Check form labels, profile summaries, and action buttons.
- **DietScreen.kt**: Check meal item headers, macro summaries, and input fields.
- **WorkoutScreen.kt**: Check exercise card headers, set items, and routine titles.
- **GrowthComponents.kt**: Check reusable cards, badges, and stat items for any fixed narrow widths or missing horizontal weights.

## Verification Plan
- Run `compile_applet` to ensure successful compilation.
- Verify text is properly aligned horizontally in Compose layouts.
