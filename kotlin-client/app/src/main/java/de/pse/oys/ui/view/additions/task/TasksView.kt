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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.pse.oys.data.facade.ExamTaskData
import de.pse.oys.data.facade.OtherTaskData
import de.pse.oys.data.facade.SubmissionTaskData
import de.pse.oys.data.facade.Task
import de.pse.oys.ui.theme.Blue
import de.pse.oys.ui.theme.LightBlue
import de.pse.oys.ui.view.additions.freetime.format
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.number


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
                Text(
                    "Meine Aufgaben",
                    style = typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.padding(top = 18.dp, bottom = 10.dp)
                )
            }
            items(viewModel.tasks) { task ->
                OutlinedButton(
                    onClick = { viewModel.select(task) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp, horizontal = 16.dp),
                    shape = RoundedCornerShape(24.dp),
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
                            Text(" aus " + task.data.module.data.title)
                        }
                        when (task.data) {
                            is ExamTaskData -> {
                                Text("Kategorie: Klausur")
                                Text("Klausurtermin: " + task.data.examDate.format())
                            }

                            is SubmissionTaskData -> {
                                Text("Kategorie: Abgabe")
                                Text("Seit " + task.data.firstDate.formatDate + ", um " + task.data.firstDate.formatTime + " Uhr im " + task.data.cycle.toString() + "-Wochenzyklus")
                            }

                            is OtherTaskData -> {
                                Text("Kategorie: Sonstige")
                                Text("Aufgabezeitraum: " + task.data.start.format() + " - " + task.data.end.format())
                            }
                        }
                    }
                }
            }
        }
    }
}


interface ITasksViewModel {
    val tasks: List<Task>

    fun select(task: Task)
}

val LocalDateTime.formatDate: String
    get() = "${day.toString().padStart(2, '0')}.${month.number.toString().padStart(2, '0')}.$year"

val LocalDateTime.formatTime: String
    get() = "${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}"
