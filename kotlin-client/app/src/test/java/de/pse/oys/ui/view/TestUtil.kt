package de.pse.oys.ui.view

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
    val TEST_PRIORITY = Priority.NEUTRAL

    // Feste Daten weit in der Zukunft verhindern "Past Date" Fehler
    val TEST_DATE = LocalDate(2030, 1, 1)
    val TEST_DATE_FUTURE = LocalDate(2030, 1, 2)

    // Unterschiedliche Zeiten für Start und Ende (wichtig für Validierung)
    val TEST_TIME = LocalTime(10, 0)
    val TEST_TIME_FUTURE = LocalTime(12, 0)

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