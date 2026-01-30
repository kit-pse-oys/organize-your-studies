package de.pse.oys.ui.debug

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import de.pse.oys.data.Answer
import kotlinx.datetime.LocalTime
import de.pse.oys.data.Question
import de.pse.oys.data.RatingAspect
import de.pse.oys.data.facade.ExamTaskData
import de.pse.oys.data.facade.FreeTime
import de.pse.oys.data.facade.FreeTimeData
import de.pse.oys.data.facade.Identified
import de.pse.oys.data.properties.Darkmode
import de.pse.oys.data.facade.ModelFacade
import de.pse.oys.data.facade.Module
import de.pse.oys.data.facade.ModuleData
import de.pse.oys.data.facade.OtherTaskData
import de.pse.oys.data.facade.Priority
import de.pse.oys.data.facade.Rating
import de.pse.oys.data.facade.SubmissionTaskData
import de.pse.oys.data.facade.Task
import de.pse.oys.ui.theme.OrganizeYourStudiesTheme
import de.pse.oys.ui.view.additions.AdditionsView
import de.pse.oys.ui.view.additions.IAdditionsViewModel
import de.pse.oys.ui.view.additions.freetime.BaseCreateFreeTimeViewModel
import de.pse.oys.ui.view.additions.freetime.CreateFreeTimeView
import de.pse.oys.ui.view.additions.freetime.FreeTimesView
import de.pse.oys.ui.view.additions.freetime.ICreateFreeTimeViewModel
import de.pse.oys.ui.view.additions.freetime.IFreeTimesViewModel
import de.pse.oys.ui.view.additions.module.CreateModuleView
import de.pse.oys.ui.view.additions.module.ICreateModuleViewModel
import de.pse.oys.ui.view.additions.module.IModulesViewModel
import de.pse.oys.ui.view.additions.module.ModulesView
import de.pse.oys.ui.view.additions.task.BaseCreateTaskViewModel
import de.pse.oys.ui.view.additions.task.CreateTaskView
import de.pse.oys.ui.view.additions.task.ITasksViewModel
import de.pse.oys.ui.view.additions.task.TasksView
import de.pse.oys.ui.view.menu.AccountSettingsView
import de.pse.oys.ui.view.menu.IAccountSettingsViewModel
import de.pse.oys.ui.view.menu.IMenuViewModel
import de.pse.oys.ui.view.menu.MenuView
import de.pse.oys.ui.view.onboarding.ILoginViewModel
import de.pse.oys.ui.view.onboarding.IQuestionnaireViewModel
import de.pse.oys.ui.view.onboarding.LoginView
import de.pse.oys.ui.view.onboarding.QuestionnaireView
import de.pse.oys.ui.view.ratings.AvailableRatingsView
import de.pse.oys.ui.view.ratings.IAvailableRatingsViewModel
import de.pse.oys.ui.view.ratings.IRatingViewModel
import de.pse.oys.ui.view.ratings.RatingTarget
import de.pse.oys.ui.view.ratings.RatingView
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.uuid.Uuid

@Preview(
    showBackground = true,)
@Composable
fun CreateModuleInteractivePreview() {
    val mockVM = remember {
        object : ICreateModuleViewModel {
            override val showDelete = false
            override var title by mutableStateOf("")
            override var description by mutableStateOf("")
            override var priority by mutableStateOf(Priority.NEUTRAL)
            override var color by mutableStateOf(Color.Blue)

            override fun submit() { /* Klick-Simulation */
            }

            override fun delete() { /* Klick-Simulation */
            }
        }
    }
    OrganizeYourStudiesTheme {
        CreateModuleView(viewModel = mockVM)
    }
}


class PreviewFreeTimeViewModel(override val showDelete: Boolean = true) : ICreateFreeTimeViewModel {
    override var title by mutableStateOf("Kinoabend")
    override var date by mutableStateOf(LocalDate(2024, 12, 24))
    override var start by mutableStateOf(LocalTime(18, 0))
    override var end by mutableStateOf(LocalTime(21, 0))
    override var weekly by mutableStateOf(false)

