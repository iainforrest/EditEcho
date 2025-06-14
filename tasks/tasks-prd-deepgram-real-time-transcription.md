# Task List: Real-Time Transcription with Deepgram

This task list is based on the PRD for implementing real-time transcription and is designed to guide a developer through the implementation.

## Relevant Files & Resources

- **`secrets.properties`** - Modified to include DEEPGRAM_API_KEY configuration.
- **`app/build.gradle.kts`** - Modified to load and expose DEEPGRAM_API_KEY via BuildConfig. Existing OkHttp 4.12.0 dependency provides WebSocket support.
- **`app/src/main/java/com/editecho/network/DeepgramRepository.kt`** - Created. Basic repository structure with placeholder methods for streaming and batch transcription.
- **`app/src/main/java/com/editecho/di/NetworkModule.kt`** - Modified to provide DeepgramRepository singleton instance via Hilt dependency injection.
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

- [ ] **2.0 Implement Real-time Audio Streaming & Keyword Boosting**
  - [ ] 2.1 Modify the audio recording logic (likely in `EditEchoOverlayViewModel` or a new audio utility class) to capture raw audio chunks (e.g., using `AudioRecord`) instead of saving a complete file with `MediaRecorder`.
  - [ ] 2.2 In `DeepgramRepository`, implement a method to initialize a WebSocket connection to Deepgram's streaming endpoint.
  - [ ] 2.3 Construct the WebSocket URL to include the `keywords` query parameter with the hardcoded values: `keywords=Aleisha:1.2&keywords=Te Anau:1.2&keywords=Iain Forrest:1.2`. Use a moderate intensifier like `1.2` to start.
  - [ ] 2.4 Implement the logic to send the raw audio chunks received from the microphone over the active WebSocket connection.
  - [ ] 2.5 Ensure the app builds successfully after these changes.

- [ ] **3.0 Implement Transcription Result Handling**
  - [ ] 3.1 In `DeepgramRepository`, implement the `onMessage` listener for the WebSocket.
  - [ ] 3.2 Add logic to parse the incoming JSON messages from Deepgram, distinguishing between partial and final results using the `is_final` and `speech_final` flags.
  - [ ] 3.3 Create a mechanism within the repository to accumulate the text from transcription results.
  - [ ] 3.4 Create a public method in `DeepgramRepository` (e.g., `transcribeStream`) that hides the complexity and returns the final, complete transcript as a `String`. This ensures compatibility with the `ViewModel`.
  - [ ] 3.5 Ensure the app builds successfully.

- [ ] **4.0 Implement Stream Failure Fallback**
  - [ ] 4.1 While streaming is active, simultaneously save the captured audio chunks to a local file in the app's cache.
  - [ ] 4.2 In `DeepgramRepository`, implement `onFailure` and `onClosing` listeners for the WebSocket to detect connection errors or unexpected closures.
  - [ ] 4.3 If a stream fails, implement a fallback method in `DeepgramRepository` that sends the locally saved audio file to the Deepgram **Pre-recorded (Batch) Audio API**.
  - [ ] 4.4 Ensure this fallback method returns a `String` transcript, just like the streaming method, to maintain a consistent interface for the ViewModel.
  - [ ] 4.5 Remember to clean up the locally saved audio file after the transcription process (whether successful or failed) is complete.
  - [ ] 4.6 Ensure the app builds successfully.

- [ ] **5.0 Update UI and ViewModel**
  - [ ] 5.1 In `EditEchoOverlayViewModel`, replace the call to `whisperRepo.transcribe()` with the new method from `DeepgramRepository`.
  - [ ] 5.2 Add a new state to `RecordingState` or a new `StateFlow<Boolean>` in the ViewModel called `isTranscribing` to track when the first partial result is received from Deepgram.
  - [ ] 5.3 Modify `EditedMessageBox.kt` to observe the new state. It should display "Recording )" initially, and then both "Recording )" and "Transcribing )" on separate lines once the `isTranscribing` state is true.
  - [ ] 5.4 Update the logic so that when the user stops recording, the "Recording" text disappears, but "Transcribing" remains until the final result is processed.
  - [ ] 5.5 Confirm the final transcript is passed to the Claude editing pipeline as before and that the `refinedText` is updated correctly.
  - [ ] 5.6 Ensure the entire application builds and runs successfully, and test the end-to-end flow. 