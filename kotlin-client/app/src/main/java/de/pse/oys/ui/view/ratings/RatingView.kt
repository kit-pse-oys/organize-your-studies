package de.pse.oys.ui.view.ratings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import de.pse.oys.R
import de.pse.oys.data.RatingQuestions
import de.pse.oys.data.facade.Rating
import de.pse.oys.ui.theme.Blue
import de.pse.oys.ui.theme.LightBlue
import de.pse.oys.ui.util.RatingSlider
import de.pse.oys.ui.util.ViewHeader

@Composable
fun RatingView(viewModel: IRatingViewModel) {
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        var selectedMissed by remember { mutableStateOf(false) }
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            ViewHeader(stringResource(id = R.string.rate_unit_header))
            OutlinedButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp)
                    .padding(bottom = 10.dp),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = if (!selectedMissed) Color.LightGray else LightBlue),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.5.dp, if (!selectedMissed) Color.Gray else Blue),
                onClick = {
                    selectedMissed = !selectedMissed
                }) {
                Text(
                    stringResource(id = R.string.unit_missed_button),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            RatingQuestions.forEachIndexed { index, questionData ->
                RatingQuestion(
                    questionNumber = index + 1,
                    question = stringResource(id = questionData.textRes),
                    aspect = viewModel.getRatingById(questionData.id),
                    labels = questionData.labelsRes.map { stringResource(id = it) },
                    selectedMissed = selectedMissed,
                    onRatingChange = { newRating ->
                        viewModel.updateRatingById(questionData.id, newRating)
                    }
                )
            }
        }
    }
}

@Composable
private fun RatingQuestion(
    questionNumber: Int,
    question: String,
    aspect: Rating,
    labels: List<String>,
    selectedMissed: Boolean,
    onRatingChange: (Rating) -> Unit
) {
    Column(
        modifier = Modifier
            .padding(horizontal = 10.dp)
            .padding(bottom = 10.dp)
            .background(
                color = if (selectedMissed) Color.LightGray else LightBlue,
                shape = RoundedCornerShape(16.dp)
            )
            .border(
                width = 1.5.dp,
                color = if (selectedMissed) Color.Gray else Blue,
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
            enabled = !selectedMissed,
            onRatingChange = onRatingChange
        )
    }
}

interface IRatingViewModel {
    fun getRatingById(id: String): Rating

    fun updateRatingById(id: String, rating: Rating)

    fun submitRating()
    fun submitMissed()
}