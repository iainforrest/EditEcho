# Edit Echo - Technical Context & Patterns

## Architecture Decisions Made
- **Chat Completions over Assistants API**: Better model control, easier switching, lower latency
- **Service-based Overlay**: More reliable than Activity-based for persistent access
- **Hilt over Koin**: Better compile-time safety and Android integration
- **Compose over XML**: Future-focused, better state management (except overlay layout)
- **Room + DataStore**: Local-first with selective cloud sync later

## Android-Specific Challenges
### Overlay Management
- **Permission Handling**: SYSTEM_ALERT_WINDOW requires special user grant
- **Service Lifecycle**: Foreground service keeps overlay accessible
- **Memory Management**: WindowManager views must be properly cleaned up
- **Touch Handling**: FLAG_NOT_TOUCH_MODAL for pass-through interaction

### Audio Recording
- **Permissions**: RECORD_AUDIO + FOREGROUND_SERVICE_MICROPHONE
- **Hardware Optimization**: 44.1kHz, 128kbps AAC for Whisper compatibility
- **Lifecycle**: Proper MediaRecorder cleanup on service destruction

### Background Processing
- **Notification Persistence**: START_STICKY service with ongoing notification
- **API Calls**: Network requests must handle app backgrounding
- **Battery Optimization**: Minimize processing when overlay not visible

## Code Patterns & Standards
### State Management
```kotlin
// ViewModel pattern with StateFlow
private val _recordingState = MutableStateFlow<RecordingState>(RecordingState.Idle)
val recordingState: StateFlow<RecordingState> = _recordingState.asStateFlow()
```

### Error Handling
```kotlin
// Structured error types with user-friendly messages
sealed class RecordingState {
    object Idle : RecordingState()
    object Recording : RecordingState()
    object Processing : RecordingState()
    data class Error(val message: String) : RecordingState()
}
```

### API Integration
```kotlin
// Streaming responses with Flow
fun streamReply(tone: ToneProfile, userText: String): Flow<String> = callbackFlow {
    // Process SSE stream, emit tokens as they arrive
}
```

## Performance Considerations
- **Overlay Responsiveness**: Sub-100ms tap-to-show target
- **Audio Processing**: Stream to API, never store large files locally
- **Memory Usage**: Aggressive cleanup of MediaRecorder, OkHttp clients
- **Battery Impact**: Minimize background processing, use efficient polling

## Security & Privacy
- **API Keys**: BuildConfig injection, never in source
- **User Data**: Local-only storage, optional cloud sync
- **Audio**: Immediate upload, never persisted locally
- **Permissions**: Request just-in-time, clear explanations

## Future Architecture Considerations
- **Modular Design**: Separate concerns for future white-label versions
- **Plugin System**: Allow custom tone generators, different AI providers
- **Offline Capability**: Local models for basic transcription
- **Multi-Platform**: Architecture ready for iOS port

## Anti-Patterns to Avoid
- **God Objects**: Keep ViewModels focused on single concerns
- **Tight Coupling**: Use dependency injection consistently
- **Memory Leaks**: Always clean up listeners, streams, hardware resources
- **UI Thread Blocking**: All AI/network calls on background threads
- **Inconsistent State**: Single source of truth for UI state

## Testing Strategy
- **Unit Tests**: Business logic, prompt generation, state management
- **Integration Tests**: API clients, database operations
- **UI Tests**: Critical user flows, overlay interactions
- **Performance Tests**: Memory usage, battery impact, response times