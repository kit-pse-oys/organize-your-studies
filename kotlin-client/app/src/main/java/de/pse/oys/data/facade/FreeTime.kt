package de.pse.oys.data.facade

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

typealias FreeTime = Identified<FreeTimeData>

data class FreeTimeData(
    val title: String,
    val date: LocalDate,
    val start: LocalTime,
    val end: LocalTime,
    val weekly: Boolean
)