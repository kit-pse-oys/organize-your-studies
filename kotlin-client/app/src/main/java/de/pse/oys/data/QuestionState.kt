package de.pse.oys.data

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import de.pse.oys.R
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.toJavaDayOfWeek
import java.time.format.TextStyle
import java.util.Locale

val Questions: List<Question> = listOf(
    question(
        "min_unit_duration",
        R.string.min_unit_duration,
        30..360 step 30,
        R.string.minutes_template
    ),
    question(
        "max_unit_duration",
        R.string.max_unit_duration,
        30..360 step 30,
        R.string.minutes_template
    ),
//    question(
//        "max_day_load",
//        R.string.max_day_load,
//        1..14,
//        R.string.amount_template
//    ),
    question(
        "time_before_deadlines",
        R.string.time_before_deadline,
        0..7,
        R.string.days_template
    ),
    question(
        "preferred_pause_duration",
        R.string.preferred_pause_duration,
        5..30 step 5,
        R.string.minutes_template
    ),
    question<DayOfWeek>("preferred_study_days", R.string.preferred_study_days) {
        it.toJavaDayOfWeek().getDisplayName(TextStyle.FULL_STANDALONE, Locale.getDefault())
    },
    question(
        "preferred_study_times",
        R.string.preferred_study_times,
        "MORNING" to R.string.MORNING,
        "FORENOON" to R.string.FORENOON,
        "NOON" to R.string.NOON,
        "AFTERNOON" to R.string.AFTERNOON,
        "EVENING" to R.string.EVENING,
    )
)

@Suppress("SameParameterValue")
private fun question(
    id: String,
    @StringRes resId: Int,
    range: IntProgression,
    @StringRes answerTemplate: Int
) =
    Question(
        id,
        { stringResource(resId) },
        false,
        range.map { Answer(it.toString()) { stringResource(answerTemplate, it) } })

@Suppress("SameParameterValue")
private fun question(id: String, @StringRes resId: Int, vararg answers: Pair<String, Int>) =
    Question(id, { stringResource(resId) }, true, answers.map { (id, localisation) ->
        Answer(id) { stringResource(localisation) }
    })

@Suppress("SameParameterValue")
private inline fun <reified T : Enum<T>> question(
    id: String,
    @StringRes resId: Int,
    crossinline answerLocalisation: (T) -> String
) =
    Question(
        id,
        { stringResource(resId) },
        true,
        enumValues<T>().map { Answer(it.name) { answerLocalisation(it) } })

class QuestionState(
    val questions: List<Question> = Questions,
    val answers: Array<BooleanArray> = Array(questions.size) {
        BooleanArray(questions[it].answers.size)
    }
) {
    fun selected(question: Question, answer: Answer): Boolean {
        return answers[question.index][question % answer]
    }

    fun select(question: Question, answer: Answer) {
        val answers = answers[question.index]
        if (question.isMultipleChoice) {
            answers[question % answer] = !answers[question % answer]
        } else {
            answers.fill(false)
            answers[question % answer] = true
        }
    }

    private val Question.index: Int
        get() = questions.indexOf(this)


    private operator fun Question.rem(answer: Answer): Int {
        return answers.indexOf(answer)
    }
}

data class Question(
    val id: String,
    val localisation: @Composable () -> String,
    val isMultipleChoice: Boolean,
    val answers: List<Answer>,
)

data class Answer(
    val id: String,
    val localisation: @Composable () -> String,
)