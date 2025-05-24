# Edit Echo — User Flow, Architecture & Tech Stack

*(Updated May 23, 2025 – Fake Keyboard UI & Personalized Tones)*

---

## 1 Overview

Edit Echo is an **Android-first voice-to-text assistant** that transforms spoken thoughts into polished, context-aware text. It positions itself as a "fake keyboard" overlay that appears exactly where the system keyboard would, eliminating layout jank while providing AI-powered message refinement in seconds.

**Core Promise:** "Talk it. Tidy it. Send it."

---

## 2 Core User Flow (Target v1.0)

| Step | Action | Detail |
|------|--------|--------|
| **1. Persistent Notification** | Always-on notification while background service runs | • "Tap to compose with Edit Echo"<br>• Currently has swipe-away bug (force-close required to restore) |
| **2. Open "Fake Keyboard"** | User taps notification → overlay slides up from bottom | • Same height/position as system keyboard would occupy<br>• Real keyboard hides; app layout stays "keyboard raised"<br>• Width: MATCH_PARENT, Height: last IME height (~250-300dp) |
| **3. Tone Selection** | User selects from 4 personalized tones + "Transcribe Only" | • Tones are AI-generated from user's example messages<br>• Defaults to last tone used<br>• Updates stored in local preferences |
| **4. Voice Input** | User presses mic button in overlay | • Audio streams directly to OpenAI Whisper API<br>• Audio discarded immediately after transcription |
| **5. AI Processing** | Transcribed text + selected tone → Chat Completions API | • System prompt built from user's examples + tone brief<br>• Streaming response displayed in real-time |
| **6. Output & Auto-Copy** | Refined text displayed and auto-copied to clipboard | • Text automatically copied via ClipboardManager<br>• User can paste anywhere immediately |
| **7. Close Overlay** | Only X button closes (no tap-outside dismiss) | • Overlay disappears → system keyboard reappears<br>• Notification remains active for next use |

---

## 3 Revolutionary "Fake Keyboard" UI

### 3.1 Design Philosophy
- **No layout jank**: App thinks keyboard is still open
- **Natural positioning**: Overlay occupies exact keyboard space
- **Pass-through interaction**: Users can scroll/interact with underlying app
- **One-tap close**: Only X button dismisses (prevents accidental closure)

### 3.2 Implementation Strategy
```
┌─────────────────────────────────────┐
│         Host App (scrollable)       │ ← User can interact
│                                     │
├─────────────────────────────────────┤
│    Edit Echo "Fake Keyboard"        │
│ ┌─────────────────────────────────┐ │
│ │ Refined Text Area (streaming)   │ │ ← AI output appears here
│ └─────────────────────────────────┘ │
│ [Friendly] [Direct] [Warm] [Pro] [T] │ ← Personalized tone buttons
│                                 [X] │ ← Only way to close
│                               [🎤] │ ← Record button
└─────────────────────────────────────┘
```

---

## 4 Personalized Tone System

### 4.1 Dynamic Tone Creation
1. **User Upload**: User pastes 10-20 of their real messages
2. **AI Analysis**: GPT analyzes examples using tone_creator prompt
3. **Tone Generation**: System creates 4 personalized tones with:
   - Short displayName (e.g., "Warm", "Direct")
   - One-sentence description for UI
   - 2-3 line editing brief
   - Representative keywords
   - Example message assignments

### 4.2 Tone Structure (Per User)
```json
{
  "tones": [
    {
      "id": "WARM_PERSONAL",
      "displayName": "Warm",
      "description": "Friendly and conversational for close colleagues",
      "quickBrief": "Use contractions and softeners.\nKeep an approachable, genuine tone.\nPreserve personal touches and warmth.",
      "keywords": ["warm", "genuine", "conversational"],
      "exampleIndices": [1, 4, 7, 12]
    }
  ]
}
```

### 4.3 Current Default Tones (Pre-Personalization)
- **Friendly**: Warm, conversational, close peers
- **Engaged**: Balanced, informative with friendly twist  
- **Direct**: Professional, clear, concise
- **Reflective**: Thoughtful, introspective, sincere
- **Transcribe Only**: Raw transcription, no AI editing

---

## 5 System Architecture

### 5.1 Frontend (Android App)
| Component | Purpose |
|-----------|---------|
| **NotificationService** | Foreground service with persistent notification |
| **OverlayService** | WindowManager-based floating overlay (current)<br>→ **Refactoring to:** Fake keyboard positioning |
| **KeyboardInsetsObserver** | Detects/stores system keyboard height |
| **OverlayUI (Compose)** | Bottom-anchored panel with tone picker + record button |
| **UsageTracker** | SessionLog → DailyUsage rollup for subscription tiers |
| **ToneManager** | Handles personalized tone creation/storage |

### 5.2 Backend Services
- **OpenAI Whisper API**: Speech-to-text transcription
- **OpenAI Chat Completions API**: Text refinement with personalized prompts
- **Firebase**: Analytics, configuration, future cloud sync

### 5.3 Data Architecture

