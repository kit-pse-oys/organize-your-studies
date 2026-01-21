package de.pse.oys.ui.view.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import de.pse.oys.R
import de.pse.oys.data.Answer
import de.pse.oys.data.Question
import de.pse.oys.data.QuestionState
import de.pse.oys.data.Questions
import de.pse.oys.data.api.RemoteAPI
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@Composable
fun QuestionnaireView(viewModel: IQuestionnaireViewModel) {
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        if (viewModel.showWelcome) {
            Column(
                Modifier.padding(innerPadding).fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(stringResource(R.string.welcome_header))
                Text(
                    stringResource(R.string.welcome_name),
                    fontSize = LocalTextStyle.current.fontSize * 1.5f
                )
                Spacer(Modifier.height(20.dp))
                Text(stringResource(R.string.welcome_explanation), textAlign = TextAlign.Center)
                Button(onClick = viewModel::showQuestionnaire) {
                    Text(stringResource(R.string.welcome_start))
                }
                Text(
                    stringResource(R.string.welcome_hint),
                    fontSize = LocalTextStyle.current.fontSize * 0.7f
                )
            }
        } else {
            Column(Modifier.padding(innerPadding), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(stringResource(R.string.questionnaire_header))
                Questions.forEachIndexed { i, question ->
                    Card(
                        Modifier
                            .fillMaxWidth()
                            .padding(10.dp)
                    ) {
                        Text(stringResource(R.string.question_header, i))
                        // Text(question.question())
                        LazyVerticalGrid(columns = GridCells.Fixed(/* question.columnCount */ 0)) {
                            items(question.answers) { answer ->
                                val selected by viewModel.selected(question, answer)
                                    .collectAsStateWithLifecycle()
                                FilterChip(
                                    selected = selected,
                                    onClick = { viewModel.select(question, answer) },
                                    label = {
                                        // Text(answer.label())
                                        /* answer.hint()?.let { hint ->
                                            Text(
                                                hint,
                                                fontSize = LocalTextStyle.current.fontSize * 0.7f
                                            )
                                        } */
                                    })
                            }
                        }
                    }
                }
                Spacer(Modifier.weight(1f))
                Button(onClick = viewModel::submitQuestionnaire) {
                    Text(stringResource(R.string.questionnaire_commit))
                }
            }
        }
    }
}

interface IQuestionnaireViewModel {
    val showWelcome: Boolean

    fun selected(question: Question, answer: Answer): StateFlow<Boolean>
    fun select(question: Question, answer: Answer)

    fun showQuestionnaire()
    fun submitQuestionnaire()
}

abstract class BaseQuestionnaireViewModel(val api: RemoteAPI, val state: QuestionState) :
    ViewModel(),
    IQuestionnaireViewModel {
    private val _selectedFlows = Questions.associateWith { question ->
        question.answers.associateWith { answer ->
            MutableStateFlow(state.selected(question, answer))
        }
    }

    private val selectedFlows: Map<Question, Map<Answer, StateFlow<Boolean>>>
        get() = _selectedFlows.mapValues { (_, answers) ->
            answers.mapValues { (_, flow) -> flow.asStateFlow() }
        }

    override fun selected(question: Question, answer: Answer): StateFlow<Boolean> {
        return selectedFlows.getValue(question).getValue(answer)
    }

    override fun select(question: Question, answer: Answer) {
        state.select(question, answer)

        _selectedFlows.getValue(question).forEach { (answer, flow) ->
            flow.value = state.selected(question, answer)
        }
    }

    override fun submitQuestionnaire() {
        viewModelScope.launch {
            api.updateQuestionnaire(state)
            TODO("Navigate to main")
        }
    }
}

class FirstQuestionnaireViewModel(api: RemoteAPI, val navController: NavController) :
    BaseQuestionnaireViewModel(api, QuestionState()) {
    override var showWelcome by mutableStateOf(true)

    override fun showQuestionnaire() {
        showWelcome = false
    }
}

class EditQuestionnaireViewModel(api: RemoteAPI, val navController: NavController) :
    BaseQuestionnaireViewModel(api, QuestionState()) {
    override val showWelcome = false

    override fun showQuestionnaire() {}
}

fun QuestionnaireViewModel(
    firstTime: Boolean,
    api: RemoteAPI,
    navController: NavController
): BaseQuestionnaireViewModel {
    return if (firstTime) FirstQuestionnaireViewModel(api, navController)
    else EditQuestionnaireViewModel(api, navController)
}