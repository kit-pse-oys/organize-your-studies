package de.pse.oys.ui.view.additions

import androidx.navigation.NavController
import de.pse.oys.ui.navigation.createFreeTime
import de.pse.oys.ui.navigation.createModule
import de.pse.oys.ui.navigation.createTask
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test

class AdditionsViewModelTest {

    private val navController = mockk<NavController>(relaxed = true)

    private val viewModel = AdditionsViewModel(navController)

    @Test
    fun `MapsToCreateModule calls navController with correct route`() {
        viewModel.navigateToCreateModule()
        verify { navController.createModule() }
    }

    @Test
    fun `MapsToCreateTask calls navController with correct route`() {
        viewModel.navigateToCreateTask()
        verify { navController.createTask() }
    }

    @Test
    fun `MapsToCreateFreeTime calls navController with correct route`() {
        viewModel.navigateToCreateFreeTime()
        verify { navController.createFreeTime() }
    }
}