# Voice Engine 3.0 - Formality & Tone Reference Guide and Implementation

## Key Changes from Voice Engine 2.0

- **Expanded from 4 tones to 12 comprehensive tones** covering all communication intents
- **Introduced tone-specific formality ranges** preventing unnatural tone-formality combinations
- **Added confidence-based DNA blending** using both tone-specific and formality-band patterns
- **Implemented dynamic tone selection** based on user examples rather than fixed presets
- **Enhanced polish slider with tone-aware micro-labels** for intuitive user control

## Overview

This guide maps the relationship between formality levels and communication tones for Edit Echo's Voice Engine 3.0. Understanding these relationships ensures the AI generates contextually appropriate text that maintains authenticity while adapting to different professional and social contexts.

### Product Vision

Edit Echo empowers users to speak naturally and effortlessly convert raw voice-to-text transcripts into polished, context-appropriate text that authentically matches their personal communication style. 

### Core User Problem

Voice-to-text transcription on mobile generates raw, error-prone, and unstructured outputs that require significant manual editing. Users must painstakingly adapt their speech for different contexts (e.g., work email, casual chat), causing cognitive overhead and frustration. 

### Differentiator

The Edit Echo system uniquely preserves the user's authentic voice while transforming transcripts according to explicit tone selection and polish preferences, drastically reducing the need for manual edits.

### Key Principles of 3.0:

* The english written language can be divided up into 12 broad Tones (eg. Casual, Professional). 

* Formality in the english written language can be described in a 0-100 range, with 11 overlapping formality zones that map natural communication patterns (0=raw txt speak. 100 = High formal constitution document)

* Every tone has an inherent formality range. Attempting to combine incompatible tone-formality pairs (e.g., playful + legal language) creates unnatural, ineffective communication.

* Polish is a micro scale of formality within formality ranges, and creates grey edges between tones and formalities. Low polish on one formality level is the same as high polish on a lower level 

---

## Formality Zones (0-100%)

| Zone        | Label                     | Purpose                                                            | Example                                                                                                      |
| ----------- | ------------------------- | ------------------------------------------------------------------ | ------------------------------------------------------------------------------------------------------------ |
| **0-15%**   | Text speak / fragments    | Instant messaging, gaming chat, pure reaction with minimal grammar | "lol ur wild fr fr no cap"                                                                                   |
| **10-30%**  | Casual digital            | Social media, Discord, complete thoughts but very relaxed          | "gonna grab food rn, anyone wanna come? place is pretty fire"                                                |
| **20-40%**  | Friendly chat             | Personal emails, texts with friends, blog comments                 | "Hey! Just wanted to check if you're still coming tonight. Let me know!"                                     |
| **30-50%**  | Relaxed work talk         | Slack messages, internal team emails, casual office communication  | "Hi team, quick update - we're making good progress and should hit our deadline."                            |
| **40-60%**  | Standard office           | General business emails, newsletters, everyday professional        | "Dear Mr. Johnson, I'm writing to follow up on our meeting regarding the quarterly projections."             |
| **50-70%**  | Polished business         | Client communications, proposals, external-facing professional     | "We are pleased to announce our partnership with XYZ Corporation, which will enhance our service offerings." |
| **60-80%**  | Corporate formal          | Policy documents, compliance materials, official announcements     | "The institution hereby notifies all stakeholders that the proposed amendments will take effect as of..."    |
| **70-85%**  | Academic / technical      | Research papers, technical documentation, expert-to-expert         | "This study examines the correlation between variables X and Y, utilizing a mixed-methods approach..."       |
| **75-90%**  | Ceremony / diplomatic     | Formal invitations, awards, official government correspondence     | "The pleasure of your company is requested at the marriage of..."                                            |
| **80-95%**  | Legal language            | Contracts, court documents, terms and conditions                   | "WHEREAS, the party of the first part, hereinafter referred to as..."                                        |
| **85-100%** | Historic / archaic formal | Treaties, constitutional documents, traditional proclamations      | "We, the undersigned plenipotentiaries, having communicated our respective full powers..."                   |

---

## Tone Definitions with Formality Mapping

