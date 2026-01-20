package de.pse.oys.data.facade

import kotlin.uuid.Uuid

data class Identified<T>(val data: T, val id: Uuid)

class ModelFacade(
    var modules: Map<Uuid, ModuleData>? = null,
    var tasks: Map<Uuid, TaskData>? = null,
    var freeTimes: Map<Uuid, FreeTimeData>? = null,
    var steps: Map<Uuid, StepData>? = null
)