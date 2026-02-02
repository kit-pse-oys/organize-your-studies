package de.pse.oys.ui.view

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import de.pse.oys.data.api.RemoteAPI
import de.pse.oys.data.facade.FreeTime
import de.pse.oys.data.facade.ModelFacade
import de.pse.oys.data.facade.Module
import de.pse.oys.data.facade.Task
import de.pse.oys.data.properties.Properties
import de.pse.oys.ui.navigation.AccountSettings
import de.pse.oys.ui.navigation.Additions
import de.pse.oys.ui.navigation.AvailableRatings
import de.pse.oys.ui.navigation.CreateFreeTime
import de.pse.oys.ui.navigation.CreateModule
import de.pse.oys.ui.navigation.CreateTask
import de.pse.oys.ui.navigation.EditFreeTime
import de.pse.oys.ui.navigation.EditModule
import de.pse.oys.ui.navigation.EditTask
import de.pse.oys.ui.navigation.Login
import de.pse.oys.ui.navigation.Main
import de.pse.oys.ui.navigation.Menu
import de.pse.oys.ui.navigation.MyFreeTimes
import de.pse.oys.ui.navigation.MyModules
import de.pse.oys.ui.navigation.MyTasks
import de.pse.oys.ui.navigation.Questionnaire
import de.pse.oys.ui.navigation.Rating
import de.pse.oys.ui.view.additions.AdditionsView
import de.pse.oys.ui.view.additions.AdditionsViewModel
import de.pse.oys.ui.view.additions.freetime.CreateFreeTimeView
import de.pse.oys.ui.view.additions.freetime.CreateFreeTimeViewModel
import de.pse.oys.ui.view.additions.freetime.EditFreeTimeViewModel
import de.pse.oys.ui.view.additions.freetime.FreeTimesView
import de.pse.oys.ui.view.additions.freetime.FreeTimesViewModel
import de.pse.oys.ui.view.additions.module.CreateModuleView
import de.pse.oys.ui.view.additions.module.CreateModuleViewModel
import de.pse.oys.ui.view.additions.module.EditModuleViewModel
import de.pse.oys.ui.view.additions.module.ModulesView
import de.pse.oys.ui.view.additions.module.ModulesViewModel
import de.pse.oys.ui.view.additions.task.CreateTaskView
import de.pse.oys.ui.view.additions.task.CreateTaskViewModel
import de.pse.oys.ui.view.additions.task.EditTaskViewModel
import de.pse.oys.ui.view.additions.task.TasksView
import de.pse.oys.ui.view.additions.task.TasksViewModel
import de.pse.oys.ui.view.menu.AccountSettingsView
import de.pse.oys.ui.view.menu.AccountSettingsViewModel
import de.pse.oys.ui.view.menu.MenuView
import de.pse.oys.ui.view.menu.MenuViewModel
import de.pse.oys.ui.view.onboarding.LoginView
import de.pse.oys.ui.view.onboarding.LoginViewModel
import de.pse.oys.ui.view.onboarding.QuestionnaireView
import de.pse.oys.ui.view.onboarding.QuestionnaireViewModel
import de.pse.oys.ui.view.ratings.AvailableRatingsView
import de.pse.oys.ui.view.ratings.AvailableRatingsViewModel
import de.pse.oys.ui.view.ratings.RatingView
import de.pse.oys.ui.view.ratings.RatingViewModel
import kotlin.uuid.Uuid

@Composable
fun EntryPoint(
    model: ModelFacade,
    api: RemoteAPI,
    properties: Properties,
    startWithLogin: Boolean,
    onGoogleLogin: () -> Unit
) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = if (startWithLogin) Login else Main) {
        composable<Main> { MainView(viewModel { MainViewModel(api, model, navController) }) }
        composable<Login> { LoginView(viewModel { LoginViewModel(api, navController) }, onGoogleLogin = onGoogleLogin) }
        composable<Menu> { MenuView(viewModel { MenuViewModel(properties, api, navController) }) }
        composable<AccountSettings> {
            AccountSettingsView(viewModel { AccountSettingsViewModel(api, navController) })
        }
        composable<Questionnaire> { backEntry ->
            val firstTime = backEntry.toRoute<Questionnaire>().firstTime
            QuestionnaireView(viewModel { QuestionnaireViewModel(firstTime, api, navController) })
        }
        composable<Rating> { backEntry ->
            val id = backEntry.toRoute<Rating>().step
            val target = Uuid.parse(id)
            RatingView(viewModel { RatingViewModel(api, target, navController) })
        }
        composable<AvailableRatings> {
            AvailableRatingsView(viewModel { AvailableRatingsViewModel(api, model, navController) })
        }
        composable<Additions> { AdditionsView(viewModel { AdditionsViewModel(navController) }) }
        composable<MyModules> { ModulesView(viewModel { ModulesViewModel(model, navController) }) }
        composable<CreateModule> {
            CreateModuleView(viewModel { CreateModuleViewModel(api, model, navController) })
        }
        composable<EditModule> { backEntry ->
            val id = backEntry.toRoute<EditModule>().id
            val target = Uuid.parse(id)
            val module =
                model.modules?.get(target) ?: error("Module not found")
            CreateModuleView(viewModel {
                EditModuleViewModel(api, model, Module(module, target), navController)
            })
        }
        composable<MyFreeTimes> {
            FreeTimesView(viewModel { FreeTimesViewModel(model, navController) })
        }
        composable<CreateFreeTime> {
            CreateFreeTimeView(viewModel { CreateFreeTimeViewModel(api, model, navController) })
        }
        composable<EditFreeTime> { backEntry ->
            val id = backEntry.toRoute<EditFreeTime>().id
            val target = Uuid.parse(id)
            val freeTime =
                model.freeTimes?.get(target) ?: error("FreeTime not found")
            CreateFreeTimeView(viewModel {
                EditFreeTimeViewModel(api, model, FreeTime(freeTime, target), navController)
            })
        }
        composable<MyTasks> {
            TasksView(viewModel { TasksViewModel(model, navController) })
        }
        composable<CreateTask> {
            CreateTaskView(viewModel { CreateTaskViewModel(api, model, navController) })
        }
        composable<EditTask> { backEntry ->
            val id = backEntry.toRoute<EditTask>().id
            val target = Uuid.parse(id)
            val task =
                model.tasks?.get(target) ?: error("Task not found")
            CreateTaskView(viewModel {
                EditTaskViewModel(api, model, Task(task, target), navController)
            })
        }
    }
}