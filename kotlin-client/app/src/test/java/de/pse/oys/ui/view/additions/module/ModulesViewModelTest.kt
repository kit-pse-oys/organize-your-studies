package de.pse.oys.ui.view.additions.module

import androidx.navigation.NavController
import de.pse.oys.data.facade.Identified
import de.pse.oys.data.facade.ModuleData
import de.pse.oys.data.facade.Priority
import de.pse.oys.ui.navigation.editModule
import de.pse.oys.ui.theme.Blue
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Test


class ModulesViewModelTest {
    private val navController = mockk<NavController>(relaxed = true)
    val moduleData = ModuleData(
        title = "Test-Title",
        description = "Test-Description",
        priority = Priority.HIGH,
        color = Blue
    )
    val testModule = Identified(
        id = kotlin.uuid.Uuid.random(),
        data = moduleData
    )

    @Test
    fun `available list matches the data passed in constructor`() {
        val testData = listOf(testModule)
        val viewModel = ModulesViewModel(navController, testData)
        assertEquals(1, viewModel.modules.size)
        assertEquals(testModule, viewModel.modules[0])
    }

    @Test
    fun `select calls navigate with Module object`() {
        val testData = listOf(testModule)
        val viewModel = ModulesViewModel(navController, testData)
        viewModel.select(testModule)
        verify { navController.editModule(testModule) }
    }

    @Test
    fun `select should NOT navigate if module is not in the list`() {
        val viewModel = ModulesViewModel(navController, emptyList())
        val unknownModule = testModule
        viewModel.select(unknownModule)
        verify(exactly = 0) { navController.editModule(unknownModule) }
    }
}