package de.pse.oys.data

import de.pse.oys.R

data class RatingQuestionData(
    val id: String, val textRes: Int, val labelsRes: List<Int>
)

val RatingQuestions = listOf(
    RatingQuestionData(
        id = "goal_completion", textRes = R.string.goal_completion_question, labelsRes = listOf(
            R.string.rating_not_at_all,
            R.string.rating_very_little,
            R.string.rating_a_little,
            R.string.rating_a_lot,
            R.string.rating_very_much
        )
    ), RatingQuestionData(
        id = "duration", textRes = R.string.duration_question, labelsRes = listOf(
            R.string.rating_way_to_short,
            R.string.rating_too_short,
            R.string.rating_just_right,
            R.string.rating_too_long,
            R.string.rating_way_to_long
        )
    ), RatingQuestionData(
        id = "motivation", textRes = R.string.motivation_question, labelsRes = listOf(
            R.string.rating_not_at_all,
            R.string.rating_very_little,
            R.string.rating_a_little,
            R.string.rating_a_lot,
            R.string.rating_very_much
        )
    )
)