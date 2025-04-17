# Edit Echo — User Flow, Architecture & Tech Stack

_(Updated April 18 2025 – persistent‑notification MVP)_

---

## 1  Overview

Edit Echo is an **Android‑first voice‑to‑text assistant** for busy people on the move. It captures voice input anywhere, refines it with AI, and returns polished text in seconds. The pivot from a floating “chat‑head” bubble to a **persistent notification trigger** makes the MVP simpler, more robust, and fully compatible across Android versions.

---

## 2  Core User Flow (MVP v0.6)

| Step                                       | Action                                                                                                                        | Detail                                                                                                                                                                                                                     |
| ------------------------------------------ | ----------------------------------------------------------------------------------------------------------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **1. Persistent Notification (Always‑on)** | A silent, non‑dismissible notification lives in the shade while the Edit Echo background service runs.                        | • Shows app icon + “Tap to compose with Edit Echo” text.<br>• Optional quick‑action buttons (Record, Open Settings) can be added later.                                                                                    |
| **2. Open Overlay**                        | User taps the notification.                                                                                                   | • A Compose **bottom‑sheet overlay** slides up from the bottom 40 % of the screen.<br>• The underlying app (e‑mail, docs, browser, etc.) remains fully interactive—users can scroll and reference content while dictating. |
| **3. Tone Selection**                      | Three pill buttons inside the overlay let the user pick a tone before recording:<br>SMS \| Casual Email \| Professional Email | • Default to the last tone used.<br>• Selecting a tone updates in‑memory state only.                                                                                                                                       |
| **4. Voice Input**                         | User presses the mic FAB (Floating Action Button) inside the overlay.                                                         | • Audio is streamed to **OpenAI Whisper**.<br>• Audio is discarded after transcription.                                                                                                                                    |
| **5. Processing**                          | Transcribed text is added to the prompt template that matches the chosen tone.                                                | • Prompt sent to **OpenAI Assistants API** for refinement.                                                                                                                                                                 |
| **6. Output**                              | GPT response appears in a read‑only TextArea.                                                                                 | • Text is auto‑copied to clipboard via `ClipboardManager`.<br>• User can manually copy again if needed.                                                                                                                    |
| **7. Close Overlay**                       | User taps the **close (X)** icon in the FAB column.                                                                           | • Overlay animates down; **notification remains** for next use.<br>• **Tapping outside the overlay does *not* dismiss** it, allowing scrolling in the underlying app.                                                      |

---

## 3  Data‑Handling Model

| Scope             | Stored?                     | Notes                                                     |
| ----------------- | --------------------------- | --------------------------------------------------------- |
| **Voice audio**   | No                          | Streamed directly to Whisper; never written to disk.      |
| **Transcription** | RAM only                    | Discarded when overlay closes or new recording starts.    |
| **Refined text**  | RAM only                    | Lives in TextArea until overlay closes or is overwritten. |
| **User settings** | Local DB (Room + DataStore) | Tone default, optional example messages (Pro).            |

---

## 4  System Architecture

### 4.1 Frontend (Android App)

| Component                         | Purpose                                                                                         |
| --------------------------------- | ----------------------------------------------------------------------------------------------- |
| **PersistentNotificationService** | Starts foreground service, posts ongoing notification, routes tap intents to `OverlayActivity`. |
| **OverlayActivity** (transparent) | Hosts a single Compose `BottomSheetScaffold`.                                                   |
| **OverlayUI (Compose)**           | Bottom sheet layout: read‑only text area + tone buttons (left 80 %), FAB column (right 20 %).   |
| **RecorderManager**               | Starts/stops audio recording and streams to Whisper.                                            |
| **ViewModel + StateFlow**         | Holds overlay UI state (tone, recording status, GPT output).                                    |

### 4.2 Backend Services

Identical to previous plan—Firebase wrapper in Cloud Functions proxies calls to:

* **OpenAI Whisper** (speech‑to‑text)  
* **OpenAI Assistants API** (text refinement)

---

## 5  Tech Stack Summary

| Layer      | Tech                                  | Rationale                                         |
| ---------- | ------------------------------------- | ------------------------------------------------- |
| Language   | **Kotlin 2.0.21**                     | Modern Android‑first language                     |
| UI         | **Jetpack Compose 1.6.5**             | Declarative, fast iteration                       |
| Background | **Foreground Service + Notification** | Stable across OEMs; no overlay permissions needed |
| State      | **ViewModel / StateFlow**             | Lifecycle‑aware reactive state                    |
| Backend    | **Firebase Functions**                | Simple serverless proxy to OpenAI                 |
| AI         | **Whisper + Assistants API**          | Best‑in‑class speech & text                       |
| Storage    | **Room / DataStore** (local‑only)     | Settings & user examples                          |
| Clipboard  | **ClipboardManager**                  | Auto‑copy refined text                            |

---

## 6  User Interface Layout (Overlay)

```text
┌───────────────────────────────────────┐
│                                       │  ← underlying app (scrollable)
├───────────────────────────────────────┤
│          Bottom‑Sheet Overlay         │
│ ┌───────────────────────────────┐     │
│ │ Read‑only TextArea (80 %)     │  ↑  │
│ │ • shows GPT output            │  │  │
│ │ • scrollable if long          │ 80 %│
│ └───────────────────────────────┘  │  │
│ │ Tone Buttons (SMS | Email | Pro) │  │
│ └───────────────────────────────────┘  │
│                                       │
│ ┌─FAB Column (20 %)─────────────────┐  │
│ │ 1. Close (X)                      │  │
│ │ 2. EE logo (future settings)      │ 20 %│
│ │ 3. Mic (rec)                      │  │
│ │ 4. Copy                           │  │
│ └───────────────────────────────────┘  │
└───────────────────────────────────────┘
```



* - Bottom‑sheet overlay (~40 % height, rounded top corners)
    
    - **Left 80 %**: read‑only scrollable TextArea
    
    - Tone buttons (SMS / Casual Email / Pro) directly beneath
  
  - **Right 20 %** vertical FAB stack: Close (X), EE logo (settings), Mic (record), Copy
  
  - Background touch **does NOT dismiss**; only the Close FAB does.

---

## 7  Notes & Future Enhancements

* Add user‑editable TextArea (v2).  
* Async Whisper streaming to cut latency further.  
* Quick‑reply actions in the notification (record without opening overlay).  
* iOS investigation post‑MVP.  
* Optional analytics (privacy‑respecting, off by default).

---

_End of document_
