package de.pse.oys.ui.view.additions.task

import androidx.navigation.NavController
import de.pse.oys.data.facade.ExamTaskData
import de.pse.oys.data.facade.Identified
import de.pse.oys.data.facade.ModelFacade
import de.pse.oys.data.facade.OtherTaskData
import de.pse.oys.data.facade.SubmissionTaskData
import de.pse.oys.ui.navigation.editTask
import de.pse.oys.ui.view.TestUtils.TEST_DATE
import de.pse.oys.ui.view.TestUtils.TEST_TITLE
import de.pse.oys.ui.view.TestUtils.createMockModuleData
import de.pse.oys.ui.view.TestUtils.randomUuid
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import junit.framework.TestCase.assertEquals
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.junit.Before
import org.junit.Test
import kotlin.time.Clock
import kotlin.uuid.Uuid

class TasksViewModelTest {
    private val navController = mockk<NavController>(relaxed = true)
    private val modelFacade = mockk<ModelFacade>()
    private val testModuleData = createMockModuleData()
    private val testModuleId = randomUuid()
    private val testModule = Identified(id = testModuleId, data = testModuleData)

    private val otherTaskTestData = OtherTaskData(
        title = TEST_TITLE,
        module = testModule,
        weeklyTimeLoad = 120,
        start = TEST_DATE,
        end = TEST_DATE
    )
    private val testTaskId = randomUuid()
    private val testTask = Identified(id = testTaskId, data = otherTaskTestData)

    @Before
    fun setup() {
        every { modelFacade.tasks } returns mapOf(testTaskId to otherTaskTestData)
    }

    @Test
    fun `viewModel should correctly map task from facade`() {
        val viewModel = TasksViewModel(modelFacade, navController)
        assertEquals(1, viewModel.tasks.size)
        assertEquals(testTask, viewModel.tasks[0])
    }

    @Test
    fun `viewModel should preserve specific subtypes after mapping`() {
        val testExamId = Uuid.random()
        val testExamData = ExamTaskData(
            title = TEST_TITLE,
            module = testModule,
            weeklyTimeLoad = 0,
            examDate = TEST_DATE
        )
        val testSubId = Uuid.random()
        val testSubData = SubmissionTaskData(
            title = TEST_TITLE,
            module = testModule,
            weeklyTimeLoad = 100,
            firstDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
            cycle = 1
        )
        every { modelFacade.tasks } returns mapOf(
            testTaskId to otherTaskTestData,
            testExamId to testExamData,
            testSubId to testSubData
        )

        val viewModel = TasksViewModel(modelFacade, navController)

        assertEquals(3, viewModel.tasks.size)
        assert(viewModel.tasks.any { it.data is OtherTaskData })
        assert(viewModel.tasks.any { it.data is ExamTaskData })
        assert(viewModel.tasks.any { it.data is SubmissionTaskData })
    }

    @Test
    fun `select should navigate to edit screen if task is in list`() {
        val viewModel = TasksViewModel(modelFacade, navController)
        val taskToSelect = viewModel.tasks[0]

        viewModel.select(taskToSelect)
        verify { navController.editTask(taskToSelect) }
    }

    @Test
    fun `select should NOT navigate if task is unknown`() {
        val viewModel = TasksViewModel(modelFacade, navController)
        val unknownTask = Identified(id = randomUuid(), data = otherTaskTestData)

        viewModel.select(unknownTask)

        verify(exactly = 0) { navController.editTask(any()) }
    }
}