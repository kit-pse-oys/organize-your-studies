package de.pse.oys.data

import de.pse.oys.R

enum class RatingAspect(
    val textRes: Int,
    val labelsRes: List<Int>
) {
    GOAL(
        R.string.goal_completion_question, listOf(
            R.string.rating_not_at_all,
            R.string.rating_very_little,
            R.string.rating_a_little,
            R.string.rating_a_lot,
            R.string.rating_very_much
        )
    ),
    DURATION(
        R.string.duration_question, listOf(
            R.string.rating_way_to_short,
            R.string.rating_too_short,
            R.string.rating_just_right,
            R.string.rating_too_long,
            R.string.rating_way_to_long
        )
    ),
    MOTIVATION(
        R.string.motivation_question, listOf(
            R.string.rating_not_at_all,
            R.string.rating_very_little,
            R.string.rating_a_little,
            R.string.rating_a_lot,
            R.string.rating_very_much
        )
    )
}