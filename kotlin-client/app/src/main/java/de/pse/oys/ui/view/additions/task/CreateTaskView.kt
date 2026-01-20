package de.pse.oys.ui.view.additions.task

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime

@Composable
fun CreateTaskView(viewModel: ICreateTaskViewModel) {
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        TODO()
    }
}

enum class TaskType {
    EXAM,
    SUBMISSION,
    OTHER,
}

interface ICreateTaskViewModel {
    val availableModules: List<String>
    val showDelete: Boolean

    var title: String
    var module: String
    var type: TaskType
    var weeklyTimeLoad: Int
    var sendNotification: Boolean

    var examDate: LocalDate

    var submissionDate: LocalDateTime
    var submissionCycle: Int

    var start: LocalDate
    var end: LocalDate

    fun submit()
    fun delete()
}