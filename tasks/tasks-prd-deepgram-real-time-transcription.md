# Task List: Real-Time Transcription with Deepgram

This task list is based on the PRD for implementing real-time transcription and is designed to guide a developer through the implementation.

## Relevant Files & Resources

- **`secrets.properties`** - Modified to include DEEPGRAM_API_KEY configuration.
- **`app/build.gradle.kts`** - Modified to load and expose DEEPGRAM_API_KEY via BuildConfig. Existing OkHttp 4.12.0 dependency provides WebSocket support.
- **`app/src/main/java/com/editecho/network/DeepgramRepository.kt`** - Completed. Full WebSocket connection management, audio streaming integration, keyword boosting, connection state management, JSON response parsing, transcript accumulation with interim/final result handling, KeepAlive mechanism, comprehensive error handling, automatic reconnection with exponential backoff, and batch API fallback with cleanup.
- **`app/src/main/java/com/editecho/di/NetworkModule.kt`** - Modified to provide DeepgramRepository singleton instance via Hilt dependency injection.
- **`app/src/main/java/com/editecho/util/StreamingAudioRecorder.kt`** - Created. New audio recorder that captures raw PCM chunks using AudioRecord instead of saving to file with MediaRecorder. Provides SharedFlow of audio chunks for real-time streaming.
- **`app/src/main/java/com/editecho/network/dto/DeepgramResponse.kt`** - Created. Complete data classes for parsing Deepgram's JSON response format including transcription results, confidence scores, metadata, and utility extension functions.
- **`app/src/main/java/com/editecho/util/AudioFormatConverter.kt`** - Created. Complete utility for converting raw PCM audio chunks to WAV format with proper header generation for Deepgram's batch API compatibility.
- **`app/src/main/java/com/editecho/view/EditEchoOverlayViewModel.kt`** - To be modified to use the new `DeepgramRepository` and update the UI state according to the PRD.
- **`app/src/main/java/com/editecho/util/AudioRecorder.kt`** - May need modification or replacement to provide raw audio data chunks instead of saving to a file. The existing `MediaRecorder` might not be suitable.
- **`app/src/main/java/com/editecho/ui/components/EditedMessageBox.kt`** - To be modified to handle the new dual "Recording" / "Transcribing" UI state.
- **Reference 1: Deepgram Streaming Docs** - `https://developers.deepgram.com/docs/streaming`
- **Reference 2: Deepgram Android Example** - `https://github.com/deepgram-devs/android-streaming-example`
- **Reference 3: Deepgram Keywords Docs** - `https://developers.deepgram.com/docs/keywords`

### Notes

- Unit tests should be created for new components, especially the `DeepgramRepository`, to mock the WebSocket and test response handling.
- We are building an Android app. Every parent task must finish with a successful and error-free build.

## Tasks

- [x] **1.0 Setup Deepgram API Integration**
  - [x] 1.1 Add the Deepgram API Key to `local.properties` and `build.gradle.kts` to expose it via `BuildConfig`, following the existing pattern for other API keys.
  - [x] 1.2 Create a new file `app/src/main/java/com/editecho/network/DeepgramRepository.kt`.
  - [x] 1.3 Add any necessary dependencies for WebSockets (like OkHttp) to the `app/build.gradle.kts` file.
  - [x] 1.4 Modify `app/src/main/java/com/editecho/di/NetworkModule.kt` to provide a singleton instance of `DeepgramRepository` using Hilt.
  - [x] 1.5 Ensure the app builds successfully with the new empty repository and Hilt module.

- [x] **2.0 Implement Real-time Audio Streaming & Keyword Boosting**
  - [x] 2.1 Modify the audio recording logic (likely in `EditEchoOverlayViewModel` or a new audio utility class) to capture raw audio chunks (e.g., using `AudioRecord`) instead of saving a complete file with `MediaRecorder`.
  - [x] 2.2 In `DeepgramRepository`, implement a method to initialize a WebSocket connection to Deepgram's streaming endpoint.
  - [x] 2.3 Construct the WebSocket URL to include the `keywords` query parameter with the hardcoded values: `keywords=Aleisha:1.2&keywords=Te Anau:1.2&keywords=Iain Forrest:1.2`. Use a moderate intensifier like `1.2` to start.
  - [x] 2.4 Implement the logic to send the raw audio chunks received from the microphone over the active WebSocket connection.
  - [x] 2.5 Ensure the app builds successfully after these changes.

