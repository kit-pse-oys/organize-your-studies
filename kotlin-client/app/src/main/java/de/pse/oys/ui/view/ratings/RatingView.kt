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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import de.pse.oys.data.defaultHandleError
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

/**
 * View for the rating of a unit.
 * Allows the user to rate a unit in different aspects and submit it.
 * @param viewModel the [IRatingViewModel] for this view.
 */
@Composable
fun RatingView(viewModel: IRatingViewModel) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel.error) {
        if (viewModel.error) {
            snackbarHostState.showSnackbar("Something went wrong...")
            viewModel.error = false
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) }) { innerPadding ->
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
                            currentRating = viewModel.getRating(aspect),
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

/**
 * Presents a rating question that can be answered by a slider.
 * @param questionNumber the number of the question.
 * @param question the question itself.
 * @param currentRating the current rating of the question.
 * @param labels the labels on each step of the slider.
 * @param selectedMissed whether the user has selected that the unit was missed.
 * @param onRatingChange the function to be called when the rating is changed.
 */
@Composable
private fun RatingQuestion(
    questionNumber: Int,
    question: String,
    currentRating: Rating,
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
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Text(
                text = question,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }
        RatingSlider(
            currentRating = currentRating,
            labels = labels,
            enabled = !selectedMissed,
            onRatingChange = onRatingChange
        )
    }
}


/**
 * Interface for the view model of the [RatingView].
 */
interface IRatingViewModel {
    var error: Boolean

    /**
     * Gets the rating for a specific aspect.
     * @param aspect the [RatingAspect] to get the rating for.
     */
    fun getRating(aspect: RatingAspect): Rating

    /**
     * Updates the rating for a specific aspect.
     * @param aspect the [RatingAspect] to update the rating for.
     */
    fun updateRating(aspect: RatingAspect, rating: Rating)

    /**
     * Submits the rating.
     */
    fun submitRating()

    /**
     * Submits that the unit was missed.
     */
    fun submitMissed()
}

/**
 * View model for the [RatingView].
 * @param api the [RemoteAPI] for this view.
 * @param target the [Uuid] of the unit to be rated.
 * @param navController the [NavController] for this view.
 */
class RatingViewModel(
    private val api: RemoteAPI,
    private val target: Uuid,
    private val navController: NavController
) : ViewModel(), IRatingViewModel {
    override var error: Boolean by mutableStateOf(false)

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
                .defaultHandleError(navController) { error = true }

            withContext(Dispatchers.Main.immediate) {
                navController.availableRatings(dontGoBack = AvailableRatings)
            }
        }
    }

    override fun submitMissed() {
        viewModelScope.launch {
            api.rateUnitMissed(target).defaultHandleError(navController) { error = true }
        }
    }
}