#### Time Tracking (Subscription Support)
```kotlin
@Entity(tableName = "session_log")  // Ephemeral, wiped daily
data class SessionLog(
    val timestamp: Long,      // When session ended
    val durationMillis: Long  // Recording duration
)

@Entity(tableName = "daily_usage")  // Persistent
data class DailyUsage(
    val localDate: String,    // ISO-8601 "2025-05-23"
    val totalMillis: Long,    // Aggregate recording time
    val sessionCount: Int     // Number of sessions
)
```

#### User Personalization
```kotlin
@Entity(tableName = "user_tones")
data class UserTone(
    val id: String,           // "WARM_PERSONAL"
    val displayName: String,  // "Warm"
    val description: String,  // UI description
    val quickBrief: String,   // Editing instructions
    val keywords: List<String>,
    val exampleMessages: List<String>
)
```

---

## 6 Subscription Business Model

### 6.1 Pricing Tiers
| Tier | Price/Week | Monthly Equiv | Features |
|------|------------|---------------|----------|
| **Free** | $0 | - | Basic transcription, limited usage |
| **Standard** | $0.99 | ~$4.30 | 100 min/week, tone refinement |
| **Pro** | $1.99 | ~$8.60 | 4 hours/week transcription |
| **Xtreme** | Pay-per-use | $1/hour | Unlimited after $10 prepaid balance |

### 6.2 Usage Tracking Display
- **Daily**: Sum of today's SessionLogs
- **Weekly**: Last 7 days of DailyUsage  
- **All-Time**: Total across all DailyUsage
- **Format**: "2h 45m / 4h WK" (tap to cycle D/WK/AT)

---

## 7 Tech Stack Summary

| Layer | Technology | Rationale |
|-------|------------|-----------|
| **Language** | Kotlin 2.0.21 | Modern Android development |
| **UI** | Jetpack Compose 1.6.5 | Declarative, reactive UI |
| **Architecture** | MVVM + StateFlow | Lifecycle-aware state management |
| **Background** | Foreground Service + WindowManager | Persistent overlay capability |
| **Networking** | Retrofit + OkHttp + SSE | Direct OpenAI API integration |
| **AI** | Whisper + Chat Completions | High-quality speech & text processing |
| **Storage** | Room + DataStore | Local user data & preferences |
| **Analytics** | Firebase | Usage tracking, configuration |
| **Clipboard** | ClipboardManager | Seamless text copying |

---

## 8 Current Refactor Roadmap

### Phase 1: Fake Keyboard Foundation
1. **KeyboardInsetsObserver**: Track system keyboard height
2. **Bottom-Anchored Overlay**: MATCH_PARENT × keyboard height
3. **Dynamic Height Binding**: Live height updates from insets
4. **IME Hide/Restore**: Coordinate with system keyboard
5. **Touch-Through Rules**: Remove tap-outside dismiss
6. **Orientation Handling**: Landscape fallbacks
7. **Polish & Testing**: Service lifecycle, QA matrix

### Phase 2: Personalization Engine  
1. **Example Upload UI**: Settings screen for message input
2. **Tone Analysis Pipeline**: GPT-powered tone extraction
3. **Tone Management**: Edit/rename generated tones
4. **Prompt System Refactor**: Dynamic prompts from user tones

### Phase 3: Business Features
1. **Usage Tracking Integration**: SessionLog → DailyUsage
2. **Subscription Logic**: Tier limits and upgrade prompts
3. **Firebase Integration**: Analytics and remote config
4. **Notification Persistence Fix**: Survive swipe-away

---

## 9 Key Technical Challenges

### 9.1 Fake Keyboard Implementation
- **Inset Detection**: Robust cross-API keyboard height detection
- **Race Conditions**: Ensure inset data before hiding keyboard
- **Accessibility**: Maintain TalkBack compatibility with overlay flags
- **Performance**: Minimize battery drain from persistent service

### 9.2 Personalization Engine
- **Tone Quality**: Ensure AI-generated tones are distinctly useful
- **Example Processing**: Handle varied message formats/lengths
- **Prompt Engineering**: Balance personalization with reliability

### 9.3 Business Viability
- **Usage Tracking Accuracy**: Only count successful API calls
- **Cost Management**: Monitor OpenAI API spend vs revenue
- **User Retention**: Convert free users to paid tiers

---

## 10 Success Metrics

### 10.1 Product Metrics
- **Time Saved**: 4 minutes saved per minute of audio recorded (speaking vs mobile typing)
- **Usage Frequency**: Daily active sessions per user
- **Tone Adoption**: Which personalized tones see most use
- **Refinement Quality**: User satisfaction with AI output

### 10.2 Business Metrics  
- **Conversion Rate**: Free → Standard tier upgrades
- **Usage Efficiency**: Recording minutes vs subscription limits
- **Unit Economics**: Revenue per user vs OpenAI API costs
- **Retention**: Weekly/monthly active user persistence

---

*This architecture supports Edit Echo's evolution from MVP to sustainable voice-AI business, with personalized user experience at its core.*