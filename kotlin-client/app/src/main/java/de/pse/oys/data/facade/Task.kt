package de.pse.oys.data.facade

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime

typealias Task = Identified<TaskData>
typealias ExamTask = Identified<ExamTaskData>
typealias SubmissionTask = Identified<SubmissionTaskData>
typealias OtherTask = Identified<OtherTaskData>

sealed class TaskData {
    abstract val title: String
    abstract val module: Module
    abstract val weeklyTimeLoad: Int
    abstract val sendNotification: Boolean
}

data class ExamTaskData(
    override val title: String,
    override val module: Module,
    override val weeklyTimeLoad: Int,
    override val sendNotification: Boolean,
    val examDate: LocalDate,
) : TaskData()

data class SubmissionTaskData(
    override val title: String,
    override val module: Module,
    override val weeklyTimeLoad: Int,
    override val sendNotification: Boolean,
    val firstDate: LocalDateTime,
    val cycle: Int,
) : TaskData()

data class OtherTaskData(
    override val title: String,
    override val module: Module,
    override val weeklyTimeLoad: Int,
    override val sendNotification: Boolean,
    val start: LocalDate,
    val end: LocalDate,
): TaskData()