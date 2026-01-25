package de.pse.oys.ui.view.additions.freetime

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.pse.oys.data.facade.FreeTime
import de.pse.oys.ui.theme.Blue
import de.pse.oys.ui.theme.LightBlue
import de.pse.oys.ui.util.ViewHeader
import de.pse.oys.ui.util.toFormattedString

@Composable
fun FreeTimesView(viewModel: IFreeTimesViewModel) {
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                ViewHeader(text = "Meine Freizeiten")
            }
            items(viewModel.freeTimes) { freeTime ->
                FreeTimeButton(freeTime, viewModel)
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
            containerColor = LightBlue,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp, horizontal = 2.dp)
        ) {
            Text(
                freeTime.data.title, style = typography.titleLarge.copy(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
            )
            if (freeTime.data.weekly) {
                Text("wöchentliche Freizeit (seit " + freeTime.data.date.toFormattedString() + ")")
            } else {
                Text("Freizeit am " + freeTime.data.date.toFormattedString())
            }
            Text("Uhrzeit: " + freeTime.data.start.toFormattedString() + " - " + freeTime.data.end.toFormattedString())
        }
    }
}

interface IFreeTimesViewModel {
    val freeTimes: List<FreeTime>

    fun select(freeTime: FreeTime)
}