| Tone             | Description                                           | Formality Range | Primary Zone | Example                                                                                                    |
| ---------------- | ----------------------------------------------------- | --------------- | ------------ | ---------------------------------------------------------------------------------------------------------- |
| **Playful**      | Humorous, entertaining language                       | 5-35%           | 10-25%       | "If I debug one more null pointer today, I'm gonna need a support group 😂"                                |
| **Casual**       | Relaxed, informal social chat                         | 10-40%          | 15-30%       | "hey mate, wanna grab coffee later? thinking maybe 3ish?"                                                  |
| **Supportive**   | Empathetic and reassuring communication               | 20-60%          | 25-45%       | "I understand this is challenging - you're doing great and I'm here if you need help."                     |
| **Appreciative** | Expressions of gratitude, praise, and recognition     | 15-75%          | 30-55%       | "Thank you so much for staying late to finish this - your dedication really made the difference!"          |
| **Apologetic**   | Remedial language acknowledging fault or mistakes     | 20-70%          | 35-55%       | "I'm sorry for the confusion my earlier email caused - let me clarify what I meant..."                     |
| **Thoughtful**   | Introspective, reflective exploration of ideas        | 30-70%          | 40-60%       | "I've been considering how this fits our long-term vision, and I wonder if we're thinking too narrowly..." |
| **Neutral**      | Plain, factual updates without subjective stance      | 20-70%          | 35-55%       | "The meeting has been rescheduled to Tuesday at 2pm in Conference Room B."                                 |
| **Action**       | Direct requests, task assignments, clear instructions | 30-70%          | 40-60%       | "Please review the contract by EOD tomorrow and flag any concerns."                                        |
| **Informative**  | Detailed explanations, step-by-step information       | 25-75%          | 35-65%       | "To reset your password: 1) Click 'Forgot Password' 2) Enter your email 3) Check inbox for reset link"     |
| **Critical**     | Constructive evaluations highlighting issues          | 35-75%          | 45-65%       | "While the design is creative, the load times are concerning and need optimization before launch."         |
| **Persuasive**   | Convincing language aimed at influencing decisions    | 30-80%          | 40-70%       | "This approach will save us 40% in costs while improving delivery times - here's how..."                   |
| **Formal**       | Professional, structured language                     | 50-90%          | 60-80%       | "Dear Ms. Johnson, I am writing to confirm receipt of your proposal dated January 15th."                   |

### Primary Zone

The "Primary Zone" represents where each tone performs most naturally. The AI should default to this range unless the user explicitly adjusts formality settings.

### Boundary Behavior

- **Lower boundary violations**: The tone becomes inappropriately casual, losing its intended function
- **Upper boundary violations**: The tone becomes stilted, creating register clash that sounds unnatural

### Practical Examples

**Good Combination**: Supportive tone at 40% formality

> "I know the deadline is tight, but you've got this. Let me know if you need me to review anything or grab you coffee!"

**Poor Combination**: Playful tone at 70% formality