- [x] **3.0 Implement Transcription Result Handling**
  - [x] 3.1 In `DeepgramRepository`, implement the `onMessage` listener for the WebSocket.
  - [x] 3.2 Add logic to parse the incoming JSON messages from Deepgram, distinguishing between partial and final results using the `is_final` and `speech_final` flags. Create data classes for Deepgram's response format including `type`, `channel.alternatives[0].transcript`, `confidence`, etc.
  - [x] 3.3 Create a mechanism within the repository to accumulate the text from transcription results, properly handling interim results (append/replace logic) vs final results (commit to final transcript).
  - [x] 3.4 Implement KeepAlive mechanism to send periodic ping messages when no audio data is being transmitted to prevent the 10-second timeout disconnection that Deepgram enforces.
  - [x] 3.5 Create a public method in `DeepgramRepository` (e.g., `transcribeStream`) that hides the complexity and returns the final, complete transcript as a `String`. This ensures compatibility with the `ViewModel`.
  - [x] 3.6 Add proper error handling for common Deepgram disconnection scenarios: no audio data timeout, invalid encoding parameters, and network connectivity issues.
  - [x] 3.7 Ensure the app builds successfully using DevDebug.

- [x] **4.0 Implement Stream Failure Fallback**
  - [x] 4.1 While streaming is active, simultaneously save the captured audio chunks to a local file in the app's cache. Convert raw PCM chunks to WAV format for compatibility with Deepgram's batch API.
  - [x] 4.2 In `DeepgramRepository`, implement `onFailure` and `onClosing` listeners for the WebSocket to detect connection errors or unexpected closures.
  - [x] 4.3 Implement automatic reconnection logic for transient network issues. Include exponential backoff and maximum retry attempts to handle temporary connectivity problems.
  - [x] 4.4 If a stream fails and reconnection is not possible, implement a fallback method in `DeepgramRepository` that sends the locally saved audio file (in WAV format) to the Deepgram **Pre-recorded (Batch) Audio API**.
  - [x] 4.5 Ensure this fallback method returns a `String` transcript, just like the streaming method, to maintain a consistent interface for the ViewModel.
  - [x] 4.6 Remember to clean up the locally saved audio file after the transcription process (whether successful or failed) is complete.
  - [x] 4.7 Ensure the app builds successfully using DevDebug.

- [x] **5.0 Update UI and ViewModel**
  - [x] 5.1 In `EditEchoOverlayViewModel`, replace the call to `whisperRepo.transcribe()` with the new method from `DeepgramRepository`.
  - [x] 5.2 Add a new state to `RecordingState` or a new `StateFlow<Boolean>` in the ViewModel called `isTranscribing` to track when the first partial result is received from Deepgram.
  - [x] 5.3 Modify `EditedMessageBox.kt` to observe the new state. It should display "Recording )" initially, and then both "Recording )" and "Transcribing )" on separate lines once the `isTranscribing` state is true.
  - [ ] 5.4 **TEST**: Record a 30+ second audio clip and verify the UI flow shows: "Recording )" → "Recording )\nTranscribing )" → "Transcribing )" → "Editing )" → final edited text. Confirm transcription completes ~1 second after recording stops.
  - [ ] 5.5 **TEST**: Verify transcription speed improvement compared to previous Whisper batch processing. Test with various recording lengths (5s, 30s, 60s+) and measure time from recording stop to transcription complete.
  - [ ] 5.6 **TEST**: Test edge cases and error scenarios: network disconnection during recording, very short recordings (<1s), very long recordings (>5min), microphone permission revoked mid-recording, background app switching.
  - [ ] 5.7 **TEST**: Verify the complete end-to-end flow works correctly: recording → real-time transcription → Voice Engine 3.0 editing → clipboard copy. Confirm final edited text quality matches previous Whisper results.
  - [ ] 5.8 **TEST**: Test fallback scenarios by simulating network issues during streaming. Verify automatic reconnection attempts work and batch API fallback produces correct results when streaming fails completely. 