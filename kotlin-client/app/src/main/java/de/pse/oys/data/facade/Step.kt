package de.pse.oys.data.facade

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.serialization.Serializable

typealias Step = Identified<StepData>

@Serializable
data class StepData(val task: Task, val date: LocalDate, val start: LocalTime, val end: LocalTime)