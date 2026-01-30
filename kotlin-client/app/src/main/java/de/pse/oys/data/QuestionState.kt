package de.pse.oys.data

import kotlinx.datetime.DayOfWeek

val Questions: List<Question> = listOf(
    question("min_unit_duration", 30..360 step 30),
    question("max_unit_duration", 30..360 step 30),
    question("max_day_load", 1..14),
    question("time_before_deadline", 1..7),
    question("preferred_pause_duration", 5..30 step 5),
    question<DayOfWeek>("preferred_study_days"),
    question("preferred_study_times", "MORNING", "FORENOON", "NOON", "AFTERNOON", "EVENING")
)

@Suppress("SameParameterValue")
private fun question(id: String, range: IntProgression) =
    Question(id, false, range.map { Answer(it.toString()) })

@Suppress("SameParameterValue")
private fun question(id: String, vararg answers: String) = Question(id, true, answers.map(::Answer))

@Suppress("SameParameterValue")
private inline fun <reified T : Enum<T>> question(id: String) =
    Question(id, true, enumValues<T>().map { Answer(it.name) })

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
    val isMultipleChoice: Boolean,
    val answers: List<Answer>,
)

data class Answer(val id: String)