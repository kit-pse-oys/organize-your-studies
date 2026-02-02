package de.pse.oys.ui.view.onboarding

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
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
import de.pse.oys.ui.navigation.Questionnaire
import de.pse.oys.ui.navigation.main
import de.pse.oys.ui.theme.Blue
import de.pse.oys.ui.theme.LightBlue
import de.pse.oys.ui.theme.MediumBlue
import de.pse.oys.ui.util.SubmitButton
import de.pse.oys.ui.util.ViewHeader
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * View for the welcome and questionnaire screen.
 * Shows the welcome screen if the user has not answered the questionnaire before and lets the user start the questionnaire.
 * Allows the user to answer the [Questions] by selecting given answers.
 * @param viewModel the [IQuestionnaireViewModel] for this view.
 */
@Composable
fun QuestionnaireView(viewModel: IQuestionnaireViewModel) {
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        if (viewModel.showWelcome) {
            Column(
                Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(stringResource(R.string.welcome_header))
                Text(
                    stringResource(R.string.welcome_name),
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(Modifier.height(20.dp))
                Text(stringResource(R.string.welcome_explanation), textAlign = TextAlign.Center)
                Button(
                    onClick = viewModel::showQuestionnaire,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Blue)
                ) {
                    Text(stringResource(R.string.welcome_start))
                }
                Text(
                    stringResource(R.string.welcome_hint),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        } else {
            Column(
                Modifier.padding(innerPadding),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ViewHeader(stringResource(R.string.questionnaire_header))
                Questions.forEachIndexed { i, question ->
                    Card(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp)
                            .padding(bottom = 10.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = LightBlue
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        CompositionLocalProvider(LocalContentColor provides Color.Black) {
                            Column(Modifier.padding(10.dp)) {
                                Text(
                                    stringResource(R.string.question_header, i + 1),
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(question.getDisplayQuestion())
                                Spacer(Modifier.height(4.dp))
                                FlowRow(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 10.dp),
                                    maxItemsInEachRow = Int.MAX_VALUE
                                ) {
                                    question.answers.forEach { answer ->
                                        val selected by viewModel.selected(question, answer)
                                            .collectAsStateWithLifecycle()

                                        FilterChip(
                                            modifier = Modifier.padding(horizontal = 4.dp),
                                            selected = selected,
                                            onClick = { viewModel.select(question, answer) },
                                            shape = RoundedCornerShape(20.dp),
                                            label = {
                                                Text(
                                                    text = answer.getDisplayLabel(),
                                                    textAlign = TextAlign.Center
                                                )
                                            },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = Blue,
                                                selectedLabelColor = Color.White,
                                                containerColor = Color.Transparent,
                                            ),
                                            border = FilterChipDefaults.filterChipBorder(
                                                enabled = true,
                                                selected = selected,
                                                borderColor = MediumBlue,
                                                borderWidth = 1.3.dp
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.weight(1f))
                SubmitButton(
                    stringResource(R.string.questionnaire_commit),
                    true,
                    viewModel::submitQuestionnaire
                )
            }
        }
    }
}

/**
 * Converts a [Question] to a string.
 * @return the string representation of the question.
 */
@SuppressLint("DiscouragedApi", "LocalContextResourcesRead")
@Composable
fun Question.getDisplayQuestion(): String {
    val context = LocalContext.current
    val packageName = context.packageName
    val resId = remember(id) {
        context.resources.getIdentifier(id, "string", packageName)
    }

    return if (resId != 0) stringResource(resId) else id
}

/**
 * Converts an [Answer] to a string or the id if the string resource is not found.
 * @return the string representation of the answer or the id.
 */
@SuppressLint("DiscouragedApi", "LocalContextResourcesRead")
@Composable
fun Answer.getDisplayLabel(): String {
    val context = LocalContext.current
    val resId = remember(id) {
        context.resources.getIdentifier(id, "string", context.packageName)
    }
    return if (resId != 0) stringResource(resId) else id
}

/**
 * Interface for the view model of the [QuestionnaireView].
 * @property showWelcome whether the welcome screen should be shown.
 */
interface IQuestionnaireViewModel {
    val showWelcome: Boolean

    /**
     * Returns a observable state whether the given answer is currently selected for a question.
     * @param question the question to check.
     * @param answer the answer to check.
     * @return a [StateFlow] emitting true if selected, false otherwise.
     */
    fun selected(question: Question, answer: Answer): StateFlow<Boolean>

    /**
     * Toggles or sets the selection state of an answer for a specific question.
     * @param question the question to update.
     * @param answer the answer to select.
     */
    fun select(question: Question, answer: Answer)

    /**
     * Shows the questionnaire.
     */
    fun showQuestionnaire()

    /**
     * Submits the questionnaire and navigates to the main screen.
     */
    fun submitQuestionnaire()
}

/**
 * Base view model for the [QuestionnaireView].
 * @param api the [RemoteAPI] for this view.
 * @property state the current state of the questionnaire.
 */
abstract class BaseQuestionnaireViewModel(private val api: RemoteAPI) :
    ViewModel(),
    IQuestionnaireViewModel {
    private var state: QuestionState = QuestionState()

    private val _selectedFlows = Questions.associateWith { question ->
        question.answers.associateWith {
            MutableStateFlow(false)
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

    protected fun updateState(newState: QuestionState) {
        state = newState

        _selectedFlows.forEach { (question, answers) ->
            answers.forEach { (answer, flow) ->
                flow.value = state.selected(question, answer)
            }
        }
    }

    override fun submitQuestionnaire() {
        viewModelScope.launch {
            api.updateQuestionnaire(state)

            navigateToMain()
        }
    }

    /**
     * Navigates to the main screen.
     */
    abstract fun navigateToMain()
}

/**
 * View model for the [QuestionnaireView] for when its a users first time using the app.
 * @param api the [RemoteAPI] for this view.
 * @param navController the [NavController] for this view.
 */
class FirstQuestionnaireViewModel(api: RemoteAPI, private val navController: NavController) :
    BaseQuestionnaireViewModel(api) {
    override var showWelcome by mutableStateOf(true)

    override fun showQuestionnaire() {
        showWelcome = false
    }

    override fun navigateToMain() {
        navController.main(dontGoBack = Questionnaire(true))
    }
}

/**
 * View model for the [QuestionnaireView] for when a users wants to edit their questionnaire.
 * @param api the [RemoteAPI] for this view.
 * @param navController the [NavController] for this view.
 */
class EditQuestionnaireViewModel(api: RemoteAPI, private val navController: NavController) :
    BaseQuestionnaireViewModel(api) {
    init {
        val state = TODO("Get state from api")
        updateState(state)
    }

    override val showWelcome = false

    override fun showQuestionnaire() {}

    override fun navigateToMain() {
        navController.main()
    }
}

/**
 * Decides which view model to use for the [QuestionnaireView] (edit or first time).
 * @param firstTime whether the user is a new user.
 * @param api the [RemoteAPI] for this view.
 * @param navController the [NavController] for this view.
 * @return the [BaseQuestionnaireViewModel] for the [QuestionnaireView].
 */
fun QuestionnaireViewModel(
    firstTime: Boolean,
    api: RemoteAPI,
    navController: NavController
): BaseQuestionnaireViewModel {
    return if (firstTime) FirstQuestionnaireViewModel(api, navController)
    else EditQuestionnaireViewModel(api, navController)
}