package com.example.riomarappnav

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "theme_preferences")

class ThemePreferenceManager(private val context: Context) {
    private val THEME_KEY = booleanPreferencesKey("dark_mode_enabled")

    val isDarkModeEnabled: Flow<Boolean?> = context.dataStore.data
        .map { preferences -> preferences[THEME_KEY] }

    suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[THEME_KEY] = enabled
        }
    }
}
