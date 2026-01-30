package de.pse.oys.ui.view.additions.module

import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import de.pse.oys.data.api.RemoteAPI
import de.pse.oys.data.facade.Identified
import de.pse.oys.data.facade.ModelFacade
import de.pse.oys.data.facade.ModuleData
import de.pse.oys.data.facade.Priority
import de.pse.oys.ui.navigation.main
import de.pse.oys.ui.theme.Blue
import de.pse.oys.ui.view.TestUtils.TEST_COLOR
import de.pse.oys.ui.view.TestUtils.TEST_DESC
import de.pse.oys.ui.view.TestUtils.TEST_PRIORITY
import de.pse.oys.ui.view.TestUtils.TEST_TITLE
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import kotlin.uuid.Uuid

class CreateModuleViewModelTest {
    private val api = mockk<RemoteAPI>(relaxed = true)
    private val navController = mockk<NavController>(relaxed = true)
    private val model = mockk<ModelFacade>(relaxed = true)

    @Test
    fun `initial state should be base values`() {
        val viewModel = CreateModuleViewModel(api, model, navController)

        assertEquals("", viewModel.title)
        assertEquals("", viewModel.description)
        assertEquals(Priority.NEUTRAL, viewModel.priority)
        assertEquals(Color.Unspecified, viewModel.color)
        assertFalse(viewModel.showDelete)
    }

    @Test
    fun `updating state should work`() {
        val viewModel = CreateModuleViewModel(api, model, navController)

        viewModel.title = TEST_TITLE
        viewModel.description = TEST_DESC
        viewModel.priority = TEST_PRIORITY
        viewModel.color = TEST_COLOR

        assertEquals(TEST_TITLE, viewModel.title)
        assertEquals(TEST_DESC, viewModel.description)
        assertEquals(Priority.HIGH, viewModel.priority)
        assertEquals(Blue, viewModel.color)
    }

    @Test
    fun `registerNewModule should update model and navigate`() {
        val testId = Uuid.random()
        val testData = ModuleData(TEST_TITLE, TEST_DESC, TEST_PRIORITY, TEST_COLOR)
        val testModule = Identified(testData, testId)

        val modulesMap = mutableMapOf<Uuid, ModuleData>()
        every { model.modules } returns modulesMap

        val testVM = object : BaseCreateModuleViewModel(model, navController) {
            override val showDelete = false
            override fun submit() {}
            override fun delete() {}
            fun testRegister(m: Identified<ModuleData>) = registerNewModule(m)
        }

        testVM.testRegister(testModule)
        assertEquals(testData, modulesMap[testId])
        verify { navController.main() }
    }
}