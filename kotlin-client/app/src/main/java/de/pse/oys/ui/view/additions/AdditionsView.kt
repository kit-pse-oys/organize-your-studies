package de.pse.oys.ui.view.additions

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import de.pse.oys.R
import de.pse.oys.ui.theme.Blue
import de.pse.oys.ui.theme.LightBlue
import de.pse.oys.ui.util.ViewHeader

@Composable
fun AdditionsView(viewModel: IAdditionsViewModel) {
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ViewHeader(text = stringResource(id = R.string.additions_header))
            AdditionsButton(
                label = stringResource(id = R.string.new_module),
                onClick = { viewModel.navigateToCreateModule() }
            )
            AdditionsButton(
                label = stringResource(id = R.string.new_task),
                onClick = { viewModel.navigateToCreateTask() }
            )
            AdditionsButton(
                label = stringResource(id = R.string.new_freetime),
                onClick = { viewModel.navigateToCreateFreeTime() }
            )
        }
    }
}

@Composable
private fun AdditionsButton(label: String, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 20.dp)
            .height(60.dp),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(2.dp, Blue),
        colors = ButtonDefaults.outlinedButtonColors(containerColor = LightBlue)
    ) {
        Text(
            text = label,
            style = typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Blue
        )
    }
}

interface IAdditionsViewModel {
    fun navigateToCreateModule()
    fun navigateToCreateTask()
    fun navigateToCreateFreeTime()
}