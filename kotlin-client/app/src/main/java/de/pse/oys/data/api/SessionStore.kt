package de.pse.oys.data.api

import kotlinx.serialization.Serializable

interface SessionStore {
    suspend fun getSession(): Session?

    suspend fun setSession(session: Session?)
}

@Serializable
data class Session(val accessToken: String, val refreshToken: String)