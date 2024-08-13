package app.dfeverx.ninaiva.ui.screens.play

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.dfeverx.ninaiva.models.local.Option
import app.dfeverx.ninaiva.repos.PlayRepository
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val playRepository: PlayRepository
) : ViewModel() {

    private val TAG = "PlayViewModel"
    private val levelId: Long = checkNotNull(savedStateHandle["levelId"])
    private val stage: Int = checkNotNull(savedStateHandle["stage"])

    private val _uiState = MutableStateFlow(PlayUiState(stage))
    val uiState: StateFlow<PlayUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            playRepository.questionsByLevelId(levelId).let {
                _uiState.value = PlayUiState(
                    stage = stage,
                    questions = it,
                    totalQuestionSize = it.size,
                    currentQuestionIndex = 0
                )
            }

        }
    }


    //    return true when the attempt is correct
    fun validateAttempt(): Boolean {
        val currentQuestion = _uiState.value.currentQuestion()
        val result = _uiState.value.validateAttempt()
        // update the question attempt
        viewModelScope.launch {
            currentQuestion?.let {
                playRepository.updateAttemptInQuestion(
                    questionId = it.id,
                    updatedScore = if (result) it.score + 1 else it.score - 1
                )
            }
        }
        return result
    }

    fun handleNextQuestion() {
        Log.d(TAG, "handleNext:  " + Gson().toJson(_uiState.value).toString())
        _uiState.value.attempt = listOf()//resetting attempt
        if (_uiState.value.totalQuestionSize - 1 > _uiState.value.currentQuestionIndex) {
            _uiState.update { currentState ->
                currentState.copy(currentQuestionIndex = currentState.currentQuestionIndex + 1)
            }
        }
    }

    fun handleOptionSelection(selectedOption: Option) = viewModelScope.launch(Dispatchers.IO) {
        Log.d(TAG, "handleAttempt: $selectedOption")
        val attemptInAttempt =
            _uiState.value.attempt.find { attempt -> attempt.content == selectedOption.content }
        _uiState.update { currentState ->
            if (attemptInAttempt == null) {
                currentState.copy(attempt = listOf(selectedOption))

            } else {
                currentState.copy(attempt = listOf(selectedOption))
            }
        }
    }


}