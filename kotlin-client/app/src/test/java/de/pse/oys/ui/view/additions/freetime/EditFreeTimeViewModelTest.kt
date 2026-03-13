package de.pse.oys.ui.view.additions.freetime

import androidx.navigation.NavController
import de.pse.oys.data.api.RemoteAPI
import de.pse.oys.data.api.Response
import de.pse.oys.data.facade.Identified
import de.pse.oys.data.facade.ModelFacade
import de.pse.oys.ui.navigation.Main
import de.pse.oys.ui.view.TestUtils.TEST_DATE
import de.pse.oys.ui.view.TestUtils.TEST_DATE_ALTERNATIVE
import de.pse.oys.ui.view.TestUtils.TEST_TIME
import de.pse.oys.ui.view.TestUtils.TEST_TIME_ALTERNATIVE
import de.pse.oys.ui.view.TestUtils.TEST_TITLE
import de.pse.oys.ui.view.TestUtils.createMockFreeTimeData
import de.pse.oys.ui.view.TestUtils.randomUuid
import io.ktor.http.HttpStatusCode
import io.mockk.coEvery
import io.mockk.coVerify
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

class EditFreeTimeViewModelTest {
    private val api = mockk<RemoteAPI>(relaxed = true)
    private val navController = mockk<NavController>(relaxed = true)
    private val model = mockk<ModelFacade>(relaxed = true)
    private lateinit var viewModel: EditFreeTimeViewModel

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setup() {
        Dispatchers.setMain(StandardTestDispatcher())
        val existingData = createMockFreeTimeData()
        val targetFreeTime = Identified(existingData, randomUuid())
        viewModel = EditFreeTimeViewModel(api, model, targetFreeTime, navController)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state should be filled with target freeTime data`() {
        assertEquals(TEST_TITLE, viewModel.title)
        assertEquals(TEST_DATE, viewModel.date)
        assertEquals(TEST_TIME, viewModel.start)
        assertEquals(TEST_TIME, viewModel.end)
        assertFalse(viewModel.weekly)
        assertTrue(viewModel.showDelete)
    }

    @Test
    fun `editing fields should change the state`() {
        val newTitle = "New-Title"
        viewModel.title = newTitle
        assertEquals(newTitle, viewModel.title)

        viewModel.date = TEST_DATE_ALTERNATIVE
        assertEquals(TEST_DATE_ALTERNATIVE, viewModel.date)

        viewModel.start = TEST_TIME_ALTERNATIVE
        assertEquals(TEST_TIME_ALTERNATIVE, viewModel.start)

        viewModel.end = TEST_TIME_ALTERNATIVE
        assertEquals(TEST_TIME_ALTERNATIVE, viewModel.end)

        viewModel.weekly = true
        assertTrue(viewModel.weekly)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `submit in EditViewModel should call update api and navigate to main`() = runTest {
        val testId = randomUuid()
        val testData = createMockFreeTimeData()
        val target = Identified(testData, testId)
        val editViewModel = EditFreeTimeViewModel(api, model, target, navController)

        coEvery { api.updateFreeTime(any()) } returns Response(Unit, HttpStatusCode.OK.value)
        val newTitle = "New Title"
        editViewModel.title = newTitle
        editViewModel.submit()
        advanceUntilIdle()
        coVerify {
            api.updateFreeTime(match {
                it.id == testId && it.data.title == newTitle
            })
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
    fun `delete in EditViewModel should call api delete and navigate to main`() = runTest {
        val testId = randomUuid()
        val target = Identified(createMockFreeTimeData(), testId)
        val editViewModel = EditFreeTimeViewModel(api, model, target, navController)

        coEvery { api.deleteFreeTime(testId) } returns Response(Unit, HttpStatusCode.OK.value)
        editViewModel.delete()
        advanceUntilIdle()
        coVerify { api.deleteFreeTime(testId) }
        verify {
            navController.navigate(
                Main,
                any<androidx.navigation.NavOptionsBuilder.() -> Unit>()
            )
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `delete should set error true when api fails`() = runTest {
        coEvery { api.deleteFreeTime(any()) } returns Response(Unit, HttpStatusCode.InternalServerError.value)

        viewModel.delete()
        advanceUntilIdle()

        assertTrue(viewModel.error)
    }

    @Test
    fun `navigate back should pop back stack`() {
        viewModel.navigateBack()
        verify { navController.popBackStack() }
    }
}