    override fun submit() {}
    override fun delete() {}
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun CreateFreeTimePreview() {
        CreateFreeTimeView(viewModel = PreviewFreeTimeViewModel(showDelete = true))

}


@Preview(showBackground = true, name = "Preview: Freizeit")
@Composable
fun PreviewCreateFreeTime() {
    androidx.compose.ui.platform.LocalContext.current

    val mockModel = remember { ModelFacade() }
    val mockNavController = androidx.navigation.compose.rememberNavController()

    val mockVM = remember {
        object : BaseCreateFreeTimeViewModel(
            model = mockModel,
            navController = mockNavController,
            freeTime = null // Optional: Hier könntest du Testdaten übergeben
        ) {
            override val showDelete = false
            override fun submit() {}
            override fun delete() {}
        }
    }

    CreateFreeTimeView(viewModel = mockVM)
}


@Preview(showBackground = true, name = "Preview: Aufgabe")
@Composable
fun PreviewCreateTask() {
    androidx.compose.ui.platform.LocalContext.current

    // 1. Mock ModelFacade vorbereiten
    val mockModel = remember {
        ModelFacade().apply {
            // WICHTIG: Hier müssen Daten rein, damit require(model.modules != null) klappt
            modules = mutableMapOf()
            // Optional: Hier könntest du schon echte Strings einfügen
        }
    }

    // 2. Mock NavController
    val mockNavController = androidx.navigation.compose.rememberNavController()

    val mockTaskVM = remember {
        object : BaseCreateTaskViewModel(
            model = mockModel,
            navController = mockNavController,
            task = null
        ) {
            override val showDelete = false

            // Da die Basisklasse availableModules aus dem Model zieht,
            // kannst du es hier entweder überschreiben oder das Model füllen.
            override val availableModules = listOf("Mathe I", "Programmieren", "Design")

            override fun submit() {}
            override fun delete() {}
        }
    }
    CreateTaskView(viewModel = mockTaskVM)
}

@Preview(showBackground = true, name = "Vorschau: Bewertungen Liste")
@Composable
fun PreviewAvailableRatings() {
    // Wir erstellen ein Mock-ViewModel direkt in der Preview
    val mockViewModel = object : IAvailableRatingsViewModel {
        override val available = listOf(
            RatingTarget("Mathematik I", Color(0xFFE57373)), // Rot
            RatingTarget("Programmieren", Color(0xFF81C784)), // Grün
            RatingTarget("Software Engineering", Color(0xFF64B5F6)), // Blau
            RatingTarget("Mediendesign", Color(0xFFFFB74D)), // Orange
            RatingTarget("Kaffee-Pause", Color(0xFF9575CD)),  // Lila
            RatingTarget("Mathematik", Color(0xFFE57373)), // Rot
            RatingTarget("Programmeren", Color(0xFF81C784)), // Grün
            RatingTarget("Software Egineering", Color(0xFF64B5F6)), // Blau
            RatingTarget("Mediendesin", Color(0xFFFFB74D)), // Orange
            RatingTarget("Kaffee-Pase", Color(0xFF9575CD))  // Lila
        )

        override fun selectRating(rating: RatingTarget) {
            println("Ausgewählt: ${rating.name}")
        }


    }
    AvailableRatingsView(viewModel = mockViewModel)
}

@Preview(showBackground = true, name = "Vorschau: Bewertungen Liste")
@Composable
fun PreviewAvailableRatingsI() {
    // Wir erstellen ein Mock-ViewModel direkt in der Preview
    val mockViewModel = object : IAvailableRatingsViewModel {
        override val available = emptyList<RatingTarget>()

        override fun selectRating(rating: RatingTarget) {
            println("Ausgewählt: ${rating.name}")
        }

    }
    AvailableRatingsView(viewModel = mockViewModel)
}

class MockRatingViewModel : IRatingViewModel {
    override fun getRating(aspect: RatingAspect): Rating = Rating.MEDIUM
    override fun updateRating(aspect: RatingAspect, rating: Rating) {}
    override fun submitRating() {}
    override fun submitMissed() {}
}

@Preview(showBackground = true, name = "Rating View Preview")
@Composable
fun RatingViewPreview() {
    // Hier rufen wir einfach deine View mit dem Mock-ViewModel auf
    // Ohne OYSTheme oder sonstigen Schnickschnack
    RatingView(viewModel = MockRatingViewModel())
}


@Preview(showBackground = true, name = "Login & Register Preview")
@Composable
fun PreviewLoginView() {
    // States für das Mock-ViewModel
    val usernameState = remember { mutableStateOf("") }
    val passwordState = remember { mutableStateOf("") }

    val mockVM = object : ILoginViewModel {
        override var username: String
            get() = usernameState.value
            set(value) {
                usernameState.value = value
            }

        override var password: String
            get() = passwordState.value
            set(value) {
                passwordState.value = value
            }

        override fun login() {
            println("Login mit ${usernameState.value}")
        }

        override fun loginWithOIDC() {}
        override fun register() {
            println("Registrierung mit ${usernameState.value}")
        }

        override fun registerWithOIDC() {}
    }
    LoginView(viewModel = mockVM)
}


class PreviewTasksViewModel : ITasksViewModel {
    // Hilfs-Modul für die Tasks
    private val testModule = Identified(
        id = Uuid.random(),
        data = ModuleData("Informatik", "Grundlagen", Priority.HIGH, Color.Blue)
    )

