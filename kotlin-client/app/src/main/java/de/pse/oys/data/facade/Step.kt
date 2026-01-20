package de.pse.oys.data.facade

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

typealias Step = Identified<StepData>

data class StepData(val task: Task, val data: LocalDate, val start: LocalTime, val end: LocalTime)