# Implementation Plan - Global Theme Toggle (Light & Dark Mode)

## User Review Required
> [!IMPORTANT]
> The theme toggle will allow switching between Dark Mode and Light Mode seamlessly across the entire app, with persistent state in the user profile and switches accessible both from the Top App Bar and the User Profile screen.

- **Theme Persistence**: Saved in `UserProfile` state model and saved locally.
- **UI Changes**: 
  - Add a sun/moon icon toggle button in the Top App Bar of key screens.
  - Add a dedicated Light/Dark Mode toggle setting in the User Profile screen.
  - Update `MyApplicationTheme` in `Theme.kt` and `MainActivity.kt` to dynamically respond to the user's selected theme state.

## Proposed Changes

### Data & State Model
#### [GrowthViewModel.kt](app/src/main/java/com/example/GrowthViewModel.kt)
- Add `isDarkMode: Boolean` to `UserProfile` (defaulting to `true` for the app's sleek dark theme).
- Add a method `toggleTheme()` or `setDarkMode(isDark: Boolean)` to update the preference.

### Theming & UI Components
#### [Theme.kt](app/src/main/java/com/example/ui/theme/Theme.kt)
- Update `MyApplicationTheme` parameter `darkTheme: Boolean` to be dynamic (observed from ViewModel).
- Ensure all Material 3 color schemes (`DarkColorScheme` and `LightColorScheme`) render properly with high contrast and accessibility.

#### [MainActivity.kt](app/src/main/java/com/example/MainActivity.kt)
- Observe `profile.isDarkMode` in `MainAppScreen`.
- Pass `darkTheme = profile.isDarkMode` into `MyApplicationTheme`.
- Add a theme toggle IconButton in the Top App Bar for quick access.

#### [UserProfileScreen.kt](app/src/main/java/com/example/UserProfileScreen.kt)
- Add a Light/Dark mode toggle switch card in the Profile settings view.

## Verification Plan

### Automated Tests
- Run `gradle :app:testDebugUnitTest` to verify compilation and basic unit tests.

### Manual Verification
- Verify theme switches instantly when toggled from the Top App Bar.
- Verify theme switches instantly when toggled from the User Profile screen.
- Verify theme preference persists across app navigation and state updates.