    override val tasks: List<Task> = listOf(
        // Beispiel 1: Klausur
        Identified(
            id = Uuid.random(),
            data = ExamTaskData(
                title = "Mathe-Klausur",
                module = testModule,
                weeklyTimeLoad = 10,
                sendNotification = true,
                examDate = LocalDate(2026, 2, 15)
            )
        ),
        // Beispiel 2: Abgabe
        Identified(
            id = Uuid.random(),
            data = SubmissionTaskData(
                title = "Übungsblatt 1",
                module = testModule,
                weeklyTimeLoad = 5,
                sendNotification = false,
                firstDate = LocalDateTime(2026, 1, 23, 12, 0),
                cycle = 2
            )
        ),
        // Beispiel 3: Sonstiges
        Identified(
            id = Uuid.random(),
            data = OtherTaskData(
                title = "Projektarbeit",
                module = testModule,
                weeklyTimeLoad = 8,
                sendNotification = true,
                start = LocalDate(2026, 1, 1),
                end = LocalDate(2026, 3, 31)
            )
        )
    )

    override fun select(task: Task) {
        // Nicht nötig für Preview
    }
}

@Preview(showBackground = true)
@Composable
fun TasksViewPreview() {
    // Hier kannst du dein Theme drumherum setzen, falls vorhanden
    TasksView(viewModel = PreviewTasksViewModel())
}


class PreviewFreeTimesViewModel : IFreeTimesViewModel {
    private val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())

    override val freeTimes: List<FreeTime> = listOf(
        Identified(
            id = Uuid.random(),
            data = FreeTimeData(
                title = "Fitnessstudio",
                date = now.date,
                start = now.time,
                end = now.time, // In echt wäre das natürlich später
                weekly = true
            )
        ),
        Identified(
            id = Uuid.random(),
            data = FreeTimeData(
                title = "Kino Abend",
                date = now.date,
                start = now.time,
                end = now.time,
                weekly = false
            )
        )
    )

    override fun select(freeTime: FreeTime) {
        // Logik für Preview nicht nötig
    }

}

@Preview(showBackground = true)
@Composable
fun FreeTimesViewPreview() {
    // Ersetze 'YourThemeName' durch den echten Namen deines Themes (z.B. OYSTheme)
    Column {
        FreeTimesView(viewModel = PreviewFreeTimesViewModel())
    }
}


