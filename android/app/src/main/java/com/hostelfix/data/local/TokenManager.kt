package com.hostelfix.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "hostelfix_prefs")

class TokenManager(private val context: Context) {

    companion object {
        private val KEY_TOKEN = stringPreferencesKey("jwt_token")
        private val KEY_USER_ID = stringPreferencesKey("user_id")
        private val KEY_USER_NAME = stringPreferencesKey("user_name")
        private val KEY_USER_EMAIL = stringPreferencesKey("user_email")
        private val KEY_USER_PHONE = stringPreferencesKey("user_phone")
        private val KEY_USER_BLOCK = stringPreferencesKey("user_block")
        private val KEY_USER_ROOM = stringPreferencesKey("user_room")
        private val KEY_IS_VERIFIED = booleanPreferencesKey("is_location_verified")
        private val KEY_FCM_TOKEN = stringPreferencesKey("fcm_token")
    }

    val tokenFlow: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[KEY_TOKEN]
    }

    val isLocationVerifiedFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_IS_VERIFIED] ?: false
    }

    val userNameFlow: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[KEY_USER_NAME]
    }

    val userBlockFlow: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[KEY_USER_BLOCK]
    }

    val userRoomFlow: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[KEY_USER_ROOM]
    }

    suspend fun saveAuthData(
        token: String,
        id: String,
        name: String,
        email: String,
        phone: String,
        block: String,
        room: String,
        isVerified: Boolean
    ) {
        context.dataStore.edit { prefs ->
            prefs[KEY_TOKEN] = token
            prefs[KEY_USER_ID] = id
            prefs[KEY_USER_NAME] = name
            prefs[KEY_USER_EMAIL] = email
            prefs[KEY_USER_PHONE] = phone
            prefs[KEY_USER_BLOCK] = block
            prefs[KEY_USER_ROOM] = room
            prefs[KEY_IS_VERIFIED] = isVerified
        }
    }

    suspend fun setLocationVerified(verified: Boolean, block: String? = null) {
        context.dataStore.edit { prefs ->
            prefs[KEY_IS_VERIFIED] = verified
            if (block != null) {
                prefs[KEY_USER_BLOCK] = block
            }
        }
    }

    suspend fun getToken(): String? {
        val prefs = context.dataStore.data.first()
        return prefs[KEY_TOKEN]
    }

    suspend fun saveFcmToken(fcmToken: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_FCM_TOKEN] = fcmToken
        }
    }

    suspend fun getFcmToken(): String? {
        return context.dataStore.data.first()[KEY_FCM_TOKEN]
    }

    suspend fun clearSession() {
        context.dataStore.edit { prefs ->
            prefs.remove(KEY_TOKEN)
            prefs.remove(KEY_USER_ID)
            prefs.remove(KEY_USER_NAME)
            prefs.remove(KEY_USER_EMAIL)
            prefs.remove(KEY_USER_PHONE)
            prefs.remove(KEY_USER_BLOCK)
            prefs.remove(KEY_USER_ROOM)
            prefs.remove(KEY_IS_VERIFIED)
        }
    }
}
