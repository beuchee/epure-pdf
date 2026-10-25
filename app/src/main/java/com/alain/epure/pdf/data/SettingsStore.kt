package com.alain.epure.pdf.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore by preferencesDataStore(name = "settings")

// User settings, kept in the app's private storage (SPEC F-15.1).
class SettingsStore(private val context: Context) {
    // Off by default: only the last document is kept (DEC-0024).
    val keepList: Flow<Boolean> = context.settingsDataStore.data
        .catch { emit(emptyPreferences()) }
        .map { it[KEEP_LIST] ?: false }

    suspend fun setKeepList(enabled: Boolean) {
        context.settingsDataStore.edit { it[KEEP_LIST] = enabled }
    }

    // Off by default. Applies to every document (SPEC F-8.3, DEC-0029).
    val nightMode: Flow<Boolean> = context.settingsDataStore.data
        .catch { emit(emptyPreferences()) }
        .map { it[NIGHT_MODE] ?: false }

    suspend fun setNightMode(enabled: Boolean) {
        context.settingsDataStore.edit { it[NIGHT_MODE] = enabled }
    }

    // On by default (SPEC F-9.2, decided by the project owner). Keeps the screen on while reading only.
    val keepScreenOn: Flow<Boolean> = context.settingsDataStore.data
        .catch { emit(emptyPreferences()) }
        .map { it[KEEP_SCREEN_ON] ?: true }

    suspend fun setKeepScreenOn(enabled: Boolean) {
        context.settingsDataStore.edit { it[KEEP_SCREEN_ON] = enabled }
    }

    private companion object {
        val KEEP_LIST = booleanPreferencesKey("keep_list")
        val NIGHT_MODE = booleanPreferencesKey("night_mode")
        val KEEP_SCREEN_ON = booleanPreferencesKey("keep_screen_on")
    }
}
