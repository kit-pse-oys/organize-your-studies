package de.pse.oys.ui.view.additions.module

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.pse.oys.data.facade.Module
import de.pse.oys.ui.theme.DarkBlue
import de.pse.oys.ui.theme.LightBlue

@Composable
fun ModulesView(viewModel: IModulesViewModel) {
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Text(
                    "Meine Module",
                    style = typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.padding(top = 18.dp, bottom = 10.dp)
                )
            }
            items(viewModel.modules) { module ->
                OutlinedButton(
                    onClick = { viewModel.select(module) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp, horizontal = 16.dp),
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(2.dp, DarkBlue),
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
                            module.data.title, style = typography.titleLarge.copy(
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                        Text(module.data.description)
                        Text("Priorität: " + module.data.priority.toGermanString())
                    }
                }
            }
        }
    }
}

interface IModulesViewModel {
    val modules: List<Module>

    fun select(module: Module)
}
