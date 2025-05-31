## Relevant Files

- `app/src/main/java/com/editecho/service/OverlayService.kt` - New Android Service to manage the `WindowManager` overlay, its lifecycle, and parameters.
- `app/src/test/java/com/editecho/service/OverlayServiceTest.kt` - Unit tests for `OverlayService.kt`.
- `app/src/main/java/com/editecho/ui/overlay/OverlayScreen.kt` (or `OverlayView.kt`) - New Jetpack Compose composable function defining the UI for the compact keyboard-style overlay.
- `app/src/test/java/com/editecho/ui/overlay/OverlayScreenTest.kt` - Unit tests for `OverlayScreen.kt`.
- `app/src/main/java/com/editecho/ui/components/CompactOverlayComponents.kt` (New or existing, modified) - Specific UI components adapted or created for the compact overlay (e.g., buttons, text areas).
- `app/src/main/java/com/editecho/view/EditEchoViewModel.kt` (or similar, e.g., `MainViewModel.kt`) - Existing ViewModel that will likely need to be updated or used by the `OverlayService` to manage state and business logic for record, transcribe, edit, copy.
- `app/src/main/java/com/editecho/util/NotificationHelper.kt` (or similar) - Existing utility that manages the persistent notification; will need to be updated to trigger the start/stop of the new `OverlayService`.
- `AndroidManifest.xml` - Will need to be modified to declare the new `OverlayService` and ensure necessary permissions (e.g., `SYSTEM_ALERT_WINDOW`) are declared if not already present.
- `app/src/main/res/layout/overlay_layout.xml` (If not using Jetpack Compose exclusively for the overlay root) - XML layout file to be inflated by the `OverlayService` if a hybrid approach is taken. (Less likely if full Compose is used).

### Notes

- Unit tests should typically be placed alongside the code files they are testing (e.g., `MyComponent.kt` and `MyComponent.test.kt` in the same directory, or in parallel `test` source set structure).
- Use `gradlew testDebugUnitTest` (or similar variant) to run unit tests.
- For UI tests, consider using `gradlew connectedAndroidTest` (or similar variant) after writing Espresso or Compose UI tests.

## Tasks

- [ ] 1.0 Setup WindowManager Service for Overlay
  - [ ] 1.1 Create a new Android Foreground Service class (e.g., `OverlayService.kt`) to host the overlay.
  - [ ] 1.2 Implement `WindowManager` logic within the service to add and remove a view.
  - [ ] 1.3 Define `WindowManager.LayoutParams` for bottom screen positioning, keyboard-like height, and necessary flags (e.g., `TYPE_APPLICATION_OVERLAY`, `FLAG_NOT_FOCUSABLE`, `FLAG_NOT_TOUCH_MODAL`, `FLAG_LAYOUT_IN_SCREEN`, `FLAG_LAYOUT_NO_LIMITS`, `SOFT_INPUT_ADJUST_RESIZE`).
  - [ ] 1.4 Ensure the service requests and handles the `SYSTEM_ALERT_WINDOW` permission if not already globally available.
  - [ ] 1.5 Update the persistent notification action to start/stop this new `OverlayService`.
  - [ ] 1.6 Implement `onStartCommand` and `onDestroy` in the service to manage overlay creation and cleanup.

- [ ] 2.0 Design and Implement Compact Overlay UI
  - [ ] 2.1 Create a new Jetpack Compose function (e.g., `OverlayScreen.kt`) for the overlay's UI.
  - [ ] 2.2 Adapt existing EditEcho UI elements (record button, transcription text display, edit area) for the compact, keyboard-height layout.
  - [ ] 2.3 Remove the "EditEcho" title text from the overlay UI to save vertical space.
  - [ ] 2.4 Implement the "X" close button in the top-right (or as per final design placement) of the overlay UI.
  - [ ] 2.5 Ensure the Compose UI is responsive to portrait/landscape orientations and different screen densities, maintaining keyboard-like height.
  - [ ] 2.6 Apply standard Material Design principles to the overlay components.
  - [ ] 2.7 Inflate/attach the Compose UI within the `OverlayService`.

- [ ] 3.0 Implement Overlay Behavior and System Interaction
  - [ ] 3.1 Configure `WindowManager.LayoutParams` so the underlying app's content view resizes or pans up to accommodate the overlay (e.g., using `SOFT_INPUT_ADJUST_RESIZE` or by observing inset changes if needed).
  - [ ] 3.2 Ensure the overlay does not dismiss when tapping outside its bounds (verify `FLAG_NOT_TOUCH_MODAL` behavior).
  - [ ] 3.3 Connect the "X" button's `onClick` listener to a method in the `OverlayService` that removes the view from `WindowManager` and stops the service.
  - [ ] 3.4 Implement logic for the overlay to remain visible and cover the system keyboard if the system keyboard is explicitly shown.
  - [ ] 3.5 Verify the overlay appears correctly even if no text input field is focused or if the system keyboard is not visible.
  - [ ] 3.6 Test and document behavior when the overlay is shown over apps that don't typically resize for keyboards (e.g., full-screen games).

- [ ] 4.0 Integrate Core EditEcho Functionality into Overlay
  - [ ] 4.1 Ensure the `OverlayService` can access or instantiate the relevant ViewModel (e.g., `EditEchoViewModel`) to handle business logic.
  - [ ] 4.2 Connect UI actions in `OverlayScreen.kt` (record, stop, copy) to the ViewModel functions.
  - [ ] 4.3 Ensure audio recording is initiated correctly from the overlay.
  - [ ] 4.4 Ensure transcription results are displayed and updated in the overlay's text area.
  - [ ] 4.5 Verify text editing within the overlay functions as expected.
  - [ ] 4.6 Confirm the "copy to clipboard" functionality works correctly with the text from the overlay.
  - [ ] 4.7 Manage the lifecycle of the ViewModel in conjunction with the `OverlayService` lifecycle.

- [ ] 5.0 Thorough Testing and Refinement
  - [x] 5.1 Write unit tests for `OverlayService.kt` covering its lifecycle and `WindowManager` interactions.
  - [ ] 5.2 Test that all existing functionality works correctly in the new overlay:
    - [ ] Recording functionality
    - [ ] Transcription display and updates
    - [ ] Text editing capability
    - [ ] Copy to clipboard functionality
    - [ ] Formality and Polish sliders (VoiceSliders component)
    - [ ] Settings button remains visible but inactive
  - [ ] 5.3 Manually test all functional requirements listed in the PRD (Section 4).
  - [ ] 5.4 Manually test all user stories from the PRD (Section 3).
  - [ ] 5.5 Test on a range of Android API levels (31+) and various devices.
  - [ ] 5.6 Test in both portrait and landscape orientations.
  - [ ] 5.7 Test functionality in Android's split-screen mode.
  - [ ] 5.8 Test interaction with various host apps (messaging, email, browsers, note-taking apps).
  - [ ] 5.9 Measure overlay appearance time and responsiveness (<500ms target).
  - [ ] 5.10 Verify the overlay maintains proper keyboard height across different devices.
  - [ ] 5.11 Create and address a bug list based on testing.
  - [ ] 5.12 Ensure memory leaks are avoided when the service is stopped/started repeatedly.

### Notes

- The existing UI is already fully functional with all features (record, transcribe, edit, copy, sliders) working correctly.
- Focus is on extracting the existing UI and hosting it in a WindowManager-based service rather than building new functionality.
- Use `gradlew testDebugUnitTest` to run unit tests.
- **Testing Checklist Created**: A comprehensive testing checklist has been created at `tasks/overlay-testing-checklist.md` covering all manual testing requirements from tasks 5.2-5.12. 