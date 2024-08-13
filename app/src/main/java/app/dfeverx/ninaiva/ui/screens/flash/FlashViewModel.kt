package app.dfeverx.ninaiva.ui.screens.flash

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.dfeverx.ninaiva.models.local.FlashCard
import app.dfeverx.ninaiva.repos.PlayRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FlashViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val playRepository: PlayRepository
) : ViewModel() {

    private val TAG = "FlashViewModel"
    private val noteId: String = checkNotNull(savedStateHandle["noteId"])
    val currentCardIndex: Int = checkNotNull(savedStateHandle["currentCardIndex"])


    private val _flashCards = MutableStateFlow<List<FlashCard>>(listOf())

    val flashCards: StateFlow<List<FlashCard>> = _flashCards.asStateFlow()

    init {
        viewModelScope.launch {
            playRepository.flashCardsByNoteId(noteId).collect {
                _flashCards.value = it
            }

        }
    }


    //    return true when the attempt is correct
    fun validateAttempt(): Boolean {
        return true
    }


}