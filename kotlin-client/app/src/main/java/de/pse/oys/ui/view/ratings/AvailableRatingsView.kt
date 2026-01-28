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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import de.pse.oys.R
import de.pse.oys.ui.navigation.rating
import de.pse.oys.ui.theme.LightBlue
import de.pse.oys.ui.util.ViewHeader

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
                    modifier = Modifier
                        .fillMaxSize(),
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
            contentColor = MaterialTheme.colorScheme.onSurface
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
    private val navController: NavController,
    override val available: List<RatingTarget>
) : IAvailableRatingsViewModel {

    override fun selectRating(rating: RatingTarget) {
        if (available.contains(rating)) {
            navController.rating()
        }
    }
}