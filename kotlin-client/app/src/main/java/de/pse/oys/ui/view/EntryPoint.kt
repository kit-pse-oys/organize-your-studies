package de.pse.oys.ui.view

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import de.pse.oys.data.api.RemoteAPI
import de.pse.oys.data.facade.ModelFacade
import de.pse.oys.data.properties.Properties
import de.pse.oys.ui.navigation.AccountSettings
import de.pse.oys.ui.navigation.CreateModule
import de.pse.oys.ui.navigation.Login
import de.pse.oys.ui.navigation.Main
import de.pse.oys.ui.navigation.Menu
import de.pse.oys.ui.navigation.Questionnaire
import de.pse.oys.ui.navigation.login
import de.pse.oys.ui.view.additions.module.CreateModuleView
import de.pse.oys.ui.view.additions.module.CreateModuleViewModel
import de.pse.oys.ui.view.menu.AccountSettingsView
import de.pse.oys.ui.view.menu.AccountSettingsViewModel
import de.pse.oys.ui.view.menu.MenuView
import de.pse.oys.ui.view.menu.MenuViewModel
import de.pse.oys.ui.view.onboarding.LoginView
import de.pse.oys.ui.view.onboarding.LoginViewModel
import de.pse.oys.ui.view.onboarding.QuestionnaireView
import de.pse.oys.ui.view.onboarding.QuestionnaireViewModel

@Composable
fun EntryPoint(
    model: ModelFacade,
    api: RemoteAPI,
    properties: Properties
) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Main) {
        composable<Main> {
//            LaunchedEffect(api.isLoggedIn) {
//                if (!api.isLoggedIn) {
//                    navController.login(dontGoBack = Main)
//                }
//            }

            MainView(viewModel { MainViewModel(api, model, navController) })
        }
        composable<Login> { LoginView(viewModel { LoginViewModel(api, navController) }) }
        composable<Menu> { MenuView(viewModel { MenuViewModel(properties, api, navController) }) }
        composable<AccountSettings> {
            AccountSettingsView(viewModel {
                AccountSettingsViewModel(
                    api,
                    navController
                )
            })
        }
        composable<Questionnaire> { backEntry ->
            val firstTime = backEntry.toRoute<Questionnaire>().firstTime
            QuestionnaireView(viewModel { QuestionnaireViewModel(firstTime, api, navController) })
        }
        composable<CreateModule> { backEntry ->
            val intent = backEntry.toRoute<CreateModule>().intent
            CreateModuleView(viewModel { CreateModuleViewModel(intent, api, model, navController) })
        }
    }
}