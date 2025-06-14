# PRD: Real-Time Transcription with Deepgram

- **Feature:** Real-Time Transcription via Deepgram Streaming
- **Version:** 1.0
- **Status:** Proposed
- **Author:** AI Assistant

## 1. Introduction & Overview

This document outlines the requirements for replacing the existing Whisper AI batch transcription process with a real-time streaming solution using the Deepgram API.

Currently, the application records a user's full voice input, saves it as a file, and then uploads it to Whisper for transcription. This introduces a noticeable delay, especially for longer dictations (30+ seconds), between when the user finishes speaking and when the edited text is ready.

This feature will implement a real-time pipeline by streaming audio to Deepgram's WebSocket endpoint as it's being recorded. This will allow transcription to happen concurrently with recording, drastically reducing the perceived latency and making the user experience feel faster and more interactive.

## 2. Goals

- **Primary:** Significantly reduce the end-to-end time from when a user stops recording to when the final, edited text is available.
- **Secondary:** Establish a robust, real-time audio processing pipeline that can be built upon for future features.
- **User-Facing:** Create a faster, more responsive-feeling interaction, particularly for dictations longer than a few sentences.

## 3. User Stories

- As a user dictating a long email, I want the transcription to be completed almost instantly after I finish speaking, so I don't have to wait before the editing process begins.
- As a developer, I want to integrate a real-time transcription service that is reliable and provides high accuracy for specific keywords.
- As a user, I want the application to gracefully handle network interruptions during recording without losing my dictated text.

## 4. Functional Requirements

| ID  | Requirement                                                                                                                                                                                            | Details                                                                                                                                                                                                                                                             |
| --- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| 1   | **Integrate Deepgram Streaming API**                                                                                                                                                                     | - The app must establish a WebSocket connection to the Deepgram streaming endpoint when recording starts.<br>- Audio chunks must be sent to Deepgram in real-time as they are captured from the microphone.                                                                      |
| 2   | **Implement Keyword Boosting**                                                                                                                                                                         | - On each request, include a list of keywords to improve recognition accuracy.<br>- Initial hardcoded list: `Aleisha`, `Te Anau`, `Iain Forrest`.                                                                                                                      |
| 3   | **Update UI State During Recording**                                                                                                                                                                     | - When recording starts, the text box should display: "Recording )" (with animation).<br>- When the *first* partial transcript is received from Deepgram, the text box should display two lines: <br> `Recording )` <br> `Transcribing )` <br>(both with animation). |
| 4   | **Update UI State After Recording**                                                                                                                                                                      | - When the user stops recording, the "Recording" line must disappear immediately.<br>- The "Transcribing" line should remain until the *final* transcript is received from Deepgram, after which it also disappears and the editing pipeline is triggered.               |
| 5   | **Secure API Key Management**                                                                                                                                                                          | - The Deepgram API key must be stored securely in `BuildConfig` and not be exposed in client-side code, consistent with existing API keys.                                                                                                                            |
| 6   | **Handle Transcription Results**                                                                                                                                                                         | - The application must listen for and assemble the final transcription from Deepgram's responses.<br>- The user should **not** see the partial or final transcript text in the UI; it is to be held in memory and passed directly to the editing API (Claude).           |

## 5. Non-Goals (Out of Scope)

- A user-facing interface for managing custom keywords. This will be part of a future settings screen.
- Displaying the real-time transcription text to the user in the UI.
- Support for any real-time transcription provider other than Deepgram.

## 6. Technical Considerations

### Fallback Mechanism for Stream Failures

To ensure robustness against network issues, a fallback mechanism is required.

1.  **Local Audio Buffering:** During a recording session, audio must be simultaneously streamed to Deepgram and written to a local file/buffer.
2.  **Error Detection:** The WebSocket client must detect connection errors, timeouts, or other failures from the Deepgram stream.
3.  **Fallback to Batch:** If the stream fails and cannot be re-established, or if the recording completes while the connection is down, the system will use the locally saved audio file.
4.  **Batch Transcription:** The complete audio file will be sent to Deepgram's **Pre-recorded Audio API endpoint**. This serves as the fallback to ensure the transcription request is still fulfilled.

### Android Implementation

- **WebSocket Client:** Use a robust WebSocket client library for Android (e.g., OkHttp's `WebSocketListener`).
- **Audio Recording:** Continue using `MediaRecorder` or switch to `AudioRecord` to get direct access to raw audio byte streams, which are required for streaming. The `android-streaming-example` repo should be referenced.
- **Dependency Injection:** The new Deepgram client/repository should be integrated into the existing Hilt dependency injection setup.

## 7. Success Metrics

- **Performance:** The final, complete transcript from Deepgram should be received within **1 second** of the user pressing the "stop recording" button for a 30-second audio clip.
- **Stability:** The application builds and runs without crashing. The new transcription flow is successfully triggered.
- **Accuracy:** Qualitative assessment of boosted keywords ("Aleisha", "Te Anau", "Iain Forrest") appearing correctly in test transcriptions.

## 8. Open Questions

- None at this time. 