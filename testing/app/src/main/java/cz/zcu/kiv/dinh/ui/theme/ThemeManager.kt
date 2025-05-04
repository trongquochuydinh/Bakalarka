package cz.zcu.kiv.dinh.ui.theme

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Vytvoření privátního DataStore pro ukládání nastavení aplikace
private val Context.dataStore by preferencesDataStore(name = "settings")

/**
 * Správce tématu aplikace využívající Jetpack DataStore.
 * Umožňuje trvale ukládat a číst preferovaný vizuální režim (světlý/tmavý).
 *
 * @param context Kontext aplikace, potřebný pro inicializaci DataStore
 */
class ThemeManager(context: Context) {
    private val dataStore = context.dataStore

    companion object {
        // Klíč preference pro uložení režimu
        private val THEME_KEY = booleanPreferencesKey("dark_theme")
    }

    /**
     * Vrací proud (Flow), který obsahuje aktuální hodnotu režimu (true = dark mode).
     * Pokud není hodnota dosud nastavena, výchozí je světlý režim (false).
     */
    val getTheme: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[THEME_KEY] ?: false // Výchozí hodnota: světlý režim
    }

    /**
     * Uloží preferovaný vizuální režim do DataStore.
     *
     * @param isDarkTheme true pro tmavý režim, false pro světlý
     */
    suspend fun saveTheme(isDarkTheme: Boolean) {
        dataStore.edit { preferences ->
            preferences[THEME_KEY] = isDarkTheme
        }
    }
}