package de.pse.oys.data.api

import de.pse.oys.data.facade.Identified
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator
import kotlin.uuid.Uuid

typealias RemoteStep = Identified<RemoteStepData>

@Serializable
data class RemoteStepData(val task: Uuid, val date: LocalDate, val start: LocalTime, val end: LocalTime)

typealias RemoteTask = Identified<RemoteTaskData>
typealias RemoteExamTask = Identified<RemoteExamTaskData>
typealias RemoteSubmissionTask = Identified<RemoteSubmissionTaskData>
typealias RemoteOtherTask = Identified<RemoteOtherTaskData>

@Serializable
@JsonClassDiscriminator(discriminator = "category")
sealed class RemoteTaskData {
    abstract val title: String
    abstract val module: Uuid
    abstract val weeklyTimeLoad: Int
}

@Serializable
@SerialName("exam")
data class RemoteExamTaskData(
    override val title: String,
    override val module: Uuid,
    override val weeklyTimeLoad: Int,
    val examDate: LocalDate,
) : RemoteTaskData()

@Serializable
@SerialName("submission")
data class RemoteSubmissionTaskData(
    override val title: String,
    override val module: Uuid,
    override val weeklyTimeLoad: Int,
    val firstDate: LocalDateTime,
    val cycle: Int,
) : RemoteTaskData()

@Serializable
@SerialName("other")
data class RemoteOtherTaskData(
    override val title: String,
    override val module: Uuid,
    override val weeklyTimeLoad: Int,
    val start: LocalDate,
    val end: LocalDate,
): RemoteTaskData()