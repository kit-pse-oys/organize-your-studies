package de.pse.oys.ui.view.additions.freetime

import androidx.navigation.NavController
import de.pse.oys.data.api.RemoteAPI
import de.pse.oys.data.api.Response
import de.pse.oys.data.facade.FreeTimeData
import de.pse.oys.data.facade.ModelFacade
import de.pse.oys.ui.navigation.Main
import de.pse.oys.ui.navigation.main
import de.pse.oys.ui.view.TestUtils.TEST_DATE_ALTERNATIVE
import de.pse.oys.ui.view.TestUtils.TEST_TIME_ALTERNATIVE
import de.pse.oys.ui.view.TestUtils.TEST_TITLE
import de.pse.oys.ui.view.TestUtils.createMockFreeTimeData
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
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.time.Clock
import kotlin.uuid.Uuid


class CreateFreeTimeViewModelTest {
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
        val viewModel = CreateFreeTimeViewModel(api, model, navController)

        assertEquals("", viewModel.title)
        assertEquals(
            Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date, viewModel.date
        )
        assertEquals(LocalTime(0, 0), viewModel.start)
        assertEquals(LocalTime(0, 0), viewModel.end)
        assertFalse(viewModel.weekly)
        assertFalse(viewModel.showDelete)
    }

    @Test
    fun `updating state should work`() {
        val viewModel = CreateFreeTimeViewModel(api, model, navController)

        viewModel.title = TEST_TITLE
        viewModel.date = TEST_DATE_ALTERNATIVE
        viewModel.start = TEST_TIME_ALTERNATIVE
        viewModel.end = TEST_TIME_ALTERNATIVE
        viewModel.weekly = true

        assertEquals(TEST_TITLE, viewModel.title)
        assertEquals(TEST_DATE_ALTERNATIVE, viewModel.date)
        assertEquals(TEST_TIME_ALTERNATIVE, viewModel.start)
        assertEquals(TEST_TIME_ALTERNATIVE, viewModel.end)
        assertTrue(viewModel.weekly)
    }

    @Test
    fun `registerNewFreeTime should update model and navigate to main`() {
        val testId = randomUuid()
        val testData = createMockFreeTimeData()

        val freeTimesMap = mutableMapOf<Uuid, FreeTimeData>()
        every { model.freeTimes } returns freeTimesMap

        val testVM = object : BaseCreateFreeTimeViewModel(model, navController) {
            override val showDelete = false
            override fun submit() {
                freeTimesMap[testId] = testData
                navController.main()
            }

            override fun delete() {}
            override fun navigateBack() {
                navController.popBackStack()
            }

            fun testRegister(f: FreeTimeData) {
                registerNewFreeTime(randomUuid(), f)
                submit()
            }
        }

        testVM.testRegister(testData)
        assertEquals(testData, freeTimesMap[testId])
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
        val viewModel = CreateFreeTimeViewModel(api, model, navController)
        val testData = FreeTimeData(
            TEST_TITLE, TEST_DATE_ALTERNATIVE, TEST_TIME_ALTERNATIVE, TEST_TIME_ALTERNATIVE, false
        )

        coEvery { api.createFreeTime(any()) } returns Response(
            randomUuid(), HttpStatusCode.OK.value
        )
        viewModel.title = TEST_TITLE
        viewModel.date = TEST_DATE_ALTERNATIVE
        viewModel.start = TEST_TIME_ALTERNATIVE
        viewModel.end = TEST_TIME_ALTERNATIVE
        viewModel.submit()
        advanceUntilIdle()
        coVerify { api.createFreeTime(testData) }
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
        val viewModel = CreateFreeTimeViewModel(api, model, navController)

        coEvery { api.createFreeTime(any()) } returns Response(
            null, HttpStatusCode.InternalServerError.value
        )
        viewModel.title = TEST_TITLE
        viewModel.submit()
        advanceUntilIdle()
        assertTrue(viewModel.error)
    }
}