# Edit Echo - Todo Backlog & Feature Priorities

## Critical Path (Blocking Daily Usage)
**Priority 1 - Fix Immediately**

### 🔴 Notification Persistence
- **Issue**: Notification disappears when swiped away, requires force-close + reopen to restore
- **Impact**: Breaks core user flow, prevents reliable daily usage
- **Fix**: Improve service restart logic, handle `onTaskRemoved()` properly
- **Business Impact**: Critical - app unusable if entry point fails

### 🔴 Transcribe Only Clipboard Bug  
- **Issue**: Raw transcriptions don't auto-copy to clipboard in Transcribe Only mode
- **Impact**: Inconsistent UX across tone modes
- **Fix**: Ensure clipboard logic runs for all tone types including TRANSCRIBE_ONLY
- **Business Impact**: High - free tier feature must work perfectly

## Core Experience Enhancement
**Priority 2 - Next Sprint**

### 🟡 Fake Keyboard Overlay Implementation
- **Goal**: Position overlay exactly where system keyboard appears
- **Benefit**: Eliminates layout jank, premium UX differentiator
- **Implementation**: KeyboardInsetsObserver → dynamic height binding → IME coordination
- **Business Impact**: High - core differentiator vs competitors

### 🟡 Stable vs Dev Build Separation
- **Goal**: Two app flavors for daily use vs testing
- **Benefit**: Ship confidently while experimenting safely
- **Implementation**: Gradle build variants, separate package names
- **Business Impact**: Medium - enables faster iteration

## Personalization Engine
**Priority 3 - Major Feature**

### 🟠 User Example Upload System
- **Goal**: Settings UI for users to paste 10-20 real messages
- **Implementation**: Settings screen → text input → validation → storage
- **Business Impact**: High - core premium differentiator

### 🟠 AI Tone Generation
- **Goal**: Analyze user examples → generate 4 personalized tones
- **Implementation**: Use tone_creator.txt prompt → OpenAI call → parse JSON response
- **Business Impact**: Very High - primary value proposition vs generic tools

### 🟠 Tone Management Interface
- **Goal**: Edit/rename generated tones, preview changes
- **Implementation**: Settings overlay → tone editing → real-time preview
- **Business Impact**: Medium - improves user control and satisfaction

### 🟠 Voice Profiling from Usage History
- **Goal**: Auto-detect tone patterns from saved transcription pairs
- **Implementation**: Analyze history.txt → ML pattern detection → tone suggestions
- **Business Impact**: Medium - automated personalization improvement

## Business Model Support
**Priority 4 - Monetization**

### 🔵 Usage Tracking Display
- **Goal**: Show recording minutes used vs limits (D/WK/AT toggle)
- **Implementation**: SessionLog aggregation → UI counters → tier limit warnings
- **Business Impact**: High - drives upgrade awareness

### 🔵 Recording Counter Accuracy
- **Goal**: Only count time if Whisper API call succeeds
- **Implementation**: Track duration in ViewModel, log only on successful transcription
- **Business Impact**: Medium - ensures billing accuracy

### 🔵 Firebase Configuration Externalization
- **Goal**: Move prompts/config to remote for live updates
- **Implementation**: Firebase Remote Config → prompt templates → dynamic loading
- **Business Impact**: Medium - enables rapid A/B testing

### 🔵 Voice Protection Slider (Temperature Control)
- **Goal**: 0-9 slider controlling GPT creativity (0.5-5.0 temperature range)
- **Implementation**: Settings UI → map to OpenAI temperature parameter
- **Business Impact**: Low - power user feature

## Performance & Reliability  
**Priority 5 - Polish**

### 🟢 Progressive Audio Chunking
- **Goal**: Break long recordings into 10s chunks, send progressively
- **Implementation**: MediaRecorder → chunk buffer → parallel Whisper calls
- **Business Impact**: Low - optimization for edge cases

### 🟢 API Key Security
- **Goal**: Encrypt API key storage before beta release
- **Implementation**: Android Keystore → encrypted SharedPreferences
- **Business Impact**: Medium - security requirement for public release

## Implementation Strategy
- **Sprint 1 (2 weeks)**: Critical Path items - notification + clipboard fixes
- **Sprint 2 (2 weeks)**: Fake keyboard implementation + build separation  
- **Sprint 3 (4 weeks)**: Personalization engine core features
- **Sprint 4 (2 weeks)**: Usage tracking + business features
- **Sprint 5 (2 weeks)**: Polish + security hardening

## Success Criteria
- **Daily Usage**: Iain uses app consistently without friction
- **Time Savings**: Measurable 4:1 efficiency vs mobile typing
- **Personalization Quality**: Generated tones feel distinctly different and useful
- **Business Readiness**: Usage tracking accurate, subscription logic working