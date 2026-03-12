package de.pse.oys.ui.view.additions.task

import androidx.navigation.NavController
import de.pse.oys.data.api.RemoteAPI
import de.pse.oys.data.api.Response
import de.pse.oys.data.facade.ExamTaskData
import de.pse.oys.data.facade.Identified
import de.pse.oys.data.facade.ModelFacade
import de.pse.oys.ui.navigation.Main
import de.pse.oys.ui.view.TestUtils.TEST_DATE
import de.pse.oys.ui.view.TestUtils.TEST_TITLE
import de.pse.oys.ui.view.TestUtils.createMockModuleData
import de.pse.oys.ui.view.TestUtils.randomUuid
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class EditTaskViewModelTest {
    private val api = mockk<RemoteAPI>(relaxed = true)
    private val navController = mockk<NavController>(relaxed = true)
    private val model = mockk<ModelFacade>(relaxed = true)
    private lateinit var viewModel: EditTaskViewModel
    private val testTaskId = randomUuid()

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setup() {
        Dispatchers.setMain(StandardTestDispatcher())
        every { model.modules } returns mapOf(randomUuid() to createMockModuleData())

        val existingTaskData = ExamTaskData(
            title = TEST_TITLE,
            module = Identified(createMockModuleData(), randomUuid()),
            weeklyTimeLoad = 10,
            examDate = TEST_DATE
        )
        val targetTask = Identified(existingTaskData, testTaskId)

        viewModel = EditTaskViewModel(api, model, targetTask, navController)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state should be filled with existing task data`() {
        assertEquals(TEST_TITLE, viewModel.title)
        assertEquals(TEST_TITLE, viewModel.module)
        assertEquals(TaskType.EXAM, viewModel.type)
        assertEquals(10, viewModel.weeklyTimeLoad)
        assertEquals(TEST_DATE, viewModel.examDate)
        assertTrue(viewModel.showDelete)
    }

    @Test
    fun `changing values should update state`() {
        val newTitle = "New Title"
        viewModel.title = newTitle
        assertEquals(newTitle, viewModel.title)

        viewModel.module = newTitle
        assertEquals(newTitle, viewModel.module)

        viewModel.weeklyTimeLoad = 15
        assertEquals(15, viewModel.weeklyTimeLoad)

        viewModel.type = TaskType.SUBMISSION
        assertEquals(TaskType.SUBMISSION, viewModel.type)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `submit should call update api and navigate to main`() = runTest {
        coEvery { api.updateTask(any()) } returns Response(randomUuid(), 200)


        viewModel.title = "Updated Task"
        viewModel.submit()
        advanceUntilIdle()
        coVerify {
            api.updateTask(match { it.id == testTaskId && it.data.title == "Updated Task" })
        }
        verify {
            navController.navigate(
                Main,
                any<androidx.navigation.NavOptionsBuilder.() -> Unit>()
            )
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `delete should call delete api and navigate to main`() = runTest {
        coEvery { api.deleteTask(testTaskId) } returns Response(Unit, 200)

        viewModel.delete()
        advanceUntilIdle()
        coVerify { api.deleteTask(testTaskId) }
        verify {
            navController.navigate(
                Main,
                any<androidx.navigation.NavOptionsBuilder.() -> Unit>()
            )
        }
    }
}