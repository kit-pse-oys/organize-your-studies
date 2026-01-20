package de.pse.oys.ui.view.additions.freetime

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

@Composable
fun CreateFreeTimeView(viewModel: ICreateFreeTimeViewModel) {
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        TODO()
    }
}

interface ICreateFreeTimeViewModel {
    val showDelete: Boolean

    var title: String
    var date: LocalDate
    var start: LocalTime
    var end: LocalTime
    var weekly: Boolean

    fun submit()
    fun delete()
}