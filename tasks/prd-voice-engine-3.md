# Product Requirements Document: Voice Engine 3.0 Implementation

## Introduction/Overview

Voice Engine 3.0 replaces the current universal formality + polish slider system with a context-first approach where users select their communication intent (Tone) first, then adjust polish within that tone's natural formality boundaries. This prevents unnatural tone-formality combinations (e.g., playful + legal language) while preserving authentic voice through personalized Voice DNA patterns.

**Problem Statement**: The current 2-slider system (Formality + Polish) can create unnatural combinations where users accidentally apply inappropriate formality levels to specific communication intents, resulting in text that doesn't sound authentic or contextually appropriate.

**Goal**: Implement a tone-first interface that guides users to natural formality ranges while maintaining their authentic voice through pre-extracted DNA patterns.

## Goals

1. **Replace** the current 2-slider interface with a tone-first approach (dropdown + polish slider)
2. **Prevent** unnatural tone-formality combinations through guided polish ranges
3. **Preserve** user's authentic voice using pre-extracted Voice DNA patterns
4. **Maintain** the existing record → transcribe → edit workflow without disruption
5. **Enable** confidence-based DNA blending for robust output quality

## User Stories

**As a user, I want to:**
- Select my communication intent (tone) before adjusting polish levels
- See polish options that make sense for my selected tone
- Get edited text that sounds authentically like me in the chosen context
- Use the same familiar recording workflow without learning new steps
- Have the system automatically handle voice authenticity behind the scenes

**As a developer, I want to:**
- Use pre-extracted DNA patterns without rebuilding voice analysis
- Map polish slider positions to appropriate formality levels per tone
- Send the correct DNA patterns to the Claude API based on confidence levels
- Maintain the existing recording and transcription infrastructure

## Functional Requirements

### 3.1 User Interface Requirements

**R3.1.1** Replace the current 2-slider interface with:
- **Tone Dropdown**: Fixed 5 options (Casual, Neutral, Informative, Supportive, Thoughtful)
- **Polish Slider**: Visual range 0-100, maps to tone-specific formality ranges

**R3.1.2** Polish slider must display **dynamic micro-labels** that change based on selected tone:
- Labels represent the low and high formality boundaries for that tone
- Examples: Casual (relaxed ↔ clear), Thoughtful (brief ↔ structured)

**R3.1.3** User interaction flow:
1. User selects tone from dropdown
2. User adjusts polish slider (micro-labels update automatically)
3. User hits record and speaks
4. Recording transcribed via existing Whisper AI
5. Transcribed text + DNA and polish levels is sent to Claude Sonnet 4 and edited
6. Edited text appears and copies to clipboard

### 3.2 Formality Mapping Requirements

**R3.2.1** Polish slider (0-100) must map to tone-specific formality ranges:
- **Casual**: 0% = 10% formality, 100% = 40% formality
- **Neutral**: 0% = 20% formality, 100% = 70% formality  
- **Informative**: 0% = 25% formality, 100% = 75% formality
- **Supportive**: 0% = 20% formality, 100% = 60% formality
- **Thoughtful**: 0% = 30% formality, 100% = 70% formality

**R3.2.2** Formality calculation formula:
```
actualFormality = minFormality + ((maxFormality - minFormality) * polishLevel / 100)
```

**R3.2.3** Map calculated formality to formality bands:
- **Low**: 0-20% formality
- **Low-Mid**: 21-40% formality  
- **Mid**: 41-60% formality
- **Mid-High**: 61-80% formality
- **High**: 81-100% formality

### 3.3 DNA Selection Logic Requirements

**R3.3.1** For each edit request, determine DNA patterns to use:
- **Primary**: Use tone-specific DNA (e.g., "Casual" DNA)
- **Fallback**: If tone confidence < 0.7, also include formality band DNA

