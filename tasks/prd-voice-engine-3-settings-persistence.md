# Product Requirements Document: Voice Engine 3.0 Settings Persistence

## Introduction/Overview

EditEcho currently has a legacy settings system that persists formality and polish levels using DataStore. With the introduction of Voice Engine 3.0, we need to update the settings persistence to store the new tone selection and polish level, providing users with a consistent experience where their preferred settings are remembered between app sessions.

The problem this solves: Users currently lose their tone and polish preferences when the app is closed or the overlay is dismissed, requiring them to reconfigure their settings each time they use EditEcho.

## Goals

1. **Replace Legacy Settings**: Update SettingsRepository to persist Voice Engine 3.0 settings (selectedTone + polishLevel) instead of legacy formality/polish values
2. **Seamless User Experience**: Users' tone and polish preferences are automatically restored when opening the overlay
3. **Immediate Persistence**: Settings are saved immediately when changed, preventing loss of user preferences
4. **Reliable Defaults**: First-time users and users with corrupted settings get sensible defaults (Neutral tone, 50% polish)
5. **Reactive Updates**: Settings changes propagate throughout the app using Flow-based reactive patterns

## User Stories

**As a frequent EditEcho user**, I want my tone preference to be remembered between sessions so that I don't have to reselect "Casual" every time I open the overlay.

**As a business user**, I want my polish level (set to 80%) to persist across app restarts so that my professional communication style is consistently maintained.

**As a new user**, I want the app to start with reasonable defaults (Neutral tone, medium polish) so that I can immediately use the feature without configuration.

**As the single user of this development app**, I want the settings system to work cleanly without worrying about migration complexity since there are no other users to consider.

## Functional Requirements

### Core Persistence Requirements
1. **Replace Legacy Settings**: The system must completely replace the existing formality/polish DataStore keys with new Voice Engine 3.0 keys
2. **Tone Persistence**: The system must persist the selected tone as a string value (e.g., "Neutral", "Casual", "Informative", "Supportive", "Thoughtful")
3. **Polish Level Persistence**: The system must persist the polish level as an integer value (0-100 range)
4. **Immediate Save**: Settings must be saved to DataStore immediately when the user changes tone selection or polish slider position
5. **Automatic Restore**: Settings must be automatically loaded and applied when the overlay opens

### Data Validation Requirements
6. **Tone Validation**: When loading persisted tone, validate it exists in current ToneProfile enum; fallback to "Neutral" if invalid
7. **Polish Range Validation**: When loading persisted polish level, clamp to 0-100 range; fallback to 50 if invalid
8. **Default Handling**: For first-time users or corrupted settings, apply defaults: selectedTone = "Neutral", polishLevel = 50

### Integration Requirements
9. **ViewModel Integration**: EditEchoOverlayViewModel must automatically load persisted settings on initialization
10. **Component Integration**: TonePicker and PolishSlider components must reflect loaded settings on first render
11. **Reactive Updates**: Settings changes must propagate through Flow observers to update UI components immediately

### Cleanup Requirements
12. **Simple Replacement**: Replace legacy formality/polish DataStore keys with new Voice Engine 3.0 keys
13. **No Migration Logic**: Since there is only one user (developer), simply replace the old system without complex migration

## Non-Goals (Out of Scope)

- **Settings Export/Import**: Not including backup/restore of settings to external storage
- **Multiple Profiles**: Not supporting different setting profiles for different use cases
- **Advanced Validation**: Not including complex validation like tone-polish combination recommendations
- **Settings History**: Not tracking or allowing rollback to previous settings
- **Legacy Compatibility**: Not maintaining parallel legacy and V3 settings systems
- **Complex Migration**: Not implementing user migration logic since there is only one user (developer)

## Design Considerations

### Updated SettingsRepository Interface
```kotlin
class SettingsRepository {
    val selectedTone: Flow<String> // Replaces formality Flow
    val polishLevel: Flow<Int>     // Replaces polish Flow
    
    suspend fun setSelectedTone(tone: String)
    suspend fun setPolishLevel(level: Int)
}
```

### DataStore Key Strategy
- **New Keys**: `"selected_tone"` and `"polish_level"`
- **Removed Keys**: `"formality"` and `"polish"` (legacy keys)
- **Simple Replacement**: Direct replacement of old keys with new keys (no migration complexity needed)

### UI Component Integration
- **TonePicker**: Reads selectedTone Flow, calls setSelectedTone on change
- **PolishSlider**: Reads polishLevel Flow, calls setPolishLevel on change
- **EditEchoOverlayViewModel**: Manages settings state and triggers persistence

## Technical Considerations

### DataStore Implementation
- **Preferences DataStore**: Continue using existing preferences approach for consistency
- **Key Naming**: Use descriptive string keys that align with Voice Engine 3.0 terminology
- **Async Operations**: Maintain existing coroutine-based async patterns

### Validation Logic
- **Tone Validation**: Use `ToneProfile.fromName()` to validate persisted tone strings
- **Defensive Defaults**: Always provide valid fallback values to prevent app crashes
- **Range Clamping**: Use `coerceIn(0, 100)` for polish level validation

### Performance Considerations
- **Immediate Persistence**: Use `edit {}` block for atomic updates
- **Flow Efficiency**: Leverage existing DataStore Flow caching for reactive updates
- **Memory Usage**: Minimal impact as we're replacing, not adding to existing settings

## Success Metrics

### User Experience Metrics
- **Settings Retention Rate**: 95% of users should have their settings correctly restored on app restart
- **Default Usage**: New users should see appropriate defaults (Neutral/50) on first launch
- **Zero Setting Loss**: No user reports of lost tone/polish preferences after app updates

### Technical Metrics
- **Persistence Latency**: Settings changes should persist within 100ms of user action
- **Load Performance**: Settings should load within 50ms of overlay initialization
- **Error Rate**: <0.1% settings corruption/validation failures

### Validation Criteria  
- **Manual Testing**: Verify settings persist across app kills, device reboots, and app updates
- **Edge Case Testing**: Test with invalid persisted data, corrupted DataStore, and first-time launches
- **Integration Testing**: Ensure UI components correctly reflect loaded settings

## Open Questions

1. **Analytics Integration**: Should we track which tones/polish levels are most commonly used to inform future defaults?
2. **Settings Reset**: Should there be a "Reset to Defaults" option in the UI for users who want to start over?
3. **Migration Notifications**: Should users be notified that their settings system has been upgraded to Voice Engine 3.0?
4. **Bulk Settings**: Should we consider a single DataStore operation to save both tone and polish atomically?

---

**Implementation Priority**: High - This is foundational for Voice Engine 3.0 user experience
**Estimated Effort**: Medium - Requires SettingsRepository refactor and ViewModel integration
**Dependencies**: Voice Engine 3.0 ToneProfile enum, existing DataStore infrastructure 