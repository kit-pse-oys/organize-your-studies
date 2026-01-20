package de.pse.oys.data

val Questions: List<Question> = listOf(
    TODO()
)

class QuestionState(
    val answers: Array<BooleanArray> = Array(Questions.size) {
        BooleanArray(Questions[it].answers.size)
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
        get() = Questions.indexOf(this)


    private operator fun Question.rem(answer: Answer): Int {
        return answers.indexOf(answer)
    }
}

data class Question(
    val id: Int,
    val isMultipleChoice: Boolean,
    val answers: List<Answer>,
)

data class Answer(val id: Int)