package de.pse.oys.data.facade

data class UnitRatings(
    val goalCompletion: Rating,
    val duration: Rating,
    val motivation: Rating,
)

enum class Rating {
    LOWEST,
    LOW,
    MEDIUM,
    HIGH,
    HIGHEST
}
