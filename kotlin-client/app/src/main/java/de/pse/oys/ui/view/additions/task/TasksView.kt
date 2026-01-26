package de.pse.oys.ui.view.additions.task

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.pse.oys.R
import de.pse.oys.data.facade.ExamTaskData
import de.pse.oys.data.facade.OtherTaskData
import de.pse.oys.data.facade.SubmissionTaskData
import de.pse.oys.data.facade.Task
import de.pse.oys.ui.theme.Blue
import de.pse.oys.ui.theme.LightBlue
import de.pse.oys.ui.util.ViewHeader
import de.pse.oys.ui.util.toFormattedString

@Composable
fun TasksView(viewModel: ITasksViewModel) {
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                ViewHeader(text = stringResource(id = R.string.my_tasks_button))
            }
            items(viewModel.tasks) { task ->
                TaskButton(task, viewModel)
            }
        }
    }
}


@Composable
private fun TaskButton(task: Task, viewModel: ITasksViewModel) {
    OutlinedButton(
        onClick = { viewModel.select(task) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 16.dp),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(2.dp, Blue),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = LightBlue,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp, horizontal = 2.dp)
        ) {
            Row(
                modifier = Modifier.padding(bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    task.data.title, style = typography.titleLarge.copy(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                )
                Text(stringResource(id = R.string.task_from_module) + task.data.module.data.title)
            }
            when (task.data) {
                is ExamTaskData -> {
                    Text(stringResource(id = R.string.task_type_is) + stringResource(id = R.string.examTask))
                    Text(stringResource(id = R.string.enter_exam_date) + task.data.examDate.toFormattedString())
                }

                is SubmissionTaskData -> {
                    Text(stringResource(id = R.string.task_type_is) + stringResource(id = R.string.submissionTask))
                    Text(stringResource(id = R.string.submission_task_since) + task.data.firstDate.toFormattedString())
                    Text(task.data.cycle.toString() + stringResource(id = R.string.submission_weekcycle_is))
                }

                is OtherTaskData -> {
                    Text(stringResource(id = R.string.task_type_is) + stringResource(id = R.string.otherTask))
                    Text(task.data.start.toFormattedString() + " - " + task.data.end.toFormattedString())
                }
            }
        }
    }
}


interface ITasksViewModel {
    val tasks: List<Task>

    fun select(task: Task)
}