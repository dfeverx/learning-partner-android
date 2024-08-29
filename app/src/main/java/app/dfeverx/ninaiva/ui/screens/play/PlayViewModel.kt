package app.dfeverx.ninaiva.ui.screens.play

import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.dfeverx.ninaiva.LearningPartnerApplication
import app.dfeverx.ninaiva.models.local.Option
import app.dfeverx.ninaiva.models.local.Question
import app.dfeverx.ninaiva.repos.PlayRepository
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.time.delay
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class PlayViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    application: LearningPartnerApplication,
    private val playRepository: PlayRepository
) : ViewModel() {

    private val TAG = "PlayViewModel"
    private val levelId: Long = checkNotNull(savedStateHandle["levelId"])
    private val stage: Int = checkNotNull(savedStateHandle["stage"])
    private lateinit var tts: TextToSpeech
    private val _isReadingStateFlow: MutableStateFlow<Boolean> =
        MutableStateFlow(false)

    val isReadingStateFlow: StateFlow<Boolean>
        get() = _isReadingStateFlow

    private val _uiState = MutableStateFlow(
        PlayUiState(
            stage, questions = listOf(
                Question(

                    id = -1, "", "",
                    options = listOf(
                        Option("", false),
                        Option("", false),
                        Option("", false),
                        Option("", false),
                    ),
                    difficulty = 0,
                    explanation = ""

                ).apply { isPlaceholder = true }
            ), currentQuestionIndex = 0
        )
    )
    val uiState: StateFlow<PlayUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            delay(2000)
            playRepository.questionsByLevelId(levelId).let {
                _uiState.value = PlayUiState(
                    stage = stage,
                    questions = it,
                    totalQuestionSize = it.size,
                    currentQuestionIndex = 0
                )
            }

        }
        tts = TextToSpeech(
            application
        ) {
            if (it == TextToSpeech.SUCCESS) {
                val result = tts.setLanguage(Locale.US)

                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e("TTS", "This language is not supported")
                }
            } else {
                Log.e("TTS", "Initialization failed")
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


    fun readLoud() {
//        if (_isReadingStateFlow.value) {
        val question = _uiState.value.questions?.get(_uiState.value.currentQuestionIndex)
        val questionPayload =
            question?.statement + " ?." + question?.options?.mapIndexed { i, item -> "Option " + (i + 1) + "." + item.content + "/n" }
        tts.speak(
            questionPayload.removeSpecialCharacters(),
            TextToSpeech.QUEUE_FLUSH,
            null,
            null
        )
//        } else {
//            tts.stop()
//            _isReadingStateFlow
//        }
    }

    private fun String.removeSpecialCharacters(): String {
        val tempChar = '!' // Temporary character to replace line breaks
        val replacedText =
            this.replace("\n", tempChar.toString()) // Replace line breaks with temp char
        val regex = Regex("[^a-zA-Z0-9 .${tempChar}]") // Include temp char in allowed characters
        val filteredText = regex.replace(replacedText, "") // Remove special characters
        return filteredText.replace(tempChar.toString(), "\n")
    }

    override fun onCleared() {
        super.onCleared()
        tts.stop()
        tts.shutdown()

    }

}