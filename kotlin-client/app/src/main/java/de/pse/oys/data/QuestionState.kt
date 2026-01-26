package de.pse.oys.data

val Questions: List<Question> = listOf(
    // TODO
)

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