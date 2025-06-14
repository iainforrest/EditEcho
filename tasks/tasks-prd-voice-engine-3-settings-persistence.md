## Relevant Files

- `app/src/main/java/com/editecho/data/SettingsRepository.kt` - Main repository class that needs complete refactor for Voice Engine 3.0 settings
- `app/src/main/java/com/editecho/view/EditEchoOverlayViewModel.kt` - ViewModel that manages overlay state and settings integration
- `app/src/main/java/com/editecho/ui/components/TonePicker.kt` - UI component for tone selection that needs settings integration
- `app/src/main/java/com/editecho/ui/components/PolishSlider.kt` - UI component for polish adjustment that needs settings integration
- `app/src/main/java/com/editecho/ui/screens/EditEchoOverlay.kt` - Main overlay screen that uses the settings
- `app/src/test/java/com/editecho/data/SettingsRepositoryTest.kt` - Unit tests for the updated SettingsRepository
- `app/src/test/java/com/editecho/view/EditEchoOverlayViewModelTest.kt` - Unit tests for ViewModel settings integration

### Notes

- Unit tests should be placed in `app/src/test/java/` following the same package structure as the main code
- Use `./gradlew test` to run all unit tests for the Android project
- We are building an Android app. Every parent task must finish with a successful and error-free build
- Voice Engine 3.0 uses selectedTone (String) + polishLevel (Int) instead of legacy formality/polish (both Int)
- Settings must persist immediately when changed and restore automatically on app startup

## Tasks

- [x] 1.0 Update SettingsRepository for Voice Engine 3.0 Settings
  - [x] 1.1 Replace legacy DataStore keys with new Voice Engine 3.0 keys ("selected_tone", "polish_level")
  - [x] 1.2 Update class properties: replace `formality: Flow<Int>` and `polish: Flow<Int>` with `selectedTone: Flow<String>` and `polishLevel: Flow<Int>`
  - [x] 1.3 Replace `setFormality(value: Int)` and `setPolish(value: Int)` methods with `setSelectedTone(tone: String)` and `setPolishLevel(level: Int)`
  - [x] 1.4 Add validation logic: validate tone exists in ToneProfile enum, clamp polish level to 0-100 range
  - [x] 1.5 Set default values: "Neutral" for selectedTone, 50 for polishLevel
  - [x] 1.6 Remove all legacy formality/polish DataStore key references
  - [x] 1.7 Update AppModule dependency injection to ensure SettingsRepository builds successfully
  - [x] 1.8 Verify the app builds and runs without errors after SettingsRepository changes

  - [x] 2.0 Update EditEchoOverlayViewModel Settings Integration
  - [x] 2.1 Remove legacy `voiceSettings: StateFlow<VoiceSettings>` property and related logic
  - [x] 2.2 Add new properties: `selectedTone: StateFlow<ToneProfile>` and `polishLevel: StateFlow<Int>`
  - [x] 2.3 Update ViewModel initialization to collect from new SettingsRepository flows
  - [x] 2.4 Replace `onFormalityChanged(Int)` and `onPolishChanged(Int)` methods with `onToneSelected(ToneProfile)` and `onPolishLevelChanged(Int)`
  - [x] 2.5 Add ToneProfile conversion logic: convert String from repository to ToneProfile enum for UI, and vice versa
  - [x] 2.6 Update Voice Engine 3.0 prompt building to use new settings (replace VoiceSettings usage)
  - [x] 2.7 Ensure settings are saved immediately when user changes tone or polish level
  - [x] 2.8 Verify ViewModel builds successfully and maintains existing recording/processing functionality

- [x] 3.0 Update UI Components for New Settings Architecture
  - [x] 3.1 Remove legacy slider section from EditEchoOverlayContent: delete `showLegacySliders` logic and formality/polish sliders
  - [x] 3.2 Remove VoiceSettings parameter from EditEchoOverlayContent and replace with selectedTone/polishLevel parameters
  - [x] 3.3 Update TonePicker component to read selectedTone from StateFlow and call onToneSelected callback
  - [x] 3.4 Update PolishSlider component to read polishLevel from StateFlow and call onPolishLevelChanged callback
  - [x] 3.5 Update EditEchoOverlay dialog to remove voiceSettings StateFlow collection and legacy callback parameters
  - [x] 3.6 Remove legacy callback methods (onFormalityChanged, onPolishChanged) from all UI components
  - [x] 3.7 Update OverlayService to pass new settings parameters to EditEchoOverlayContent
  - [x] 3.8 Verify UI builds successfully and shows correct default values (Neutral tone, 50% polish)

- [x] 4.0 Replace Legacy Settings Keys (Simplified - Single User)
  - [x] 4.1 Remove legacy DataStore key references ("formality", "polish") from SettingsRepository
  - [x] 4.2 Replace with new Voice Engine 3.0 keys ("selected_tone", "polish_level") 
  - [x] 4.3 Verify app handles default values correctly (Neutral tone, 50% polish)
  - [x] 4.4 Test that settings persist and restore correctly with new key structure
  - [x] 4.5 Ensure app builds and runs successfully after key replacement

- [ ] 5.0 Add Comprehensive Testing and Validation
  - [x] 5.1 Create SettingsRepositoryTest: test selectedTone/polishLevel persistence, validation, and default handling
  - [x] 5.2 Create EditEchoOverlayViewModelTest: test settings integration, StateFlow updates, and callback handling
  - [x] 5.3 Add integration tests: verify settings persist across app restarts and overlay open/close cycles
  - [x] 5.4 Test edge cases: invalid tone names, out-of-range polish values, corrupted DataStore scenarios
  - [x] 5.5 Manual testing: verify settings UI shows correct values, changes persist immediately, and defaults work correctly
  - [x] 5.6 Performance testing: verify settings load within 50ms and save within 100ms as per PRD success metrics
  - [x] 5.7 Regression testing: ensure existing recording, transcription, and editing functionality still works correctly
  - [ ] 5.8 Final validation: run full test suite with `./gradlew test` and ensure all tests pass with successful build 