> "We respectfully submit that the quarterly figures are, as one might colloquially express, 'absolutely bonkers' in the most favorable interpretation thereof." *(Register clash - humor doesn't work at this formality level)*

---

## UI Strategy & User Flow

### User Onboarding Flow

Step-by-Step Process:

1. **Example Capture**: Users submit 10–20 authentic written message examples.

2. **Base Analysis**: Every example message is tagged with base tone and a formality level between 0-100.

3. **Tone Diversity Selection**: From all examples, system selects 3 dynamic tones based on intent diversity, excluding Casual and Neutral.

4. **Tone Realignment**: All messages are reclassified to one of the final 5 selected tones (2 fixed + 3 dynamic), even if originally tagged to unselected tones.

5. **Tone DNA Extraction**: Run tone-specific DNA analysis on reclassified messages for each of the 5 selected tones. Confidence score logged for each tone.

6. **Formality Band Classification**: All examples are grouped into the 5 formality bands based on their 0-100 formality ratings.

7. **Formality DNA Extraction**: VoiceDNA extraction is run for each of the 5 formality bands using examples that have a formality rating inside the band. Generalized DNA patterns generated for each band, used to supplement tone-specific DNA when confidence is low.

8. **Dropdown Assembly**: Present 5-tone UI (2 fixed, 3 dynamic).

### UI Strategy

#### Dropdown Structure:

- 5-item dropdown: Casual, Neutral, +3 dynamic tones
  - **Casual & Neutral**: Fixed inclusion ensures universal coverage of daily digital communication (~70%) and professional updates (~60%), providing reliable fallback patterns when confidence is low
  - **Dynamic Tones**: Selected from remaining tones based on maximum semantic and intent diversity from user examples

#### Slider Interaction:

- One slider controls polish within tone-specific formality bounds

- Visual polish range clipped to each tone (e.g., Casual maxes at 40%)

- Slider hint text varies per tone

#### Dynamic Micro-copy (Slider Hints)

Each tone displays contextual hints that change based on slider position:

- **Casual**: "Relaxed ↔ Tidy"
- **Neutral**: "Clear ↔ Structured"
- **Action**: "Brief ↔ Direct"
- **Persuasive**: "Friendly ↔ Influential"
- **Supportive**: "Empathetic ↔ Reassuring"
- **Informative**: "Simple ↔ Detailed"
- **Appreciative**: "Warm ↔ Gracious"
- **Apologetic**: "Sincere ↔ Formal"
- **Critical**: "Frank ↔ Diplomatic"
- **Thoughtful**: "Reflective ↔ Analytical"
- **Playful**: "Silly ↔ Witty"
- **Formal**: "Professional ↔ Ceremonial" 

### Tone Selection Algorithm

1. **Diversity Scoring**: Calculate semantic distance between tones using:
   - Intent category (task, emotion, information, social)
   - Formality range overlap
   - Typical use contexts
2. **Coverage Optimization**: Ensure selected tones cover user's communication spectrum
3. **Frequency Weighting**: Prioritize tones that appear more in user examples

**Sarah's Communication Profile** (based on 15 submitted examples):

- 4 examples tagged as Action (work tasks)
- 3 examples tagged as Supportive (team feedback)
- 3 examples tagged as Neutral (status updates)
- 2 examples tagged as Thoughtful (strategy discussions)
- 2 examples tagged as Casual (team chat)
- 1 example tagged as Appreciative (thanking colleagues)

**System Selection**:

1. Fixed: Casual, Neutral (always included)
2. Dynamic selections: Action (high frequency), Supportive (distinct intent), Thoughtful (diversity)
3. Final dropdown: [Casual | Neutral | Action | Supportive | Thoughtful]

**Reallocation**: The single Appreciative example gets reassigned to Supportive tone for DNA extraction.

---

## Voice DNA Conceptual Overview

### What is VoiceDNA?

VoiceDNA is Edit Echo's core personalization system that captures and preserves a user's unique communication patterns. Unlike generic AI assistants that apply one-size-fits-all transformations, VoiceDNA learns how YOU specifically communicate across different tones and formality levels, ensuring edited text always sounds authentically like your voice.

### Core Philosophy
**Your voice is sacred.** VoiceDNA doesn't change who you are - it helps you communicate more effectively while preserving the patterns, phrases, and personality that make your communication uniquely yours.

### How VoiceDNA Works

#### 1. **Pattern Extraction**
Users upload 15-25 real messages they've written. Our AI analyzes these examples to extract:

- **Tone patterns**: How you express different intents (casual chat vs formal requests)
- **Formality patterns**: How your language changes from relaxed to professional
- **Voice constants**: What never changes about your style (punctuation, greetings, sign-offs)
- **Unique markers**: Personal phrases and patterns that identify your voice

#### 2. **Dual Classification System**

Each message example gets tagged with:
- **Primary Tone**: Best match from user's 5 selected tones
- **Base Tone**: Best match from all 12 possible tones
- **Formality Level**: 0-100% detected formality rating

#### 3. **DNA Generation**

We create **12 ToneDNA objects** (one per possible tone) and **5 FormalityDNA objects** (covering formality bands), giving us comprehensive coverage of how you communicate.

#### 4. **Smart Blending**

When editing text, we use confidence-weighted blending:
- **High tone confidence**: Use tone-specific patterns
- **Low tone confidence**: Fall back to formality band patterns from similar contexts
- **Graceful degradation**: Always have something personalized to work with

### Benefits of VoiceDNA

#### For Users
- **Authentic output**: Edited text sounds like YOU, not generic AI
- **Contextual flexibility**: Same voice, appropriate for any situation
- **Learning system**: Gets better with more examples
- **Confidence transparency**: Know when patterns are well-established

#### For Edit Echo
- **Competitive differentiation**: Unique personalization vs generic tools
- **User stickiness**: Switching costs increase with personalization investment
- **Quality consistency**: Reliable output even with limited tone examples
- **Scalable architecture**: Same system works for all communication styles

---

## Technical Implementation

### Core VoiceDNA Object

```kotlin
data class VoiceDNA(
  val tone: String,                    // "Casual" OR "Mid-Formality" 
  val confidence: Float,               // 0.0-1.0 based on example count
  val userFormalityRange: IntRange,    // e.g. 25..65 (user's actual range)
  val theoreticalRange: IntRange,      // e.g. 10..75 (design intent range)
  // Core voice patterns (natural language descriptions)
  val formalityShifts: String,         // How this tone/formality changes across levels
  val polishPatterns: String,          // How rushed vs polished versions differ
  val constants: String,               // What never changes 
  val voiceMarkers: String,            // Unique identifying patterns
  val antiPatterns: String             // What this voice never does
)
```

### Formality Zones to Bands Mapping

Voice Engine 3.0 uses five discrete formality bands to classify communication patterns, derived from the broader formality zones:

| Band | Range | Label | Communication Context |
|------|-------|-------|----------------------|
| **Low** | 0-20% | Text speak & fragments | Gaming chat, instant reactions, minimal grammar |
| **Low-Mid** | 21-40% | Casual digital | Social media, personal texts, relaxed communication |
| **Mid** | 41-60% | Standard professional | Business emails, office communication, everyday work |
| **Mid-High** | 61-80% | Polished business | Client communications, formal proposals, external-facing |
| **High** | 81-100% | Formal & ceremonial | Legal documents, academic papers, official announcements |

### Formality Band Mapping

```kotlin
enum class FormalityBand(val range: IntRange, val label: String) {
  LOW(0..20, "Low"),
  LOW_MID(21..40, "Low-Mid"), 
  MID(41..60, "Mid"),
  MID_HIGH(61..80, "Mid-High"),
  HIGH(81..100, "High")
}
fun getFormalityBand(level: Int): FormalityBand? {
  return FormalityBand.values().find { level in it.range }
}
```

### Polish Slider to Formality Mapping

```kotlin
fun calculateFormality(tone: String, polishLevel: Int): Int {
  val toneRanges = mapOf(
      "Casual" to (10 to 40),
      "Appreciative" to (15 to 75),
      "Action" to (30 to 70),
      "Formal" to (50 to 90)
      // ... other tones
  )
  val (minFormality, maxFormality) = toneRanges[tone] ?: (20 to 70)
  return minFormality + ((maxFormality - minFormality) * polishLevel / 100)
}
```

### Formality Zones Boundary Micro Labels

| Formality % | Micro Label     | Description                 |
| ----------- | --------------- | --------------------------- |
| 0%          | **raw**         | Unfiltered thoughts         |
| 5%          | **silly**       | Playful and loose           |
| 10%         | **relaxed**     | Very casual and natural     |
| 15%         | **warm**        | Friendly and approachable   |
| 20%         | **simple**      | Easy to understand          |
| 25%         | **empathetic**  | Caring and understanding    |
| 30%         | **brief**       | Concise and focused         |
| 35%         | **tidy**        | Well-organized              |
| 40%         | **clear**       | Well-structured             |
| 45%         | **sincere**     | Genuine and honest          |
| 50%         | **frank**       | Direct and honest           |
| 55%         | **reflective**  | Thoughtful consideration    |
| 60%         | **detailed**    | Comprehensive information   |
| 65%         | **reassuring**  | Confident and supportive    |
| 70%         | **structured**  | Well-organized presentation |
| 75%         | **direct**      | Clear and purposeful        |
| 80%         | **influential** | Persuasive and compelling   |
| 85%         | **gracious**    | Polite and refined          |
| 90%         | **diplomatic**  | Tactful and measured        |
| 95%         | **analytical**  | Logical and systematic      |
| 100%        | **ceremonial**  | Formal and distinguished    |

### Polish Slider Examples by Tone

| Tone             | Polish Range | Label Span                | Example Progression                                                                                       |
| ---------------- | ------------ | ------------------------- | --------------------------------------------------------------------------------------------------------- |
| **Playful**      | 5-35%        | silly → tidy              | "lol no way" → "Haha, that's quite surprising!"                                                           |
| **Casual**       | 10-40%       | relaxed → clear           | "sup" → "Hey, how's it going?"                                                                            |
| **Supportive**   | 20-60%       | empathetic → reassuring   | "you got this" → "I have complete confidence in your abilities"                                           |
| **Appreciative** | 15-75%       | warm → direct             | "thanks!" → "I am deeply grateful for your contribution"                                                  |
| **Apologetic**   | 20-70%       | simple → structured       | "sorry bout that" → "I sincerely apologize for the inconvenience"                                         |
| **Thoughtful**   | 30-70%       | brief → structured        | "thinking maybe..." → "Upon reflection, I believe..."                                                     |
| **Neutral**      | 20-70%       | simple → structured       | "meeting at 2" → "The meeting is scheduled for 2:00 PM"                                                   |
| **Action**       | 30-70%       | brief → structured        | "check this plz" → "Please review this document at your earliest convenience"                             |
| **Informative**  | 25-75%       | empathetic → direct       | "here's how: do X then Y" → "Please follow these steps: First, complete X. Subsequently, proceed with Y." |
| **Critical**     | 35-75%       | tidy → direct             | "this needs work" → "This requires substantial improvement to meet our standards"                         |
| **Persuasive**   | 30-80%       | brief → influential       | "this'll save money" → "This initiative will yield substantial cost efficiencies"                         |
| **Formal**       | 50-90%       | frank → diplomatic        | "I'm writing about..." → "I have the honor to address you regarding..."                                   |

### Example VoiceDNA Objects

#### ToneDNA Example: Casual

```kotlin
val casualToneDNA = VoiceDNA(
  tone = "Casual",
  confidence = 0.85f,
  userFormalityRange = 15..35,  // User never goes formal in casual mode
  theoreticalRange = 10..40,
  formalityShifts = """
      • Low (15-20): lowercase ok ('hey.'), 'prob', 'mate', no "?", no greeting.
      • Mid (25-30): "Hey Sam," mixed fragments, casual but complete.
      • High (30-35): "Hi Sam.", caps on, brief context, "Cheers,\nSarah".
      Greeting: none → Hey → Hi.
  """.trimIndent(),
  polishPatterns = """
      • Raw (0-30): fragments, minimal punctuation ("just checked... looks good").
      • Polished (50-100): complete thoughts, clear structure, decision first.
  """.trimIndent(),
  constants = """
      Always: spaced em-dash ( – ), line break per thought, "All good" for reassurance.
      Contractions frequent, sentence-end "sorry", action/decision first.
  """.trimIndent(),
  voiceMarkers = """
      "ish" times (3:30ish), "prob" not "probably", "mate" in greetings, 
      questions drop "?", punchy over long sentences.
  """.trimIndent(),
  antiPatterns = """
      Never: "Dear/Greetings", corporate speak, excessive caps, "furthermore", 
      overly formal language that doesn't match casual intent.
  """.trimIndent()
)
```

#### FormalityDNA Example: Mid-Formality Band

```kotlin
val midFormalityDNA = VoiceDNA(
  tone = "Mid-Formality",  // 41-60% formality band
  confidence = 0.92f,      // High confidence from 18 examples across tones
  userFormalityRange = 42..58,  // User's actual mid-range usage
  theoreticalRange = 41..60,
  formalityShifts = """
      • Entry (41-45): "Hi [name]," complete sentences, professional but warm.
      • Core (50-55): Standard business style, context provided, clear structure.
      • Peak (56-60): Polished professional, formal greetings, detailed context.
  """.trimIndent(),
  polishPatterns = """
      • Basic (41-50): clear communication, proper grammar, organized thoughts.
      • Refined (51-70): sophisticated structure, smooth transitions, detailed explanations.
  """.trimIndent(),
  constants = """
      Consistent: proper punctuation, complete sentences, logical flow.
      Maintains: personal voice markers but in professional context.
  """.trimIndent(),
  voiceMarkers = """
      Professional but personal, uses "I think" over "I believe", 
      maintains contractions but reduces slang, structured but not stiff.
  """.trimIndent(),
  antiPatterns = """
      Avoids: overly casual slang, text-speak, but also avoids corporate jargon,
      legalese, or unnecessarily complex language.
  """.trimIndent()
)
```

### Confidence-Based Blending Logic

```kotlin
fun buildEditingPrompt(selectedTone: String, formalityLevel: Int, userText: String): String {
  val toneDNA = getToneDNA(selectedTone)
  val formalityDNA = getFormalityBandDNA(formalityLevel)
  return when {
      toneDNA.confidence >= 0.8 -> {
          // High tone confidence: Use tone patterns with formality guidance
          """
          TONE PATTERNS (${toneDNA.tone}, confidence ${toneDNA.confidence}):
          ${toneDNA.formalityShifts}
          ${toneDNA.constants}
          ${toneDNA.voiceMarkers}
          TARGET FORMALITY: ${formalityLevel}%
          NEVER: ${toneDNA.antiPatterns}
          Edit: $userText
          """.trimIndent()
      }
      formalityDNA?.confidence ?: 0f >= 0.6 -> {
          // Low tone confidence: Use formality band patterns with tone intent
          """
          FORMALITY BAND PATTERNS (${formalityDNA!!.tone}, confidence ${formalityDNA.confidence}):
          ${formalityDNA.formalityShifts}
          ${formalityDNA.constants}
          TONE INTENT: ${selectedTone}
          Apply ${selectedTone} intent using the above formality band patterns.
          Edit: $userText
          """.trimIndent()
      }
      else -> {
          // Low confidence: Use templates with minimal personalization
          buildTemplatePrompt(selectedTone, formalityLevel, userText)
      }
  }
}
fun getFormalityBandDNA(formalityLevel: Int): VoiceDNA? {
  return when(formalityLevel) {
      in 0..20 -> lowFormalityBandDNA
      in 21..40 -> lowMidFormalityBandDNA  
      in 41..60 -> midFormalityBandDNA
      in 61..80 -> midHighFormalityBandDNA
      in 81..100 -> highFormalityBandDNA
      else -> null
  }
}
```

### Processing Pipeline

1. **Transcription**: Raw voice → text via Whisper
2. **Tone Application**: Apply selected tone's communication intent
3. **DNA Injection**: Preserve user's Voice DNA constants and markers
4. **Polish Adjustment**: Apply formality level based on slider position
5. **Final Output**: Polished text maintaining authentic voice

### Edge Case Handling

- **Tone Mismatch**: If input doesn't match selected tone, gently guide toward tone intent
- **Over-polishing**: Prevent loss of voice authenticity at high polish levels
- **Under-polishing**: Ensure minimum clarity even at lowest polish settings

### Future Enhancements
1. **Dynamic DNA Updates**: Continuous learning from user corrections
2. **Context Awareness**: Meeting vs text message adaptations
3. **Team DNA**: Shared voice patterns for consistent team communication
4. **Multi-Language Support**: VoiceDNA patterns for different languages
5. **Voice Coaching**: Suggestions for expanding communication range

---

## Quick Reference Matrix

| User Intent               | Recommended Tone      | Polish Setting    | Formality Result | Example Output                                                                               |
| ------------------------- | --------------------- | ----------------- | ---------------- | -------------------------------------------------------------------------------------------- |
| Team chat about lunch     | Casual                | 20-40% (simple)   | 15-20%           | "wanna grab lunch at 12?"                                                                    |
| Bug report to colleague   | Neutral/Informative   | 40-60% (clear)    | 35-45%           | "Found a bug in the login flow - users can't reset passwords"                                |
| Client project update     | Action/Neutral        | 60-80% (detailed) | 50-60%           | "Please find the updated timeline attached. We're on track for Friday's delivery."           |
| Apologizing for delay     | Apologetic            | 50-70% (frank)    | 45-55%           | "I apologize for the delayed response - I've been in back-to-back meetings"                  |
| Praising team achievement | Appreciative          | 40-60% (clear)    | 35-50%           | "Great job on the presentation! Your insights really impressed the client."                  |
| Proposing new strategy    | Persuasive/Thoughtful | 70-90% (direct)   | 55-70%           | "I propose we pivot our approach to focus on mobile-first development, which aligns with..." |
| Official announcement     | Formal                | 80-100% (diplomatic) | 70-85%           | "We are pleased to announce the appointment of Dr. Sarah Chen as Chief Technology Officer."  |
| Contract negotiation      | Formal/Persuasive     | 85-100% (analytical) | 75-85%           | "The parties hereby agree to the following terms and conditions, subject to..."              |