package de.pse.oys.ui.view.ratings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import de.pse.oys.R
import de.pse.oys.data.RatingAspect
import de.pse.oys.data.api.RemoteAPI
import de.pse.oys.data.facade.Rating
import de.pse.oys.data.facade.UnitRatings
import de.pse.oys.ui.navigation.AvailableRatings
import de.pse.oys.ui.navigation.availableRatings
import de.pse.oys.ui.theme.Blue
import de.pse.oys.ui.theme.LightBlue
import de.pse.oys.ui.util.RatingSlider
import de.pse.oys.ui.util.SubmitButton
import de.pse.oys.ui.util.ViewHeader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.uuid.Uuid

@Composable
fun RatingView(viewModel: IRatingViewModel) {
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        var selectedMissed by remember { mutableStateOf(false) }
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ViewHeader(stringResource(id = R.string.rate_unit_header))
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(innerPadding)
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                item {
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
                }
                RatingAspect.entries.forEachIndexed { index, aspect ->
                    item {
                        RatingQuestion(
                            questionNumber = index + 1,
                            question = stringResource(id = aspect.textRes),
                            aspect = viewModel.getRating(aspect),
                            labels = aspect.labelsRes.map { stringResource(id = it) },
                            selectedMissed = selectedMissed,
                            onRatingChange = { newRating ->
                                viewModel.updateRating(aspect, newRating)
                            }
                        )
                    }
                }
            }
            SubmitButton(stringResource(id = R.string.save_rating)) { viewModel.submitRating() }
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
    fun getRating(aspect: RatingAspect): Rating
    fun updateRating(aspect: RatingAspect, rating: Rating)
    fun submitRating()
    fun submitMissed()
}

class RatingViewModel(
    private val api: RemoteAPI,
    private val target: Uuid,
    private val navController: NavController
) : ViewModel(), IRatingViewModel {
    private var completion by mutableStateOf(Rating.MEDIUM)
    private var duration by mutableStateOf(Rating.MEDIUM)
    private var motivation by mutableStateOf(Rating.MEDIUM)

    override fun getRating(aspect: RatingAspect): Rating {
        return when (aspect) {
            RatingAspect.GOAL -> completion
            RatingAspect.DURATION -> duration
            RatingAspect.MOTIVATION -> motivation
        }
    }

    override fun updateRating(
        aspect: RatingAspect,
        rating: Rating
    ) {
        when (aspect) {
            RatingAspect.GOAL -> completion = rating
            RatingAspect.DURATION -> duration = rating
            RatingAspect.MOTIVATION -> motivation = rating
        }
    }

    override fun submitRating() {
        viewModelScope.launch {
            api.rateUnit(target, UnitRatings(completion, duration, motivation))

            withContext(Dispatchers.Main.immediate) {
                navController.availableRatings(dontGoBack = AvailableRatings)
            }
        }
    }

    override fun submitMissed() {
        TODO("Not yet implemented")
    }
}