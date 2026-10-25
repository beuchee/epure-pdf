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

    private companion object {
        val KEEP_LIST = booleanPreferencesKey("keep_list")
    }
}
