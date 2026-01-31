package de.pse.oys.ui.view.additions.freetime

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
import androidx.navigation.NavController
import de.pse.oys.R
import de.pse.oys.data.facade.FreeTime
import de.pse.oys.data.facade.ModelFacade
import de.pse.oys.ui.navigation.editFreeTime
import de.pse.oys.ui.theme.Blue
import de.pse.oys.ui.theme.LightBlue
import de.pse.oys.ui.util.ViewHeader
import de.pse.oys.ui.util.toFormattedString

@Composable
fun FreeTimesView(viewModel: IFreeTimesViewModel) {
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ViewHeader(text = stringResource(id = R.string.my_free_times_button))
            if (viewModel.freeTimes.isEmpty()) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(id = R.string.no_freetimes_available),
                        color = Color.Gray,
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    items(viewModel.freeTimes) { freeTime ->
                        FreeTimeButton(freeTime, viewModel)
                    }
                }
            }
        }
    }
}


@Composable
private fun FreeTimeButton(freeTime: FreeTime, viewModel: IFreeTimesViewModel) {
    OutlinedButton(
        onClick = { viewModel.select(freeTime) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 16.dp),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(2.dp, Blue),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = LightBlue, contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp, horizontal = 2.dp)
        ) {
            Text(
                freeTime.data.title, style = typography.titleLarge.copy(
                    fontSize = 18.sp, fontWeight = FontWeight.SemiBold
                )
            )
            if (freeTime.data.weekly) {
                Text(stringResource(id = R.string.weekly_freetime_since) + freeTime.data.date.toFormattedString())
            } else {
                Text(stringResource(id = R.string.freetime_on_date) + freeTime.data.date.toFormattedString())
            }
            Text(stringResource(id = R.string.at_time) + freeTime.data.start.toFormattedString() + " - " + freeTime.data.end.toFormattedString())
        }
    }
}

interface IFreeTimesViewModel {
    val freeTimes: List<FreeTime>

    fun select(freeTime: FreeTime)
}

class FreeTimesViewModel(
    model: ModelFacade,
    private val navController: NavController
) : ViewModel(), IFreeTimesViewModel {
    override var freeTimes: List<FreeTime> by mutableStateOf(listOf())

    init {
        require(model.freeTimes != null)
        freeTimes = model.freeTimes!!.map { FreeTime(it.value, it.key) }
    }

    override fun select(freeTime: FreeTime) {
        if (freeTimes.contains(freeTime)) {
            navController.editFreeTime(freeTime)
        }
    }
}