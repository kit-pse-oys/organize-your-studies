package de.pse.oys.ui.view.ratings

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import de.pse.oys.data.properties.Darkmode

@Composable
fun AvailableRatingsView(viewModel: IAvailableRatingsViewModel) {
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        TODO()
    }
}

data class RatingTarget(val name: String, val color: Color)

interface IAvailableRatingsViewModel {
    val available: List<RatingTarget>

    fun selectRating(rating: RatingTarget)
}