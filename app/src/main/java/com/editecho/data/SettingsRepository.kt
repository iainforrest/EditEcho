package com.editecho.data

import android.content.Context
import com.editecho.prompt.ToneProfile
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "settings")

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val selectedToneKey = stringPreferencesKey("selected_tone")
    private val polishLevelKey = intPreferencesKey("polish_level")

    val selectedTone: Flow<String> = context.dataStore.data.map { preferences ->
        val savedTone = preferences[selectedToneKey] ?: "Neutral"
        // Validate tone exists in ToneProfile enum, fallback to "Neutral" if invalid
        if (ToneProfile.fromName(savedTone) != null) savedTone else "Neutral"
    }

    val polishLevel: Flow<Int> = context.dataStore.data.map { preferences ->
        val savedLevel = preferences[polishLevelKey] ?: 50
        // Clamp polish level to 0-100 range
        savedLevel.coerceIn(0, 100)
    }

    suspend fun setSelectedTone(tone: String) {
        // Validate tone exists in ToneProfile enum, fallback to "Neutral" if invalid
        val validTone = if (ToneProfile.fromName(tone) != null) tone else "Neutral"
        context.dataStore.edit { preferences ->
            preferences[selectedToneKey] = validTone
        }
    }

    suspend fun setPolishLevel(level: Int) {
        // Clamp polish level to 0-100 range
        val validLevel = level.coerceIn(0, 100)
        context.dataStore.edit { preferences ->
            preferences[polishLevelKey] = validLevel
        }
    }
} 