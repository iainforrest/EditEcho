## Relevant Files

- `app/src/main/java/com/editecho/service/NotificationService.kt` - Entry point for launching the overlay. Investigate this file to determine which overlay implementation is used.
- `app/src/main/java/com/editecho/service/OverlayService.kt` - The WindowManager-based overlay implementation. This is a likely candidate for modification.
- `app/src/main/java/com/editecho/ui/screens/EditEchoOverlay.kt` - The Dialog-based overlay implementation. Previous incorrect modifications should be reverted here.
- `app/src/main/java/com/editecho/ui/components/TonePicker.kt` - Contains the `ExposedDropdownMenuBox` that is causing the issue. May need to be replaced with a custom component.
- `app/src/main/java/com/editecho/MainActivity.kt` - The main activity, useful for understanding the app's startup flow and permissions.

### Notes

- Unit tests should typically be placed alongside the code files they are testing.
- We are building an Android app. Every parent task must finish with a successful and error-free build.
- Use Logcat to monitor logs. Filter by tags like `OverlayService` or custom tags you add to debug the overlay lifecycle and keyboard events.

## Tasks

- [x] 1.0 Identify the Active Overlay Implementation
  - [x] 1.1 Examine `NotificationService.kt` to understand how the overlay is created and launched when the notification is tapped.
  - [x] 1.2 Add distinct `Log.d` statements in both `EditEchoOverlay.kt` (inside the `Dialog` composable) and `OverlayService.kt` (inside its `onCreate` or `onStartCommand` method).
  - [x] 1.3 Run the app, tap the notification to trigger the overlay, and check Logcat to see which log message appears, confirming the active implementation.
  - [x] 1.4 Build the app to ensure no errors were introduced.

- [x] 2.0 Revert the Previous Erroneous Fix
  - [x] 2.1 Open `app/src/main/java/com/editecho/ui/screens/EditEchoOverlay.kt`.
  - [x] 2.2 In the `Dialog` composable, locate the `properties` parameter.
  - [x] 2.3 Remove the `decorFitsSystemWindows = false` line from `DialogProperties`.
  - [x] 2.4 Remove the `.imePadding()` modifier from the `Card` composable within the `Dialog` to fully revert the previous changes.
  - [x] 2.5 Build the application to confirm the reversion caused no compilation errors.

- [x] 3.0 Implement Correct Fix Based on Active Implementation
  - [x] 3.1 **If `OverlayService.kt` is the active implementation (most likely):**
    - [x] 3.1.1 Review the `createLayoutParams()` method in `OverlayService.kt` to understand how `softInputMode` is configured.
    - [x] 3.1.2 Analyze the `setOnApplyWindowInsetsListener` to see how the `imePadding` state is calculated and applied.
    - [x] 3.1.3 Ensure the `imePadding` state variable is correctly passed down to the `EditEchoOverlayContent` composable and applied as padding to its root container.
  - [ ] 3.2 **If `EditEchoOverlay.kt` (Dialog) is the active implementation:**
    - [ ] 3.2.1 Apply the `.imePadding()` modifier to the `Card` or the root `Box` that holds the main content.
    - [ ] 3.2.2 Ensure that no other window-related properties (like `decorFitsSystemWindows`) are being incorrectly modified.
  - [ ] 3.3 Build and run the app to confirm the fix is applied without errors.

- [x] 4.0 Address `ExposedDropdownMenu` Limitations
  - [x] 4.1 After applying the fix in Task 3, test if the `TonePicker.kt` dropdown is still obscured by the keyboard.
  - [x] 4.2 If the issue persists, create a new custom composable file, e.g., `KeyboardAwareDropdown.kt`.
  - [x] 4.3 In the new component, use a standard `DropdownMenu` which offers more positioning control.
  - [x] 4.4 Manually calculate the dropdown's vertical offset using `WindowInsets.ime.getBottom(LocalDensity.current)` to position it above the keyboard when it's visible.
  - [x] 4.5 Replace the `ExposedDropdownMenuBox` in `TonePicker.kt` with the new custom implementation.
  - [x] 4.6 Build the app to confirm successful integration.

- [x] 5.0 Implement Robust Testing Strategy
  - [x] 5.1 Manually test opening the tone dropdown *after* the keyboard is already visible.
  - [x] 5.2 Manually test opening the tone dropdown *before* the keyboard is visible, then tap a text field to bring up the keyboard and observe the behavior.
  - [x] 5.3 Test on different Android API levels (e.g., 29 and 33) if emulators are available, to check for version-specific issues.
  - [x] 5.4 Test in both portrait and landscape orientations to ensure the layout adapts correctly.

- [x] 6.0 Evaluate and Consider Alternative UI Patterns
- [x] 7.0 Implement Defensive Programming and State Management

- [x] 8.0 Complete Documentation and Monitoring
  - [x] 8.1 Add a KDoc comment to the top of the identified active overlay file (`OverlayService.kt` or `EditEchoOverlay.kt`) explaining its role and how it handles keyboard insets.
  - [x] 8.2 (Optional) Add analytics logging to track tone picker usage and any errors during its interaction.
  - [x] 8.3 Build the app one final time to ensure no issues were introduced. 