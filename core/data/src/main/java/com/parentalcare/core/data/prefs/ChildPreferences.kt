package com.parentalcare.core.data.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "child_prefs")

@Singleton
class ChildPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val DEVICE_ID_KEY = stringPreferencesKey("device_id")
    private val FAMILY_ID_KEY = stringPreferencesKey("family_id")
    private val PARENT_PUBLIC_KEY = stringPreferencesKey("parent_public_key")

    val deviceId: Flow<String?> = context.dataStore.data.map { it[DEVICE_ID_KEY] }
    val familyId: Flow<String?> = context.dataStore.data.map { it[FAMILY_ID_KEY] }
    val parentPublicKey: Flow<String?> = context.dataStore.data.map { it[PARENT_PUBLIC_KEY] }

    suspend fun savePairingData(deviceId: String, familyId: String, parentPublicKey: String) {
        context.dataStore.edit { prefs ->
            prefs[DEVICE_ID_KEY] = deviceId
            prefs[FAMILY_ID_KEY] = familyId
            prefs[PARENT_PUBLIC_KEY] = parentPublicKey
        }
    }

    suspend fun clear() {
        context.dataStore.edit { it.clear() }
    }
}
