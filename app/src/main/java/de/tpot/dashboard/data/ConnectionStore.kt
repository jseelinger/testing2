package de.tpot.dashboard.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "tpot_settings")

/**
 * Persists the T-Pot connection settings using DataStore.
 */
class ConnectionStore(private val context: Context) {

    companion object {
        private val SERVER_URL = stringPreferencesKey("server_url")
        private val USERNAME = stringPreferencesKey("username")
        private val PASSWORD = stringPreferencesKey("password")
        private val ACCEPT_SELF_SIGNED = booleanPreferencesKey("accept_self_signed")
    }

    val connection: Flow<TpotConnection> = context.dataStore.data.map { prefs ->
        TpotConnection(
            serverUrl = prefs[SERVER_URL] ?: "",
            username = prefs[USERNAME] ?: "",
            password = prefs[PASSWORD] ?: "",
            acceptSelfSigned = prefs[ACCEPT_SELF_SIGNED] ?: true
        )
    }

    suspend fun saveConnection(conn: TpotConnection) {
        context.dataStore.edit { prefs ->
            prefs[SERVER_URL] = conn.serverUrl
            prefs[USERNAME] = conn.username
            prefs[PASSWORD] = conn.password
            prefs[ACCEPT_SELF_SIGNED] = conn.acceptSelfSigned
        }
    }

    suspend fun clearConnection() {
        context.dataStore.edit { it.clear() }
    }
}
