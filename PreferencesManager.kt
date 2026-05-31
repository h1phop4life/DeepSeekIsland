package com.dynamicisland.app

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class PreferencesManager(private val context: Context) {

    companion object {
        val POSITION_HORIZONTAL = stringPreferencesKey("position_horizontal") // "left", "center", "right"
        val OFFSET_X = intPreferencesKey("offset_x")
        val OFFSET_Y = intPreferencesKey("offset_y")
        val SMALL_DURATION = intPreferencesKey("small_duration") // saniye
        val EXPANDED_DURATION = intPreferencesKey("expanded_duration") // saniye

        // Renk anahtarları
        val TEXT_COLOR = stringPreferencesKey("text_color")
        val BUTTON_COLOR = stringPreferencesKey("button_color")
        val BACKGROUND_COLOR = stringPreferencesKey("background_color")

        // Dinamik renk toggle'ları
        val TEXT_DYNAMIC = booleanPreferencesKey("text_dynamic")
        val BUTTON_DYNAMIC = booleanPreferencesKey("button_dynamic")
        val BACKGROUND_DYNAMIC = booleanPreferencesKey("background_dynamic")
    }

    // Akışlar
    val horizontalPosition: Flow<String> = context.dataStore.data.map { it[POSITION_HORIZONTAL] ?: "left" }
    val offsetX: Flow<Int> = context.dataStore.data.map { it[OFFSET_X] ?: 0 }
    val offsetY: Flow<Int> = context.dataStore.data.map { it[OFFSET_Y] ?: 0 }
    val smallDurationSec: Flow<Int> = context.dataStore.data.map { it[SMALL_DURATION] ?: 5 }
    val expandedDurationSec: Flow<Int> = context.dataStore.data.map { it[EXPANDED_DURATION] ?: 10 }
    val textColor: Flow<String> = context.dataStore.data.map { it[TEXT_COLOR] ?: "#FFFFFF" }
    val buttonColor: Flow<String> = context.dataStore.data.map { it[BUTTON_COLOR] ?: "#FFFFFF" }
    val backgroundColor: Flow<String> = context.dataStore.data.map { it[BACKGROUND_COLOR] ?: "#000000" }
    val textDynamic: Flow<Boolean> = context.dataStore.data.map { it[TEXT_DYNAMIC] ?: false }
    val buttonDynamic: Flow<Boolean> = context.dataStore.data.map { it[BUTTON_DYNAMIC] ?: false }
    val backgroundDynamic: Flow<Boolean> = context.dataStore.data.map { it[BACKGROUND_DYNAMIC] ?: false }

    suspend fun setHorizontalPosition(value: String) {
        context.dataStore.edit { it[POSITION_HORIZONTAL] = value }
    }
    suspend fun setOffsetX(value: Int) {
        context.dataStore.edit { it[OFFSET_X] = value }
    }
    suspend fun setOffsetY(value: Int) {
        context.dataStore.edit { it[OFFSET_Y] = value }
    }
    suspend fun setSmallDuration(value: Int) {
        context.dataStore.edit { it[SMALL_DURATION] = value }
    }
    suspend fun setExpandedDuration(value: Int) {
        context.dataStore.edit { it[EXPANDED_DURATION] = value }
    }
    suspend fun setTextColor(value: String) {
        context.dataStore.edit { it[TEXT_COLOR] = value }
    }
    suspend fun setButtonColor(value: String) {
        context.dataStore.edit { it[BUTTON_COLOR] = value }
    }
    suspend fun setBackgroundColor(value: String) {
        context.dataStore.edit { it[BACKGROUND_COLOR] = value }
    }
    suspend fun setTextDynamic(value: Boolean) {
        context.dataStore.edit { it[TEXT_DYNAMIC] = value }
    }
    suspend fun setButtonDynamic(value: Boolean) {
        context.dataStore.edit { it[BUTTON_DYNAMIC] = value }
    }
    suspend fun setBackgroundDynamic(value: Boolean) {
        context.dataStore.edit { it[BACKGROUND_DYNAMIC] = value }
    }
}