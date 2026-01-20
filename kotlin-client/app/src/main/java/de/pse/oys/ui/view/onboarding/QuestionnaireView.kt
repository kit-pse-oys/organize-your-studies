package de.pse.oys.ui.view.onboarding

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import de.pse.oys.data.Answer
import de.pse.oys.data.Question
import kotlinx.coroutines.flow.StateFlow

@Composable
fun QuestionnaireView(viewModel: IQuestionnaireViewModel) {
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        TODO()
    }
}

interface IQuestionnaireViewModel {
    val showWelcome: Boolean

    fun selected(question: Question, answer: Answer): StateFlow<Boolean>
    fun select(question: Question, answer: Answer)

    fun showQuestionnaire()
    fun submitQuestionnaire()
}