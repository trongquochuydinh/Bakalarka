package cz.zcu.kiv.dinh.ui.theme

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "settings")

class ThemeManager(context: Context) {
    private val dataStore = context.dataStore

    companion object {
        private val THEME_KEY = booleanPreferencesKey("dark_theme")
    }

    val getTheme: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[THEME_KEY] ?: false // Default to light mode
    }

    suspend fun saveTheme(isDarkTheme: Boolean) {
        dataStore.edit { preferences ->
            preferences[THEME_KEY] = isDarkTheme
        }
    }
}
