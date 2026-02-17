package de.pse.oys.data.facade

import kotlinx.serialization.Serializable

@Serializable
data class UnitRatings(
    val goalCompletion: Rating,
    val perceivedDuration: Rating,
    val concentration: Rating,
)

@Serializable
enum class Rating {
    LOWEST,
    LOW,
    MEDIUM,
    HIGH,
    HIGHEST
}
