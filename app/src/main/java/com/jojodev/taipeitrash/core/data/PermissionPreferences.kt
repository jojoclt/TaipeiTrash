package com.jojodev.taipeitrash.core.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "permission_preferences")

class PermissionPreferences(private val context: Context) {

    companion object {
        private val WAS_PERMISSION_DENIED_KEY = booleanPreferencesKey("was_permission_denied")
    }

    val wasPermissionDenied: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[WAS_PERMISSION_DENIED_KEY] ?: false
        }

    suspend fun setPermissionDenied(denied: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[WAS_PERMISSION_DENIED_KEY] = denied
        }
    }

    suspend fun clearPermissionDenied() {
        context.dataStore.edit { preferences ->
            preferences.remove(WAS_PERMISSION_DENIED_KEY)
        }
    }
}

