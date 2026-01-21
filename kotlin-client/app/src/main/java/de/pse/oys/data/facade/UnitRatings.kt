package de.pse.oys.data.facade

import kotlinx.serialization.Serializable

@Serializable
data class UnitRatings(
    val goalCompletion: Rating,
    val duration: Rating,
    val motivation: Rating,
)

@Serializable
enum class Rating {
    LOWEST,
    LOW,
    MEDIUM,
    HIGH,
    HIGHEST
}
