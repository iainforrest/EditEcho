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
    val formality: Int, // 1-5 scale
    val polish: Int     // 1-5 scale
)

/**
 * Hardcoded Voice DNA for Iain
 */
object IainVoiceDNA {
    val voiceDNA = VoiceDNA(
        formalityShifts = """When casual, Iain drops capital letters entirely ("yes. talked to franki about this last week"), uses minimal punctuation, and often writes in fragments that assume shared context. His casual messages to family are remarkably brief - "Sorry honey. Kids should be tired anyway" - with no greetings or sign-offs. As formality increases, he adds proper capitalization, complete sentences, and contextual information. In professional messages, he includes greetings ("Hi Graham"), provides detailed background ("I'm at Totaranui Beach this week. Reception goes from 1 bar to 0"), and adds sign-offs with his name. He shifts from "prob" to "probably," "info" to "information," and starts using more formal conjunctions. Notably, even in formal messages, he maintains some casual markers - "That's the bit we don't quite get" rather than "That is the aspect we do not understand."""",
        
        polishPatterns = """In rushed messages, Iain writes stream-of-consciousness style with minimal punctuation and no paragraph breaks - everything runs together like "just tried calling them but no answer. if they call you back. we only have 4 on tomorrow." When he has more time, he organizes thoughts into distinct paragraphs, uses proper sentence boundaries, and structures information logically. Quick messages often have lowercase starts and inconsistent punctuation, while edited messages show careful formatting with numbered lists ("1. McConnell Executive – as per last build"), proper em-dashes, and strategic line breaks. Rushed messages convey one thought per sentence fragment, while polished ones combine related ideas with conjunctions and subordinate clauses. The more polished the message, the more likely he is to provide reasoning ("which is why I was hoping to talk to someone today") rather than just stating facts.""",
        
        constants = """Regardless of context, Iain always uses line breaks to separate distinct thoughts - this is his signature formatting style across all messages. He consistently uses "sorry" as both apology and softener, deploys "all good" as his universal acknowledgment phrase, and structures longer messages with clear topic progression. His core personality shows through short, declarative sentences even in formal contexts. He always provides specific details when discussing logistics - times ("by 6:15 prob"), dates ("21-26 is locked in"), and quantities ("We've got 8 coaches suitable"). He never uses exclamation marks except in rare moments of emphasis. His thanks are always simple - "Thanks" or "Thank you" - never effusive. He consistently uses "we" when discussing team or family matters, showing his collaborative mindset. Most distinctively, he always writes with absolute clarity about what he needs or what will happen next.""",
        
        voiceMarkers = """Iain's Kiwi voice shines through in phrases like "you reckon" (even in semi-formal contexts), "all good" as his default positive response, and "keen" for enthusiasm. He uses "mate" in casual contexts and distinctive constructions like "what you're like behind the wheel" instead of "how you drive." His pragmatic Kiwi directness appears in phrases like "Ah crap, yeah, I need to get on that" and "Sorry if this doesn't work for you" - polite but unapologetic. He has a unique way of combining formal structure with casual interjections: "Also — please send through options" uses an em-dash where others might use a colon. His favorite transition is "Also" for adding points. He consistently uses "bit" as a softener ("a bit behind," "bit differently"). Location names are dropped casually assuming local knowledge ("Jack's," "the hall," "11th Ave"). His time expressions are distinctly casual - "end of next week," "later this week," "the following week" - avoiding specific dates unless critical. The phrase "get on that" appears multiple times as his way of acknowledging tasks."""
    )
} 