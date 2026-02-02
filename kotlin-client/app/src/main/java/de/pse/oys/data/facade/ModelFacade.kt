package de.pse.oys.data.facade

import kotlinx.datetime.DayOfWeek
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
data class Identified<out T>(val data: T, val id: Uuid)

class ModelFacade(
    var modules: Map<Uuid, ModuleData>? = null,
    var tasks: Map<Uuid, TaskData>? = null,
    var freeTimes: Map<Uuid, FreeTimeData>? = null,
    var steps: Map<DayOfWeek, Map<Uuid, StepData>>? = null
)