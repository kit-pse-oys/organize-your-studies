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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
import de.pse.oys.data.facade.ModelFacade
import de.pse.oys.ui.navigation.rating
import de.pse.oys.ui.theme.LightBlue
import de.pse.oys.ui.util.ViewHeader
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.launch
import kotlin.uuid.Uuid

@Composable
fun AvailableRatingsView(viewModel: IAvailableRatingsViewModel) {
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ViewHeader(stringResource(id = R.string.rate_units_header))
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
        Text(
            text = target.name,
            style = typography.titleLarge.copy(
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            ),
            modifier = Modifier.padding(vertical = 8.dp)
        )
    }
}

data class RatingTarget(val name: String, val color: Color)

interface IAvailableRatingsViewModel {
    val available: List<RatingTarget>

    fun selectRating(rating: RatingTarget)
}

class AvailableRatingsViewModel(
    private val api: RemoteAPI,
    private val model: ModelFacade,
    private val navController: NavController
) : ViewModel(), IAvailableRatingsViewModel {
    private var _available: Map<RatingTarget, Uuid> by mutableStateOf(mapOf())
    override val available: List<RatingTarget> by derivedStateOf {
        _available.entries.sortedBy { it.value }.map { it.key }
    }

    init {
        require(model.steps != null)

        viewModelScope.launch {
            val rateable = api.queryRateable()
            if (rateable.status != HttpStatusCode.OK.value) {
                // TODO: Show error
            }
            val uuids = rateable.response ?: error("No response with Status 200")
            _available = uuids.associateBy { uuid ->
                val step = model.steps?.get(uuid) ?: error("Task not found")
                RatingTarget(step.task.data.title, step.task.data.module.data.color)
            }
        }
    }

    override fun selectRating(rating: RatingTarget) {
        val uuid = _available[rating] ?: return
        navController.rating(uuid)
    }
}