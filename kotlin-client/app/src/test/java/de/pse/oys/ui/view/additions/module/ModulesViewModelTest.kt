package de.pse.oys.ui.view.additions.module

import androidx.navigation.NavController
import de.pse.oys.data.facade.Identified
import de.pse.oys.data.facade.ModelFacade
import de.pse.oys.ui.navigation.editModule
import de.pse.oys.ui.view.TestUtils.createMockModuleData
import de.pse.oys.ui.view.TestUtils.randomUuid
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class ModulesViewModelTest {
    private val navController = mockk<NavController>(relaxed = true)
    private val modelFacade = mockk<ModelFacade>()

    private val moduleData = createMockModuleData()
    private val testId = randomUuid()
    private val testModule = Identified(id = testId, data = moduleData)

    @Before
    fun setup() {
        every { modelFacade.modules } returns mapOf(testId to moduleData)
    }

    @Test
    fun `available list matches the data from model facade`() {
        val viewModel = ModulesViewModel(modelFacade, navController)
        assertEquals(1, viewModel.modules.size)
        assertEquals(testModule, viewModel.modules[0])
    }

    @Test
    fun `select calls navigate with Module object`() {
        val viewModel = ModulesViewModel(modelFacade, navController)
        val moduleToSelect = viewModel.modules[0]

        viewModel.select(moduleToSelect)

        verify { navController.editModule(moduleToSelect) }
    }

    @Test
    fun `select should NOT navigate if module is not in the list`() {
        val viewModel = ModulesViewModel(modelFacade, navController)

        val unknownModule = Identified(moduleData, randomUuid())

        viewModel.select(unknownModule)

        verify(exactly = 0) { navController.editModule(unknownModule) }
    }
}