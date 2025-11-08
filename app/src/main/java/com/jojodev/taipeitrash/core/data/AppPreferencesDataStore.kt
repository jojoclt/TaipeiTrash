package com.jojodev.taipeitrash.core.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.jojodev.taipeitrash.core.model.City
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "app_preferences")

@Singleton
class AppPreferencesDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object PreferencesKeys {
        val IS_DATA_LOADED = booleanPreferencesKey("is_data_loaded")
        val SELECTED_CITY = stringPreferencesKey("selected_city")
        val IS_FIRST_LAUNCH = booleanPreferencesKey("is_first_launch")
    }

    val isDataLoaded: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.IS_DATA_LOADED] ?: false
        }

    val selectedCity: Flow<City> = context.dataStore.data
        .map { preferences ->
            val cityString = preferences[PreferencesKeys.SELECTED_CITY] ?: City.TAIPEI.name
            City.fromString(cityString)
        }

    val isFirstLaunch: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.IS_FIRST_LAUNCH] ?: true
        }

    suspend fun setDataLoaded(isLoaded: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_DATA_LOADED] = isLoaded
        }
    }

    suspend fun setSelectedCity(city: City) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SELECTED_CITY] = city.name
        }
    }

    suspend fun setFirstLaunchComplete() {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_FIRST_LAUNCH] = false
        }
    }

    suspend fun clearPreferences() {
        context.dataStore.edit { it.clear() }
    }
}

