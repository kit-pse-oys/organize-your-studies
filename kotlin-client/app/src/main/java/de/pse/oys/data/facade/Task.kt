package de.pse.oys.data.facade

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

typealias Task = Identified<TaskData>
typealias ExamTask = Identified<ExamTaskData>
typealias SubmissionTask = Identified<SubmissionTaskData>
typealias OtherTask = Identified<OtherTaskData>

@Serializable
sealed class TaskData {
    abstract val title: String
    abstract val module: Module
    abstract val weeklyTimeLoad: Int
}

@Serializable
@SerialName("exam")
data class ExamTaskData(
    override val title: String,
    override val module: Module,
    override val weeklyTimeLoad: Int,
    val examDate: LocalDate,
) : TaskData()

@Serializable
@SerialName("submission")
data class SubmissionTaskData(
    override val title: String,
    override val module: Module,
    override val weeklyTimeLoad: Int,
    val firstDate: LocalDateTime,
    val cycle: Int,
) : TaskData()

@Serializable
@SerialName("other")
data class OtherTaskData(
    override val title: String,
    override val module: Module,
    override val weeklyTimeLoad: Int,
    val start: LocalDate,
    val end: LocalDate,
): TaskData()