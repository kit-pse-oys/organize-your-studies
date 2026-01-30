package de.pse.oys.data.properties

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import de.pse.oys.data.api.Session
import de.pse.oys.data.api.SessionStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

interface Properties {
    val darkmode: Flow<Darkmode>

    suspend fun setDarkmode(darkmode: Darkmode)
}

private val Context.dataStore by preferencesDataStore("settings")

class DataStoreProperties(private val context: Context) : Properties, SessionStore {
    companion object {
        private val DARKMODE = stringPreferencesKey("darkmode")

        private val HAS_SESSION = booleanPreferencesKey("has_session")
        private val SESSION_ACCESS_TOKEN = stringPreferencesKey("session_access_token")
        private val SESSION_REFRESH_TOKEN = stringPreferencesKey("session_refresh_token")
    }

    override val darkmode: Flow<Darkmode>
        get() = context.dataStore.data.map { preferences ->
            preferences[DARKMODE]?.let { Darkmode.valueOf(it) } ?: Darkmode.SYSTEM
        }

    override suspend fun setDarkmode(darkmode: Darkmode) {
        context.dataStore.edit { preferences ->
            preferences[DARKMODE] = darkmode.name
        }
    }

    val hasSession = context.dataStore.data.map { preferences ->
        preferences[HAS_SESSION] ?: false
    }

    override suspend fun getSession(): Session? {
        val preferences = context.dataStore.data.first()
        return if (preferences[HAS_SESSION] == true) {
            Session(
                preferences[SESSION_ACCESS_TOKEN]
                    ?: throw IllegalStateException("HAS_SESSION is true"),
                preferences[SESSION_REFRESH_TOKEN]
                    ?: throw IllegalStateException("HAS_SESSION is true")
            )
        } else null
    }

    override suspend fun setSession(session: Session?) {
        context.dataStore.edit { preferences ->
            preferences[HAS_SESSION] = session != null
            if (session != null) {
                preferences[SESSION_ACCESS_TOKEN] = session.accessToken
                preferences[SESSION_REFRESH_TOKEN] = session.refreshToken
            } else {
                preferences.remove(SESSION_ACCESS_TOKEN)
                preferences.remove(SESSION_REFRESH_TOKEN)
            }
        }
    }
}