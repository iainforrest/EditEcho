package com.example.editecho.prompt

object ToneProfile {

    val smsExamples = listOf(
        "Sweet as, see ya soon.",
        "Nah all good mate.",
        "Shot for that, legend.",
        "Yo, running 10 late sorry."
    )

    val emailExamples = listOf(
        "Hey team, just a quick heads up...",
        "Let me know if that works for you.",
        "Cheers for sorting that out."
    )

    val proExamples = listOf(
        "Kia ora, appreciate the update.",
        "Happy to chat further if needed.",
        "Let me know if you'd like me to draft something up."
    )

    val styleRules = """
        - Keep it concise.
        - Use Kiwi slang when appropriate.
        - Always friendly and casual unless told otherwise.
        - Avoid corporate jargon.
        - Use contractions often (I'm, it's, can't).
    """.trimIndent()
}
