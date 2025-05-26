package com.editecho.prompt

/**
 * Data class representing the core components of a person's voice DNA
 */
data class VoiceDNA(
    val formalityShifts: String,
    val polishPatterns: String,
    val constants: String,
    val voiceMarkers: String
)

/**
 * Data class for voice settings with formality and polish levels
 */
data class VoiceSettings(
    val formality: Int, // 0-100 scale
    val polish: Int     // 0-100 scale
)

/**
 * Hardcoded Voice DNA for Iain
 */
object IainVoiceDNA {
    val voiceDNA = VoiceDNA(
        formalityShifts = """In casual contexts, drops capitalization entirely ('yes.' 'just tried calling'), uses 'prob' for probably, addresses with 'honey', and omits question marks ('Anything else you need'). Mid-formality shows 'Hey [Name],' greetings and mixed sentence completeness. In formal messages, uses 'Hi [Name].' with period after name, maintains proper capitalization, includes context before requests ('From our call the other week...'), and signs off with 'Thanks,\nIain'. Greeting hierarchy: no greeting for quick responses, 'Hey' for familiar colleagues, 'Hi' for external/formal contacts.""",
        
        polishPatterns = """Rushed messages show all lowercase ('just tried calling them but no answer'), minimal punctuation, and fragment-based structure. Polished messages demonstrate multi-paragraph organization, context provided before requests ('We originally did it to sponsor Blake...'), numbered lists for multiple items, and clear topic separation with line breaks. Polish indicated by explanation depth and structure, not length—even short polished messages have proper breaks ('All good.\nLet's do the following week.'). Decisions appear first, explanations follow.""",
        
        constants = """Across all contexts: Em-dashes with spaces around them (' – ') in 5/16 examples. 'All good' as acknowledgment in 3/16 examples. Line breaks between distinct thoughts/topics in 14/16 examples. 'We've got' instead of 'We have' for resources in 3/16. Short acknowledgments followed by period ('yes.', 'All good.'). States action/decision first, then context. These patterns persist regardless of formality or polish level.""",
        
        voiceMarkers = """'All good' opens 3 responses as primary acknowledgment. Questions without question marks in casual contexts ('Anything else you need', 'Can we please make all driving jobs visa support standard'). 'ish' suffix for approximate times ('4:30ish'). 'prob' abbreviation in casual contexts. 'Sorry' positioned at sentence end ('We won't be able to reschedule sorry', 'sorry for the delay'). Em-dash with spaces for parenthetical information. 'We've got' construction for describing available resources. Distinctive double space after periods (inconsistent but frequent). Brief, punchy sentence structure with periods as thought separators rather than commas."""
    )
} 