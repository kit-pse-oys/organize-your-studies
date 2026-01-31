package de.pse.oys.ui.view.additions.task

import androidx.navigation.NavController
import de.pse.oys.data.api.RemoteAPI
import de.pse.oys.data.facade.ModelFacade
import de.pse.oys.ui.view.TestUtils.TEST_DATE_FUTURE
import de.pse.oys.ui.view.TestUtils.TEST_TITLE
import de.pse.oys.ui.view.TestUtils.createMockModuleData
import de.pse.oys.ui.view.TestUtils.randomUuid
import io.mockk.every
import io.mockk.mockk
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Before
import org.junit.Test
import kotlin.time.Clock

class CreateTaskViewModelTest {
    private val api = mockk<RemoteAPI>(relaxed = true)
    private val navController = mockk<NavController>(relaxed = true)
    private val model = mockk<ModelFacade>(relaxed = true)
    private lateinit var viewModel: CreateTaskViewModel

    @Before
    fun setup() {
        val mockModules = mapOf(
            randomUuid() to createMockModuleData()
        )
        every { model.modules } returns mockModules

        viewModel = CreateTaskViewModel(api, model, navController)
    }

    @Test
    fun `initial state should be base values`() {
        assertEquals(1, viewModel.availableModules.size)
        assertEquals(TEST_TITLE, viewModel.availableModules[0])
        assertEquals("", viewModel.module)

        assertEquals("", viewModel.title)
        assertEquals(0, viewModel.weeklyTimeLoad)
        assertFalse(viewModel.sendNotification)
        assertEquals(TaskType.EXAM, viewModel.type)

        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        assertEquals(today, viewModel.examDate)
        assertEquals(1, viewModel.submissionCycle)
        assertEquals(today, viewModel.submissionDate.date)
        assertEquals(today, viewModel.start)
        assertEquals(today, viewModel.end)

        assertFalse(viewModel.showDelete)
    }

    @Test
    fun `updating task should work`() {
        viewModel.title = TEST_TITLE
        viewModel.module = TEST_TITLE
        viewModel.weeklyTimeLoad = 90
        viewModel.sendNotification = true
        assertEquals(TEST_TITLE, viewModel.title)
        assertEquals(TEST_TITLE, viewModel.module)
        assertEquals(90, viewModel.weeklyTimeLoad)
        assertEquals(true, viewModel.sendNotification)

        viewModel.type = TaskType.EXAM
        viewModel.examDate = TEST_DATE_FUTURE
        assertEquals(TEST_DATE_FUTURE, viewModel.examDate)
        assertEquals(TaskType.EXAM, viewModel.type)

        viewModel.type = TaskType.SUBMISSION
        viewModel.submissionCycle = 3
        val futureDateTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        viewModel.submissionDate = futureDateTime
        assertEquals(futureDateTime, viewModel.submissionDate)
        assertEquals(3, viewModel.submissionCycle)
        assertEquals(TaskType.SUBMISSION, viewModel.type)

        viewModel.type = TaskType.OTHER
        viewModel.start = TEST_DATE_FUTURE
        viewModel.end = TEST_DATE_FUTURE
        assertEquals(TaskType.OTHER, viewModel.type)
        assertEquals(TEST_DATE_FUTURE, viewModel.start)
        assertEquals(TEST_DATE_FUTURE, viewModel.end)
    }

    @Test
    fun `end date should not be before start date`() {
        viewModel.type = TaskType.OTHER
        viewModel.start = TEST_DATE_FUTURE
        val pastDate = LocalDate(2024, 1, 1)
        viewModel.end = pastDate
        assertNotEquals(pastDate, viewModel.end)
    }

    @Test
    fun `exam date or submission date should not be in the past`() {
        val pastDate = LocalDate(2024, 1, 1)
        viewModel.type = TaskType.EXAM
        viewModel.examDate = pastDate
        assertNotEquals(pastDate, viewModel.examDate)

        val pastDateTime = LocalDateTime(2024, 1, 1, 1, 1)
        viewModel.type = TaskType.SUBMISSION
        viewModel.submissionDate = pastDateTime
        assertNotEquals(pastDateTime, viewModel.submissionDate)
    }
}