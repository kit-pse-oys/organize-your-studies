package de.pse.oys.data

val Questions: List<Question> = listOf(
    TODO()
)

class QuestionState {
    fun selected(question: Question, answer: Answer): Boolean {
        TODO()
    }

    fun select(question: Question, answer: Answer) {
        TODO()
    }
}

data class Question(
    val id: Int,
    val isMultipleChoice: Boolean,
    val answers: List<Answer>,
)

data class Answer(val id: Int)