package de.pse.oys.data.api

import de.pse.oys.data.facade.UnitRatings
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable
import kotlin.time.Duration
import kotlin.uuid.Uuid

object Routes {
    object Auth {
        @Serializable
        data class RefreshToken(val refreshToken: String)

        @Serializable
        data class AccessToken(val accessToken: String)
    }

    @Serializable
    data class Unit(
        val id: Uuid,
        val actualDuration: Int? = null,
        val newTime: LocalDateTime? = null,
        val ratings: UnitRatings? = null
    )

    @Serializable
    data class Id(val id: Uuid)
}