class PreviewModulesViewModel : IModulesViewModel {
    override val modules: List<Module> = listOf(
        Identified(
            // Hier war der Fehler: Wir parsen den String zu einer UUID
            id = Uuid.parse("00000000-0000-0000-0000-000000000001"),
            data = ModuleData(
                title = "Mathematik 1",
                description = "Analysis",
                priority = Priority.HIGH,
                color = Color.Blue
            )
        ),
        Identified(
            id = Uuid.parse("00000000-0000-0000-0000-000000000002"),
            data = ModuleData(
                title = "Programmieren",
                description = "Kotlin Basics",
                priority = Priority.NEUTRAL,
                color = Color.Green
            )
        )
    )

    override fun select(module: Module) {
        // Preview braucht keine Logik
    }

}

@Preview(showBackground = true)
@Composable
fun ModulesViewPreview() {

    val mockVM = PreviewModulesViewModel()
    ModulesView(viewModel = mockVM)

}

@Preview(showBackground = true, name = "Additions Auswahl")
@Composable
fun PreviewAdditionsView() {
    // Einfaches Mock-ViewModel für die Preview
    val mockViewModel = object : IAdditionsViewModel {
        override fun navigateToCreateModule() {
            println("Navigiere zu Modul")
        }

        override fun navigateToCreateTask() {
            println("Navigiere zu Aufgabe")
        }

        override fun navigateToCreateFreeTime() {
            println("Navigiere zu Freizeit")
        }
    }


    AdditionsView(viewModel = mockViewModel)

}



@Preview(showBackground = true)
@Composable
fun QuestionnaireViewPreview() {
    val mockViewModel = object : IQuestionnaireViewModel {
        override var showWelcome: Boolean = false // Setze auf true, um den Willkommens-Screen zu sehen
        override fun selected(question: Question, answer: Answer): StateFlow<Boolean> {
            return MutableStateFlow(false)
        }
        override fun select(question: Question, answer: Answer) {
        }
        override fun showQuestionnaire() {
        }
        override fun submitQuestionnaire() {
        }
    }
    QuestionnaireView(viewModel = mockViewModel)
}

@Preview(showBackground = true)
@Composable
fun WelcomeViewPreview() {
    val mockViewModel = object : IQuestionnaireViewModel {
        override var showWelcome: Boolean = true // Setze auf true, um den Willkommens-Screen zu sehen
        override fun selected(question: Question, answer: Answer): StateFlow<Boolean> {
            return MutableStateFlow(false)
        }
        override fun select(question: Question, answer: Answer) {
        }
        override fun showQuestionnaire() {
        }
        override fun submitQuestionnaire() {
        }
    }
    QuestionnaireView(viewModel = mockViewModel)
}


/**
 * Ein einfaches Mock-ViewModel für die Preview.
 * Hiermit können wir die UI rendern, ohne eine echte API oder Navigation zu brauchen.
 */
class PreviewMenuViewModel : IMenuViewModel {
    override val darkmode: StateFlow<Darkmode> = MutableStateFlow(Darkmode.SYSTEM)

