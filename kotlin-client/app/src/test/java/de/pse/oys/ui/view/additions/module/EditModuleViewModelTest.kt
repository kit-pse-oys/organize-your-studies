package de.pse.oys.ui.view.additions.module

import androidx.navigation.NavController
import de.pse.oys.data.api.RemoteAPI
import de.pse.oys.data.api.Response
import de.pse.oys.data.facade.Identified
import de.pse.oys.data.facade.ModelFacade
import de.pse.oys.data.facade.Priority
import de.pse.oys.ui.navigation.main
import de.pse.oys.ui.theme.LightBlue
import de.pse.oys.ui.view.TestUtils
import de.pse.oys.ui.view.TestUtils.TEST_DESC
import de.pse.oys.ui.view.TestUtils.TEST_TITLE
import de.pse.oys.ui.view.TestUtils.createMockModuleData
import de.pse.oys.ui.view.TestUtils.randomUuid
import io.ktor.http.HttpStatusCode
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class EditModuleViewModelTest {
    private val api = mockk<RemoteAPI>(relaxed = true)
    private val navController = mockk<NavController>(relaxed = true)
    private val model = mockk<ModelFacade>(relaxed = true)
    private lateinit var viewModel: EditModuleViewModel

    @Before
    fun setup() {
        val existingData = createMockModuleData()
        val targetModule = Identified(existingData, randomUuid())
        viewModel = EditModuleViewModel(api, model, targetModule, navController)
    }

    @Test
    fun `initial state should be filled with target module data`() {
        assertEquals(TEST_TITLE, viewModel.title)
        assertEquals(TEST_DESC, viewModel.description)
        assertEquals(TestUtils.TEST_PRIORITY, viewModel.priority)
        assertEquals(TestUtils.TEST_COLOR, viewModel.color)
        assertTrue(viewModel.showDelete)
    }

    @Test
    fun `editing fields should change the state`() {
        val newTitle = "New-Title"
        viewModel.title = newTitle
        assertEquals(newTitle, viewModel.title)

        val newDesc = "New-Description"
        viewModel.description = newDesc
        assertEquals(newDesc, viewModel.description)

        viewModel.priority = Priority.LOW
        assertEquals(Priority.LOW, viewModel.priority)

        viewModel.color = LightBlue
        assertEquals(LightBlue, viewModel.color)
    }

    @Test
    fun `submit in EditViewModel should call update api and navigate to main`() = runTest {
        val testId = randomUuid()
        val testData = createMockModuleData()
        val target = Identified(testData, testId)
        val editViewModel = EditModuleViewModel(api, model, target, navController)

        coEvery { api.updateModule(any()) } returns Response(Unit, HttpStatusCode.OK.value)
        val newTitle = "New Title"
        editViewModel.title = newTitle
        editViewModel.submit()
        coVerify {
            api.updateModule(match {
                it.id == testId && it.data.title == newTitle
            })
        }
        verify { navController.main() }
    }

    @Test
    fun `delete in EditViewModel should call api delete and navigate to main`() = runTest {
        val testId = randomUuid()
        val target = Identified(createMockModuleData(), testId)
        val editViewModel = EditModuleViewModel(api, model, target, navController)

        coEvery { api.deleteModule(testId) } returns Response(Unit, HttpStatusCode.OK.value)
        editViewModel.delete()
        coVerify { api.deleteModule(testId) }
        verify { navController.main() }
    }
}