**R3.3.2** Confidence-based blending logic:
- **High tone confidence (≥0.7)**: Use tone DNA patterns with formality guidance
- **Low tone confidence (<0.7)**: Use formality band DNA patterns with tone intent
- **Very low confidence**: Use template prompts with minimal personalization

**R3.3.3** Formality band DNA selection based on calculated formality level:
- 0-20% → Low formality band DNA
- 21-40% → Low-Mid formality band DNA
- 41-60% → Mid formality band DNA  
- 61-80% → Mid-High formality band DNA
- 81-100% → High formality band DNA

### 3.4 API Integration Requirements

**R3.4.1** Update Claude API prompt to include:
- Selected tone and calculated formality level
- Appropriate DNA pattern(s) based on confidence logic
- Original transcribed text for editing

**R3.4.2** Maintain existing API infrastructure:
- Same Claude Sonnet 4 endpoint
- Same error handling and retry logic
- Same response processing and clipboard copying

## Non-Goals (Out of Scope)

- **User customization**: No ability to modify tones, ranges, or DNA patterns
- **Multi-user support**: Implementation only works for the primary user
- **Recording/transcription changes**: Existing Whisper AI integration remains unchanged
- **DNA extraction**: Using pre-existing DNA patterns, no new voice analysis
- **A/B testing**: Single implementation, no alternative interfaces
- **Analytics**: No usage tracking or performance metrics for this version

## Design Considerations

### 3.5 UI Components

**Tone Dropdown**:
- Style consistent with existing app design
- Clear labels for each tone option
- Default selection: Neutral

**Polish Slider**:
- Fixed visual width matching current design
- Dynamic micro-labels at 0% and 100% positions
- Smooth sliding interaction with immediate label updates

### 3.6 Micro-label Specifications

Based on tone selection, display these micro-labels:

| Tone | Low Label | High Label |
|------|-----------|------------|
| Casual | relaxed | clear |
| Neutral | simple | structured |
| Informative | empathetic | direct |
| Supportive | empathetic | reassuring |
| Thoughtful | brief | structured |

## Technical Considerations

### 3.7 Implementation Details

**Frontend Changes**:
- Replace slider components with dropdown + single slider
- Implement dynamic label updating on tone selection
- Calculate formality values before API calls

**Backend Changes**:
- Update prompt construction logic to use DNA patterns
- Implement confidence-based DNA selection
- Maintain existing Claude API integration patterns

**Data Integration**:
- Use pre-extracted DNA patterns from `all DNAs.txt`
- No database changes required for this version

### 3.8 DNA Pattern Usage

**Available DNA Objects**:
- 5 Tone-specific DNA patterns (Casual, Neutral, Informative, Supportive, Thoughtful)
- 5 Formality band DNA patterns (Low, Low-Mid, Mid, Mid-High, High)

**Prompt Construction Strategy**:
- High confidence: Include tone DNA patterns with formality target
- Low confidence: Include formality band DNA with tone intent guidance
- Always preserve user's voice constants and markers

## Success Metrics

**Primary Success Criteria**:
- **Functional Implementation**: All 5 tones work with appropriate formality mapping
- **UI Responsiveness**: Micro-labels update correctly on tone selection
- **Voice Preservation**: Output maintains user's authentic voice patterns
- **API Integration**: Claude API receives properly formatted prompts with DNA

**Testing Approach**:
- Manual testing by primary user over several weeks
- Iterative feedback and adjustments based on output quality
- Focus on authenticity and contextual appropriateness

## Open Questions

1. **Error Handling**: How should the system behave if DNA patterns are missing or corrupted?
2. **Performance**: Are there latency concerns with more complex prompt construction?
3. **User Feedback**: Should there be a mechanism for users to rate output quality?
4. **Edge Cases**: How to handle very short transcriptions or unclear speech?

---

**Target Audience**: Development team implementing Voice Engine 3.0
**Implementation Priority**: High - Replace existing system completely
**Dependencies**: Pre-extracted DNA patterns, existing Claude API integration 