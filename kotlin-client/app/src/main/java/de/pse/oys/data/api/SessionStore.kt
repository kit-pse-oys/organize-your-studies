package de.pse.oys.data.api

interface SessionStore {
    suspend fun getSession(): Session?

    suspend fun setSession(session: Session?)
}

data class Session(val accessToken: String, val refreshToken: String)