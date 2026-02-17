package de.pse.oys.ui.view.ratings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
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
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import de.pse.oys.R
import de.pse.oys.data.api.RemoteAPI
import de.pse.oys.data.defaultHandleError
import de.pse.oys.data.ensureUnits
import de.pse.oys.data.facade.ModelFacade
import de.pse.oys.ui.navigation.rating
import de.pse.oys.ui.theme.LightBlue
import de.pse.oys.ui.util.ViewHeaderWithBackOption
import de.pse.oys.ui.util.toFormattedString
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlin.uuid.Uuid

/**
 * View for the available ratings.
 * Shows a list of available ratings and allows the user to select one and navigate to the rating view.
 * @param viewModel the [IAvailableRatingsViewModel] for this view.
 */
@Composable
fun AvailableRatingsView(viewModel: IAvailableRatingsViewModel) {
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
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ViewHeaderWithBackOption(
                viewModel::navigateBack,
                stringResource(R.string.rate_units_header)
            )
            if (viewModel.available.isEmpty()) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(id = R.string.no_ratings_available),
                        color = Color.Gray,
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    items(viewModel.available) { target ->
                        RatingSelectionItem(
                            target = target,
                            onClick = { viewModel.selectRating(target) }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Presents a unit that's available for rating.
 * @param target the [RatingTarget] to be displayed.
 * @param onClick the function to be called when the item is clicked.
 */
@Composable
private fun RatingSelectionItem(
    target: RatingTarget,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 20.dp),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(2.dp, target.color),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = LightBlue,
            contentColor = Color.Black
        )
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp, horizontal = 2.dp)
        ) {
            Text(
                text = target.name,
                style = typography.titleLarge.copy(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                modifier = Modifier.padding(vertical = 8.dp)
            )
            Text("${target.day.toFormattedString()} | ${target.start.toFormattedString()} - ${target.end.toFormattedString()}")
        }
    }
}

/**
 * Data class representing a rating target.
 * @param name the name of the rating target.
 * @param color the color of the module associated with the rating target.
 */
data class RatingTarget(
    val name: String,
    val color: Color,
    val day: LocalDate,
    val start: LocalTime,
    val end: LocalTime
)

/**
 * Interface for the view model of the [AvailableRatingsView].
 * @property available the list of available ratings.
 */
interface IAvailableRatingsViewModel {
    var error: Boolean
    val available: List<RatingTarget>

    /**
     * Selects a rating target and navigates to the rating view.
     * @param rating the [RatingTarget] to be selected.
     */
    fun selectRating(rating: RatingTarget)

    /**
     * Navigates back to the previous view.
     */
    fun navigateBack()
}

/**
 * View model for the [AvailableRatingsView].
 * @param api the [RemoteAPI] for this view.
 * @param model the [ModelFacade] for this view.
 * @param navController the [NavController] for this view.
 */
class AvailableRatingsViewModel(
    private val api: RemoteAPI,
    private val model: ModelFacade,
    private val navController: NavController
) : ViewModel(), IAvailableRatingsViewModel {
    override var error: Boolean by mutableStateOf(false)

    private var _available: Map<RatingTarget, Uuid> by mutableStateOf(mapOf())
    override val available: List<RatingTarget> by derivedStateOf {
        _available.entries.sortedBy { it.value }.map { it.key }
    }

    init {
        require(model.steps != null)

        viewModelScope.launch {
            model.ensureUnits(api).defaultHandleError(navController) { error = true }
                ?.let { units ->
                    api.queryRateable().defaultHandleError(navController) { error = true }
                        ?.let { rateable ->
                            _available = rateable.associateBy { uuid ->
                                val step = units.values.firstOrNull { uuid in it.keys }?.get(uuid)
                                    ?: error("Unit not found")
                                RatingTarget(
                                    step.task.data.title,
                                    step.task.data.module.data.color,
                                    step.date,
                                    step.start,
                                    step.end
                                )
                            }
                        }
                }
        }
    }

    override fun selectRating(rating: RatingTarget) {
        val uuid = _available[rating] ?: return
        navController.rating(uuid)
    }

    override fun navigateBack() {
        navController.popBackStack()
    }
}