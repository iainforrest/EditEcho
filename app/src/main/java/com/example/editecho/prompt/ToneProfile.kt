// src/main/java/com/example/editecho/prompt/ToneProfile.kt
package com.example.editecho.prompt

/**
 * Intent–based tone categories.
 */
enum class ToneProfile(val displayName: String, val systemLabel: String) {
    QUICK("Quick", "Quick Message"),
    FRIENDLY("Friendly", "Friendly Reply"),
    POLISHED("Polished", "Clear and Polished");

    companion object {
        fun fromDisplayName(name: String) =
            values().firstOrNull { it.displayName == name } ?: FRIENDLY
    }
}
