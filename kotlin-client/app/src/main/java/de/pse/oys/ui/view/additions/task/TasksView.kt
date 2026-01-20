package de.pse.oys.ui.view.additions.task

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import de.pse.oys.data.facade.Task

@Composable
fun TasksView(viewModel: ITasksViewModel) {
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        TODO()
    }
}

interface ITasksViewModel {
    val tasks: List<Task>

    fun select(task: Task)
}