    override fun setDarkmode(darkmode: Darkmode) {}
    override fun navigateToModules() {}
    override fun navigateToTasks() {}
    override fun navigateToFreeTimes() {}
    override fun navigateToEditQuestionnaire() {}
    override fun navigateToAccountSettings() {}
    override fun updatePlan() {}
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun MenuPreview() {
        MenuView(viewModel = PreviewMenuViewModel())
}

/**
 * Mock-ViewModel für die AccountSettings-Vorschau.
 */
class PreviewAccountSettingsViewModel : IAccountSettingsViewModel {
    override fun logout() {
    }
    override fun deleteAccount() {
    }
}
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun AccountSettingsPreview() {
        AccountSettingsView(viewModel = PreviewAccountSettingsViewModel())
}

/*
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


   Spacer(modifier = Modifier.weight(1f))
            SubmitButton(stringResource(id = R.string.save_freetime)) { viewModel.submit() }
            BackButton(onClick = { viewModel.navigateBack() })

             */



// DARKMODE PREVIEWS:

@Preview(name = "Dark Mode",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun CreateModuleDarkPreview() {
    val mockVM = remember {
        object : ICreateModuleViewModel {
            override val showDelete = false
            override var title by mutableStateOf("")
            override var description by mutableStateOf("")
            override var priority by mutableStateOf(Priority.NEUTRAL)
            override var color by mutableStateOf(Color.Blue)

            override fun submit() { /* Klick-Simulation */
            }

            override fun delete() { /* Klick-Simulation */
            }
        }
    }
    OrganizeYourStudiesTheme {
        CreateModuleView(viewModel = mockVM)
    }
}


@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun CreateFreeTimeDarkPreview() {
    OrganizeYourStudiesTheme {
        CreateFreeTimeView(viewModel = PreviewFreeTimeViewModel(showDelete = true))
    }
}


@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun DarkPreviewCreateFreeTime() {
    androidx.compose.ui.platform.LocalContext.current

    val mockModel = remember { ModelFacade() }
    val mockNavController = androidx.navigation.compose.rememberNavController()

    val mockVM = remember {
        object : BaseCreateFreeTimeViewModel(
            model = mockModel,
            navController = mockNavController,
            freeTime = null // Optional: Hier könntest du Testdaten übergeben
        ) {
            override val showDelete = false
            override fun submit() {}
            override fun delete() {}
        }
    }
    OrganizeYourStudiesTheme() {
        CreateFreeTimeView(viewModel = mockVM)
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PreviewCreateTaskDark() {
    androidx.compose.ui.platform.LocalContext.current

    // 1. Mock ModelFacade vorbereiten
    val mockModel = remember {
        ModelFacade().apply {
            // WICHTIG: Hier müssen Daten rein, damit require(model.modules != null) klappt
            modules = mutableMapOf()
            // Optional: Hier könntest du schon echte Strings einfügen
        }
    }

    // 2. Mock NavController
    val mockNavController = androidx.navigation.compose.rememberNavController()

    val mockTaskVM = remember {
        object : BaseCreateTaskViewModel(
            model = mockModel,
            navController = mockNavController,
            task = null
        ) {
            override val showDelete = false

            // Da die Basisklasse availableModules aus dem Model zieht,
            // kannst du es hier entweder überschreiben oder das Model füllen.
            override val availableModules = listOf("Mathe I", "Programmieren", "Design")

            override fun submit() {}
            override fun delete() {}
        }
    }
    OrganizeYourStudiesTheme() {
        CreateTaskView(viewModel = mockTaskVM)
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PreviewAvailableRatingsDark() {
    // Wir erstellen ein Mock-ViewModel direkt in der Preview
    val mockViewModel = object : IAvailableRatingsViewModel {
        override val available = listOf(
            RatingTarget("Mathematik I", Color(0xFFE57373)), // Rot
            RatingTarget("Programmieren", Color(0xFF81C784)), // Grün
            RatingTarget("Software Engineering", Color(0xFF64B5F6)), // Blau
            RatingTarget("Mediendesign", Color(0xFFFFB74D)), // Orange
            RatingTarget("Kaffee-Pause", Color(0xFF9575CD)),  // Lila
            RatingTarget("Mathematik", Color(0xFFE57373)), // Rot
            RatingTarget("Programmeren", Color(0xFF81C784)), // Grün
            RatingTarget("Software Egineering", Color(0xFF64B5F6)), // Blau
            RatingTarget("Mediendesin", Color(0xFFFFB74D)), // Orange
            RatingTarget("Kaffee-Pase", Color(0xFF9575CD))  // Lila
        )

        override fun selectRating(rating: RatingTarget) {
            println("Ausgewählt: ${rating.name}")
        }


    }
    OrganizeYourStudiesTheme() {
        AvailableRatingsView(viewModel = mockViewModel)
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PreviewAvailableRatingsIDark() {
    // Wir erstellen ein Mock-ViewModel direkt in der Preview
    val mockViewModel = object : IAvailableRatingsViewModel {
        override val available = emptyList<RatingTarget>()

        override fun selectRating(rating: RatingTarget) {
            println("Ausgewählt: ${rating.name}")
        }

    }
    OrganizeYourStudiesTheme() {
        AvailableRatingsView(viewModel = mockViewModel)
    }
}


@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun RatingViewPreviewDark() {
    // Hier rufen wir einfach deine View mit dem Mock-ViewModel auf
    // Ohne OYSTheme oder sonstigen Schnickschnack
    OrganizeYourStudiesTheme() {
        RatingView(viewModel = MockRatingViewModel())
    }
}


@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PreviewLoginViewDark() {
    // States für das Mock-ViewModel
    val usernameState = remember { mutableStateOf("") }
    val passwordState = remember { mutableStateOf("") }

    val mockVM = object : ILoginViewModel {
        override var username: String
            get() = usernameState.value
            set(value) {
                usernameState.value = value
            }

        override var password: String
            get() = passwordState.value
            set(value) {
                passwordState.value = value
            }

        override fun login() {
            println("Login mit ${usernameState.value}")
        }

        override fun loginWithOIDC() {}
        override fun register() {
            println("Registrierung mit ${usernameState.value}")
        }

        override fun registerWithOIDC() {}
    }
    OrganizeYourStudiesTheme() {
        LoginView(viewModel = mockVM)
    }
}


@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun TasksViewPreviewDark() {
    // Hier kannst du dein Theme drumherum setzen, falls vorhanden
    OrganizeYourStudiesTheme() {
        TasksView(viewModel = PreviewTasksViewModel())
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun FreeTimesViewPreviewDark() {
    // Ersetze 'YourThemeName' durch den echten Namen deines Themes (z.B. OYSTheme)
    OrganizeYourStudiesTheme() {
        FreeTimesView(viewModel = PreviewFreeTimesViewModel())
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun ModulesViewPreviewDark() {

    val mockVM = PreviewModulesViewModel()
    OrganizeYourStudiesTheme() {
        ModulesView(viewModel = mockVM)
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PreviewAdditionsViewDark() {
    // Einfaches Mock-ViewModel für die Preview
    val mockViewModel = object : IAdditionsViewModel {
        override fun navigateToCreateModule() {
            println("Navigiere zu Modul")
        }

        override fun navigateToCreateTask() {
            println("Navigiere zu Aufgabe")
        }

        override fun navigateToCreateFreeTime() {
            println("Navigiere zu Freizeit")
        }
    }
    OrganizeYourStudiesTheme() {
        AdditionsView(viewModel = mockViewModel)
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun QuestionnaireViewPreviewDark() {
    val mockViewModel = object : IQuestionnaireViewModel {
        override var showWelcome: Boolean = false // Setze auf true, um den Willkommens-Screen zu sehen
        override fun selected(question: Question, answer: Answer): StateFlow<Boolean> {
            return MutableStateFlow(false)
        }
        override fun select(question: Question, answer: Answer) {
        }
        override fun showQuestionnaire() {
        }
        override fun submitQuestionnaire() {
        }
    }
    OrganizeYourStudiesTheme() {
        QuestionnaireView(viewModel = mockViewModel)
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun WelcomeViewPreviewDark() {
    val mockViewModel = object : IQuestionnaireViewModel {
        override var showWelcome: Boolean = true // Setze auf true, um den Willkommens-Screen zu sehen
        override fun selected(question: Question, answer: Answer): StateFlow<Boolean> {
            return MutableStateFlow(false)
        }
        override fun select(question: Question, answer: Answer) {
        }
        override fun showQuestionnaire() {
        }
        override fun submitQuestionnaire() {
        }
    }
    OrganizeYourStudiesTheme() {
        QuestionnaireView(viewModel = mockViewModel)
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun MenuPreviewDark() {
    OrganizeYourStudiesTheme() {
        MenuView(viewModel = PreviewMenuViewModel())
    }
}
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun AccountSettingsPreviewDark() {
    OrganizeYourStudiesTheme() {
        AccountSettingsView(viewModel = PreviewAccountSettingsViewModel())
    }
}

