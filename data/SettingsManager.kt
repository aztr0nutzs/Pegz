// data/SettingsManager.kt (FINAL MERGED VERSION)

package com.neon.peggame.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "game_settings")

@Singleton
class SettingsManager @Inject constructor(@ApplicationContext context: Context) {
    private val dataStore = context.dataStore

    companion object {
        val VIBRATION_ENABLED = booleanPreferencesKey("vibration_enabled")
        val MUSIC_ENABLED = booleanPreferencesKey("music_enabled")
        val IS_PREMIUM_USER = booleanPreferencesKey("is_premium_user")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        
        // PEGZ Bible Keys
        val MASTER_VOLUME = floatPreferencesKey("master_volume")
        val ACTIVE_PEGZ_SET = stringPreferencesKey("active_pegz_set")

        // Developer / QA Key
        val DEBUG_COORDS = booleanPreferencesKey("debug_coords")
    }

    // --- Read operations ---

    val vibrationSetting: Flow<Boolean> = dataStore.data
        .map { it[VIBRATION_ENABLED] ?: true }

    val musicSetting: Flow<Boolean> = dataStore.data
        .map { it[MUSIC_ENABLED] ?: true }

    val isPremiumUser: Flow<Boolean> = dataStore.data
        .map { it[IS_PREMIUM_USER] ?: false }

    val onboardingCompleted: Flow<Boolean> = dataStore.data
        .map { it[ONBOARDING_COMPLETED] ?: false }

    val masterVolume: Flow<Float> = dataStore.data
        .map { it[MASTER_VOLUME] ?: 0.8f }

    val activeSet: Flow<String> = dataStore.data
        .map { it[ACTIVE_PEGZ_SET] ?: "BIO_LAB" }

    val debugCoordsSetting: Flow<Boolean> = dataStore.data
        .map { it[DEBUG_COORDS] ?: false }

    // --- Write operations ---
    
    suspend fun setVibration(enabled: Boolean) {
        dataStore.edit { it[VIBRATION_ENABLED] = enabled }
    }

    suspend fun setMusic(enabled: Boolean) {
        dataStore.edit { it[MUSIC_ENABLED] = enabled }
    }
    
    suspend fun setPremiumStatus(isPremium: Boolean) {
        dataStore.edit { it[IS_PREMIUM_USER] = isPremium }
    }
    
    suspend fun completeOnboarding() {
        dataStore.edit { it[ONBOARDING_COMPLETED] = true }
    }

    suspend fun setVolume(volume: Float) {
        dataStore.edit { it[MASTER_VOLUME] = volume }
    }

    suspend fun setActiveSet(setName: String) {
        dataStore.edit { it[ACTIVE_PEGZ_SET] = setName }
    }

    suspend fun setDebugCoords(enabled: Boolean) {
        dataStore.edit { it[DEBUG_COORDS] = enabled }
    }
}
