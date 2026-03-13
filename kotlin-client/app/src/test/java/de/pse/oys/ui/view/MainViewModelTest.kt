package de.pse.oys.ui.view

import androidx.compose.ui.graphics.Color
import de.pse.oys.data.api.RemoteAPI
import de.pse.oys.data.api.Response
import de.pse.oys.data.ensureFreeTimes
import de.pse.oys.data.ensureUnits
import de.pse.oys.data.facade.FreeTimeData
import de.pse.oys.data.facade.ModelFacade
import de.pse.oys.data.facade.StepData
import de.pse.oys.ui.navigation.Additions
import de.pse.oys.ui.navigation.Menu
import de.pse.oys.ui.navigation.menu
import de.pse.oys.ui.view.TestUtils.TEST_TITLE
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit4.MockKRule
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.time.Clock
import kotlin.uuid.Uuid

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {

    @get:Rule
    val mockkRule = MockKRule(this)

    private val testDispatcher = StandardTestDispatcher()

    @MockK
    lateinit var api: RemoteAPI

    @MockK(relaxed = true)
    lateinit var model: ModelFacade

    @MockK(relaxed = true)
    lateinit var navController: androidx.navigation.NavController

    private lateinit var viewModel: MainViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        mockkStatic("de.pse.oys.data.ModelApiInterfaceKt")
        mockkStatic("de.pse.oys.ui.navigation.MainKt")

        every { model.steps } returns null
        every { model.freeTimes } returns null

        coEvery { model.ensureUnits(api) } returns Response(mapOf(), 200)
        coEvery { model.ensureFreeTimes(api) } returns Response(mapOf(), 200)

        viewModel = MainViewModel(api, model, navController)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `MapsToMenu should call navController extension`() {
        viewModel.navigateToMenu()
        verify { navController.menu() }
    }

    @Test
    fun `error state should be set when ensureUnits fails`() = runTest {
        coEvery { model.ensureUnits(api) } returns Response(null, 500)

        viewModel = MainViewModel(api, model, navController)
        advanceUntilIdle()

        assert(viewModel.error)
    }

    @Test
    fun `moveUnitAutomatically should trigger reload on success`() = runTest {
        val uuid = Uuid.random()
        coEvery { api.moveUnitAutomatically(uuid) } returns Response(mockk(relaxed = true), 200)

        coEvery { model.ensureUnits(api) } returns Response(mapOf(), 200)
    }

    @Test
    fun `updateUnits should correctly map StepData to PlannedUnit`() = runTest {
        val uuid = Uuid.random()
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date

        val mockStepData = StepData(
            task = mockk(relaxed = true) {
                every { data.title } returns TEST_TITLE
                every { data.module.data.title } returns TEST_TITLE
                every { data.module.data.color } returns Color.Red
            },
            date = today,
            start = LocalTime(10, 0),
            end = LocalTime(12, 0)
        )

        val steps = mapOf(today.dayOfWeek to mapOf(uuid to mockStepData))

        coEvery { model.ensureUnits(api) } returns Response(steps, 200)
        coEvery { model.ensureFreeTimes(api) } returns Response(mapOf(), 200)

        viewModel = MainViewModel(api, model, navController)
        advanceUntilIdle()

        assertEquals(1, viewModel.unitsToday.size)
        val unit = viewModel.unitsToday.first()
        assertEquals(TEST_TITLE, unit.title)
        assertEquals(Color.Red, unit.color)
        assertEquals(LocalTime(10, 0), unit.start)
    }

    @Test
    fun `unitsTomorrow should contain units of the next day`() = runTest {
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        val tomorrow = today.plus(DatePeriod(days = 1))
        val tomorrowDay = tomorrow.dayOfWeek
        val uuid = Uuid.random()
        val mockModuleData = mockk<de.pse.oys.data.facade.ModuleData> {
            every { title } returns TEST_TITLE
            every { color } returns Color.Blue
        }

        val mockTaskData = mockk<de.pse.oys.data.facade.TaskData> {
            every { title } returns TEST_TITLE
            every { module.data } returns mockModuleData
        }

        val mockStepData = mockk<StepData> {
            every { task.data } returns mockTaskData
            every { date } returns tomorrow
            every { start } returns LocalTime(10, 0)
            every { end } returns LocalTime(12, 0)
        }

        val steps = mapOf(tomorrowDay to mapOf(uuid to mockStepData))
        coEvery { model.ensureUnits(api) } returns Response(steps, 200)
        coEvery { model.ensureFreeTimes(api) } returns Response(mapOf(), 200)

        viewModel = MainViewModel(api, model, navController)
        advanceUntilIdle()

        assertEquals(1, viewModel.unitsTomorrow.size)
        assertEquals(TEST_TITLE, viewModel.unitsTomorrow.first().title)
    }

    @Test
    fun `unitsTomorrow should be sorted by start time`() = runTest {
        val tomorrow = Clock.System.now()
            .toLocalDateTime(TimeZone.currentSystemDefault()).date.plus(DatePeriod(days = 1))

        val unitLate = createMockStep(tomorrow, LocalTime(14, 0))
        val unitEarly = createMockStep(tomorrow, LocalTime(8, 0))

        val steps = mapOf(
            tomorrow.dayOfWeek to mapOf(
                Uuid.random() to unitLate,
                Uuid.random() to unitEarly
            )
        )
        coEvery { model.ensureUnits(api) } returns Response(steps, 200)

        viewModel = MainViewModel(api, model, navController)
        advanceUntilIdle()

        assertEquals(LocalTime(8, 0), viewModel.unitsTomorrow[0].start)
        assertEquals(LocalTime(14, 0), viewModel.unitsTomorrow[1].start)
    }

    private fun createMockStep(date: LocalDate, start: LocalTime): StepData = mockk {
        every { this@mockk.date } returns date
        every { this@mockk.start } returns start
        every { end } returns LocalTime((start.hour + 1) % 24, start.minute)
        every { task.data.title } returns "Task"
        every { task.data.module.data.title } returns "Module"
        every { task.data.module.data.color } returns Color.Blue
    }

    @Test
    fun `updateFreeTimes should filter out non-weekly free times outside current week`() = runTest {
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        val farFutureDate = today.plus(DatePeriod(months = 1))

        val uuid = Uuid.random()
        val futureFreeTime = FreeTimeData(
            title = TEST_TITLE,
            startTime = LocalTime(9, 0),
            endTime = LocalTime(17, 0),
            date = farFutureDate,
            weekly = false
        )

        coEvery { model.ensureFreeTimes(api) } returns Response(mapOf(uuid to futureFreeTime), 200)
        coEvery { model.ensureUnits(api) } returns Response(mapOf(), 200)

        viewModel = MainViewModel(api, model, navController)
        advanceUntilIdle()

        assert(viewModel.freeTimesToday.isEmpty())
        assert(viewModel.freeTimes.isEmpty())
    }

    @Test
    fun `marksAsFinished should calculate duration and call api`() = runTest {
        val uuid = Uuid.random()
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date

        val startTime = LocalTime(9, 0)
        val endTime = LocalTime(11, 0)

        val mockStepData = mockk<StepData>(relaxed = true) {
            every { task.data.title } returns TEST_TITLE
            every { task.data.module.data.title } returns TEST_TITLE
            every { task.data.module.data.color } returns Color.Blue
            every { date } returns today
            every { start } returns startTime
            every { end } returns endTime
        }

        val steps = mapOf(today.dayOfWeek to mapOf(uuid to mockStepData))
        coEvery { model.ensureUnits(api) } returns Response(steps, 200)
        coEvery { model.ensureFreeTimes(api) } returns Response(mapOf(), 200)

        viewModel = MainViewModel(api, model, navController)
        advanceUntilIdle()

        val plannedUnit = viewModel.unitsToday.first()

        coEvery { api.markUnitFinished(uuid, any()) } returns Response(mockk(relaxed = true), 200)

        viewModel.marksAsFinished(plannedUnit)
        advanceUntilIdle()

        coVerify {
            api.markUnitFinished(
                eq(uuid),
                any<kotlin.time.Duration>()
            )
        }
    }

    @Test
    fun `should set error state when api returns server error`() = runTest {
        coEvery { model.ensureUnits(api) } returns Response(null, 500)

        viewModel = MainViewModel(api, model, navController)
        advanceUntilIdle()

        assert(viewModel.error)
    }

    @Test
    fun `navigate to menu should call menu extension`() {
        mockkStatic("de.pse.oys.ui.navigation.MainKt")

        viewModel.navigateToMenu()
        verify {
            navController.navigate(
                route = eq(Menu),
                navOptions = any(),
                navigatorExtras = any()
            )
        }
    }

    @Test
    fun `navigate to additions should call additions extension`() {
        mockkStatic("de.pse.oys.ui.navigation.MainKt")

        viewModel.navigateToAdditions()
        verify {
            navController.navigate(
                route = eq(Additions),
                navOptions = isNull(),
                navigatorExtras = any()
            )
        }
    }
}