package de.pse.oys.ui.view.additions.module

import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import de.pse.oys.data.api.RemoteAPI
import de.pse.oys.data.api.Response
import de.pse.oys.data.facade.ModelFacade
import de.pse.oys.data.facade.ModuleData
import de.pse.oys.data.facade.Priority
import de.pse.oys.ui.navigation.Main
import de.pse.oys.ui.navigation.main
import de.pse.oys.ui.theme.Blue
import de.pse.oys.ui.view.TestUtils.TEST_COLOR
import de.pse.oys.ui.view.TestUtils.TEST_DESC
import de.pse.oys.ui.view.TestUtils.TEST_PRIORITY
import de.pse.oys.ui.view.TestUtils.TEST_TITLE
import de.pse.oys.ui.view.TestUtils.createMockModuleData
import de.pse.oys.ui.view.TestUtils.randomUuid
import io.ktor.http.HttpStatusCode
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.uuid.Uuid

class CreateModuleViewModelTest {
    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setup() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private val api = mockk<RemoteAPI>(relaxed = true)
    private val navController = mockk<NavController>(relaxed = true)
    private val model = mockk<ModelFacade>(relaxed = true)

    @Test
    fun `initial state should be base values`() {
        val viewModel = CreateModuleViewModel(api, model, navController)

        assertEquals("", viewModel.title)
        assertEquals("", viewModel.description)
        assertEquals(Priority.MEDIUM, viewModel.priority)
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
    fun `registerNewModule should update model and navigate to main`() {
        val testId = Uuid.random()
        val testData = createMockModuleData()

        val modulesMap = mutableMapOf<Uuid, ModuleData>()
        every { model.modules } returns modulesMap

        val testVM = object : BaseCreateModuleViewModel(model, navController) {
            override val showDelete = false
            override fun submit() {
                modulesMap[testId] = testData
                navController.main()
            }

            override fun delete() {}
            override fun navigateBack() {}

            fun testRegister(m: ModuleData = testData) {
                registerNewModule(randomUuid(), m)
                submit()
            }
        }

        testVM.testRegister(ModuleData(TEST_TITLE, TEST_DESC, TEST_PRIORITY, TEST_COLOR))
        assertEquals(testData, modulesMap[testId])
        verify {
            navController.navigate(
                Main,
                any<androidx.navigation.NavOptionsBuilder.() -> Unit>()
            )
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `submit should call api and navigate to main when successful`() = runTest {
        val viewModel = CreateModuleViewModel(api, model, navController)

        viewModel.title = TEST_TITLE
        viewModel.description = TEST_DESC
        viewModel.priority = TEST_PRIORITY
        viewModel.color = TEST_COLOR

        val expectedData = createMockModuleData()
        coEvery { api.createModule(any()) } returns Response(randomUuid(), HttpStatusCode.OK.value)
        viewModel.submit()
        advanceUntilIdle()
        coVerify { api.createModule(expectedData) }
        verify {
            navController.navigate(
                Main,
                any<androidx.navigation.NavOptionsBuilder.() -> Unit>()
            )
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `submit should set error true when api fails`() = runTest {
        val viewModel = CreateModuleViewModel(api, model, navController)

        coEvery { api.createModule(any()) } returns Response(
            null,
            HttpStatusCode.InternalServerError.value
        )
        viewModel.title = TEST_TITLE
        viewModel.submit()
        advanceUntilIdle()
        assertTrue(viewModel.error)
    }
}