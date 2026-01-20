package de.pse.oys.data.facade

import androidx.compose.ui.graphics.Color

typealias Module = Identified<ModuleData>

data class ModuleData(
    val title: String,
    val description: String,
    val priority: Priority,
    val color: Color
)

enum class Priority {
    LOW,
    NEUTRAL,
    HIGH,
}