package de.pse.oys.ui.view.ratings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import de.pse.oys.data.facade.Rating
import de.pse.oys.ui.theme.Blue
import de.pse.oys.ui.theme.LightBlue
import de.pse.oys.ui.util.RatingSlider
import de.pse.oys.ui.util.ViewHeader

@Composable
fun RatingView(viewModel: IRatingViewModel) {
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            ViewHeader("Einheit bewerten")
            RatingQuestion(
                1,
                "Wie gut konntest du deine Ziele für diese Einheit umsetzten?",
                viewModel.goalCompletion,
                listOf("Gar nicht", "Eher nicht", "Teilweise", "Gut", "Sehr gut"),
                onRatingChange = { viewModel.goalCompletion = it })
            RatingQuestion(
                2,
                "Wie hast du die Länge der Einheit empfunden?",
                viewModel.duration,
                listOf("Viel zu kurz", "Zu kurz", "Gut", "Zu lang", "Viel zu lang"),
                onRatingChange = { viewModel.duration = it })
            RatingQuestion(
                3,
                "Wie konzentriert und motiviert warst du während dieser Einheit?",
                viewModel.motivation,
                listOf("Sehr gering", "Eher gering", "Mittel", "Eher hoch", "Sehr hoch"),
                onRatingChange = { viewModel.motivation = it })
        }
    }
}

@Composable
private fun RatingQuestion(
    questionNumber: Int,
    question: String,
    aspect: Rating,
    labels: List<String>,
    onRatingChange: (Rating) -> Unit
) {
    Column(
        modifier = Modifier
            .padding(horizontal = 10.dp)
            .padding(bottom = 10.dp)
            .background(
                color = LightBlue,
                shape = RoundedCornerShape(16.dp)
            )
            .border(
                width = 1.5.dp,
                color = Blue,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(16.dp)
    ) {
        Row {
            Text(
                "$questionNumber.",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = question,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
        RatingSlider(
            currentRating = aspect,
            labels = labels,
            onRatingChange = onRatingChange
        )
    }
}

interface IRatingViewModel {
    var goalCompletion: Rating
    var duration: Rating
    var motivation: Rating

    fun submitRating()
    fun submitMissed()
}