package de.pse.oys.data.facade

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.serialization.Serializable

typealias FreeTime = Identified<FreeTimeData>

@Serializable
data class FreeTimeData(
    val title: String,
    val date: LocalDate,
    val start: LocalTime,
    val end: LocalTime,
    val weekly: Boolean
)