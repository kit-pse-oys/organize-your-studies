package de.pse.oys.ui.view.ratings

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import de.pse.oys.data.facade.Rating

@Composable
fun RatingView(viewModel: IRatingViewModel) {
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        TODO()
    }
}

interface IRatingViewModel {
    var goalCompletion: Rating
    var duration: Rating
    var motivation: Rating

    fun submitRating()
    fun submitMissed()
}