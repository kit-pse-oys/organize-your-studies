package de.pse.oys.ui.view

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import de.pse.oys.data.facade.FreeTimeData
import de.pse.oys.data.facade.ModuleData
import de.pse.oys.data.facade.Priority
import de.pse.oys.ui.theme.Blue
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.uuid.Uuid

object TestUtils {
    const val TEST_TITLE = "Test-Title"
    const val TEST_DESC = "Test-Description"
    val TEST_COLOR = Blue
    val TEST_PRIORITY = Priority.HIGH

    private fun now() = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())

    val TEST_DATE: LocalDate get() = now().date
    val TEST_DATE_FUTURE: LocalDate get() = now().date.plus(1, DateTimeUnit.DAY)

    val TEST_TIME: LocalTime get() = LocalTime(now().hour, 0)
    val TEST_TIME_FUTURE: LocalTime get() = LocalTime((now().hour + 1) % 24, 0)

    fun randomUuid() = Uuid.random()

    fun createMockModuleData() = ModuleData(
        title = TEST_TITLE,
        description = TEST_DESC,
        priority = TEST_PRIORITY,
        color = TEST_COLOR
    )

    fun createMockFreeTimeData() = FreeTimeData(
        title = TEST_TITLE,
        date = TEST_DATE,
        start = TEST_TIME,
        end = TEST_TIME,
        weekly = false
    )
}