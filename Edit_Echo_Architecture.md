# Edit Echo — User Flow, Architecture & Tech Stack

*(Updated April 27, 2025 – Chat Completions MVP)*

---

## 1 Overview

Edit Echo is an **Android-first voice-to-text assistant** for busy people on the move. It captures voice input anywhere, refines it with AI, and returns polished text in seconds. The MVP uses a **persistent notification trigger** for simplicity and compatibility.

---

## 2 Core User Flow (MVP v0.7)

| Step                                       | Action                                                                                     | Detail                                                                                                                                        |
| ------------------------------------------ | ------------------------------------------------------------------------------------------ | --------------------------------------------------------------------------------------------------------------------------------------------- |
| **1. Persistent Notification (Always-on)** | Silent, non-dismissible notification while Edit Echo background service runs.              | • App icon + “Tap to compose with Edit Echo” text.• Optional quick-actions (Record, Settings) in future.                                      |
| **2. Open Overlay**                        | User taps the notification.                                                                | • Compose **bottom-sheet overlay** appears, covering bottom 40% of screen.• Underlying apps remain fully interactive for referencing content. |
| **3. Tone Selection**                      | User selects tone via pill buttons (SMS \| Casual Email \| Professional Email).            | • Defaults to last tone used.• Updates in-memory state only.                                                                                  |
| **4. Voice Input**                         | User presses mic FAB (Floating Action Button) in overlay.                                  | • Audio streams directly to **OpenAI Whisper API**.• Audio discarded immediately after transcription.                                         |
| **5. Prompt Assembly & Processing**        | Transcribed text prepended with tone label (e.g., "SMS:") and combined with system prompt. | • Prompt sent via HTTPS to **OpenAI Chat Completions API**.• Response fetched using OkHttp.                                                   |
| **6. Output**                              | GPT response displayed in read-only TextArea.                                              | • Text auto-copied to clipboard via `ClipboardManager`.                                                                                       |
| **7. Close Overlay**                       | User taps close (X) FAB.                                                                   | • Overlay animates down; notification remains active.• Overlay **not dismissed by outside taps**, allowing scrolling in underlying app.       |

---

## 3 Data-Handling Model

| Scope             | Stored?                     | Notes                                                |
| ----------------- | --------------------------- | ---------------------------------------------------- |
| **Voice audio**   | No                          | Streamed directly to Whisper; never written to disk. |
| **Transcription** | RAM only                    | Discarded after processing or when overlay closes.   |
| **Refined text**  | RAM only                    | Displayed until overlay closes or overwritten.       |
| **User settings** | Local DB (Room + DataStore) | Tone default, user-provided examples (future use).   |

---

## 4 System Architecture

### 4.1 Frontend (Android App)

| Component                         | Purpose                                                                                 |
| --------------------------------- | --------------------------------------------------------------------------------------- |
| **PersistentNotificationService** | Starts foreground service, posts notification, routes tap intents to `OverlayActivity`. |
| **OverlayActivity** (transparent) | Hosts Compose `BottomSheetScaffold`.                                                    |
| **OverlayUI (Compose)**           | Bottom sheet: read-only text area + tone buttons (80%), FAB column (20%).               |
| **RecorderManager**               | Manages PCM audio recording and streaming directly to Whisper.                          |
| **PromptBuilder**                 | Creates dynamic system prompts per selected tone.                                       |
| **ViewModel + StateFlow**         | Manages UI state (tone selection, recording status, GPT response).                      |

### 4.2 Backend Services

- **OpenAI Whisper API** (speech-to-text, direct HTTPS)

- **OpenAI Chat Completions API** (text refinement, direct HTTPS)

---

## 5 Prompt System

- **Structure:** Chat Completions (`system` message + `user` message)

- **System Prompt:** Built from static fragments + editing guidelines + tone brief + authentic examples (currently hardcoded)

- **Tone Labels:**
  
  - `Quick Message`: Short, spoken-style
  
  - `Friendly Reply`: Warm, conversational
  
  - `Clear and Polished`: Structured, formal

- **Future:** User-provided examples via settings stored locally (Room/DataStore) for personalized prompts.

---

## 6 Tech Stack Summary

| Layer      | Tech                                  | Rationale                             |
| ---------- | ------------------------------------- | ------------------------------------- |
| Language   | **Kotlin 2.0.21**                     | Modern Android-first language         |
| UI         | **Jetpack Compose 1.6.5**             | Declarative, fast iteration           |
| Background | **Foreground Service + Notification** | Robust, permission-friendly           |
| State      | **ViewModel / StateFlow**             | Lifecycle-aware reactive state        |
| Networking | **OkHttp / Retrofit**                 | Efficient, direct HTTPS calls         |
| AI         | **Whisper + Chat Completions API**    | High-quality speech & text refinement |
| Storage    | **Room / DataStore** (local-only)     | Settings & future user examples       |
| Clipboard  | **ClipboardManager**                  | Convenient auto-copy                  |

---

## 7 User Interface Layout (Overlay)

```
┌───────────────────────────────────────┐
│                                       │  ← underlying app (scrollable)
├───────────────────────────────────────┤
│          Bottom-Sheet Overlay         │
│ ┌───────────────────────────────┐     │
│ │ Read-only TextArea (80%)      │  ↑  │
│ │ • GPT output                  │  │  │
│ │ • scrollable if long          │ 80% │
│ └───────────────────────────────┘  │  │
│ │ Tone Buttons (SMS | Email | Pro) │  │
│ └───────────────────────────────────┘ │
│                                       │
│ ┌─FAB Column (20%)─────────────────┐  │
│ │ 1. Close (X)                     │  │
│ │ 2. EE logo (future settings)     │20%│
│ │ 3. Mic (record)                  │  │
│ │ 4. Copy                          │  │
│ └───────────────────────────────────┘ │
└───────────────────────────────────────┘
```

---

## 8 Notes & Future Enhancements

- Editable TextArea output (v2)

- Cancel recording mid-way

- Quick-record from notification

- User-customized tone training examples

- Optional analytics (privacy-respecting, off by default)

- Potential floating bubble overlay in future

---

*End of document*
