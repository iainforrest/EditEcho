# PRD: Universal DNA Integration for Prompt Engine

## 1. Introduction/Overview

This document outlines the requirements for refactoring the `VoicePromptBuilder` and related components to incorporate a `universalDNA` layer. The `universalDNA` defines a set of core voice patterns that should be applied consistently across all edits, regardless of the selected tone or formality level. This change aims to improve the consistency and quality of text edits, reduce redundancy in tone-specific DNA, and provide a reliable baseline for the voice engine, especially for formality levels with sparse data.

## 2. Goals

-   Integrate `universalDNA` as a foundational layer in the prompt generation process.
-   Ensure universal patterns are applied to all generated prompts (high confidence, low confidence, and fallback).
-   Establish a clear hierarchy where tone-specific DNA can supplement or override universal DNA.
-   Improve the overall consistency of the edited output by ensuring baseline rules are always active.
-   Reduce duplication across different `toneSpecificDNA` definitions.

## 3. User Stories

-   **As a system,** I want to load the `universalDNA` from the `voice_dna_patterns.json` file so its patterns can be used in every prompt.
-   **As a system,** I want the `VoicePromptBuilder` to inject `universalDNA` patterns into every prompt it generates, creating a consistent foundation for editing.
-   **As an LLM,** I want to receive clear instructions that specify both universal and tone-specific rules, with guidance that tone-specific rules take precedence in case of direct conflict.

## 4. Functional Requirements

1.  **Update Data Models:**
    -   Create a new data class `UniversalDNA` in `VoiceDNA.kt` to represent the structure of the `universalDNA` object in the JSON (`confidence`, `description`, `patterns`, `warnings`).
    -   Update the `VoiceDNACollection` data class in `VoiceDNA.kt` to include a field for the `universalDNA` object.

2.  **Update Data Repository:**
    -   Modify `VoiceDNARepository.kt` to correctly parse and load the `universalDNA` object from `voice_dna_patterns.json` when initializing the `VoiceDNACollection`.

3.  **Refactor Prompt Builder:**
    -   All prompt-building functions in `VoicePromptBuilder.kt` (`buildHighConfidencePrompt`, `buildLowConfidencePrompt`, `buildFallbackPrompt`) must be updated.
    -   A new section, titled **"UNIVERSAL VOICE RULES (Apply to all edits)"**, must be added to every prompt.
    -   This section should list the `patterns` from the `universalDNA` object.
    -   The prompt instructions should guide the LLM to treat these as baseline rules, while giving precedence to the more specific `TONE DNA PATTERNS` or `FORMALITY BAND PATTERNS` that follow.

4.  **Conflict Resolution:**
    -   Precedence will be managed through prompt structure. The `universalDNA` section will appear before the tone-specific or formality-specific sections.
    -   A guiding instruction should be added, for instance: "First, apply the universal rules below. Then, apply the specific tone and formality patterns, which take priority if there is a conflict."

## 5. Non-Goals (Out of Scope)

-   Developing a complex automated system for resolving conflicts between different DNA layers. Precedence will be handled via instruction in the prompt.
-   Modifying the content of the `universalDNA` patterns. This PRD is focused on the implementation of the system.
-   Changing the core logic of how `toneSpecificDNA` and `formalityBandDNA` are selected and used, beyond what is necessary to integrate the universal layer.

## 6. Design Considerations (Optional)

-   The prompt should be structured for maximum clarity to the LLM. The introduction of the universal layer should not make the prompt confusing.
-   Example of the new prompt structure:
    ```
    You are editing a voice transcription...

    CRITICAL RULES:
    ...

    ---
    **UNIVERSAL VOICE RULES (Apply to all edits):**
    - [Universal Pattern 1]
    - [Universal Pattern 2]
    ---

    TONE DNA PATTERNS ([Tone Name]):
    ...
    ```

## 7. Technical Considerations

-   The `Gson` parser in `VoiceDNARepository.kt` needs to be updated to handle the new `universalDNA` field in `VoiceDNACollection`.
-   The changes will touch multiple core files: `VoiceDNA.kt`, `VoiceDNARepository.kt`, and `VoicePromptBuilder.kt`. Care must be taken to ensure the existing logic continues to function correctly.
-   No changes are anticipated for the `ClaudeApi.kt` file, as the refactoring affects the *content* of the prompt, not the API call structure itself.

## 8. Success Metrics

-   The application compiles and the refactored code runs without errors.
-   Unit or integration tests for `VoicePromptBuilder` pass successfully.
-   The generated prompts for all tones (Casual, Informative, etc.) and at all polish levels correctly include the **"UNIVERSAL VOICE RULES"** section.
-   The user will conduct real-world testing to confirm that the output quality has improved and is more consistent.

## 9. Open Questions

-   Should the `warnings` array from `universalDNA` be included in the prompt for the LLM, or are they solely for developer reference? (Initial assumption: For developers only). 