package com.editecho.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
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
    private val formalityKey = intPreferencesKey("formality")
    private val polishKey = intPreferencesKey("polish")

    val formality: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[formalityKey] ?: 50
    }

    val polish: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[polishKey] ?: 50
    }

    suspend fun setFormality(value: Int) {
        context.dataStore.edit { preferences ->
            preferences[formalityKey] = value
        }
    }

    suspend fun setPolish(value: Int) {
        context.dataStore.edit { preferences ->
            preferences[polishKey] = value
        }
    }
} 