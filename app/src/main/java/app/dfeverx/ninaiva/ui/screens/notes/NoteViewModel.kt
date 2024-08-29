package app.dfeverx.ninaiva.ui.screens.notes

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.dfeverx.ninaiva.models.local.FlashCard
import app.dfeverx.ninaiva.models.local.KeyArea
import app.dfeverx.ninaiva.models.local.StudyNote
import app.dfeverx.ninaiva.repos.StudyNoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NoteViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle, private val noteRepository: StudyNoteRepository
) : ViewModel() {


    private val TAG = "NoteViewModel"
    private val studyNoteId: String = checkNotNull(savedStateHandle["noteId"])

    private val _studyNote: MutableStateFlow<StudyNote> = MutableStateFlow(StudyNote("", "").apply {
        currentStage = -1
        isPlaceholder = true
        keyAreas = listOf(
            KeyArea(-1, "", "", ""),
            KeyArea(-1, "", "", ""),
            KeyArea(-1, "", "", ""),
            KeyArea(-1, "", "", ""),
            KeyArea(-1, "", "", ""),
        )
    })
    val studyNote: StateFlow<StudyNote>
        get() = _studyNote


    private val _flashCards: MutableStateFlow<List<FlashCard>> = MutableStateFlow(
        listOf(
            FlashCard(emoji = "-----").apply { isPlaceholder = true },
            FlashCard(emoji = "-----").apply { isPlaceholder = true },
            FlashCard(emoji = "-----").apply { isPlaceholder = true },
            FlashCard(emoji = "-----").apply { isPlaceholder = true },
            FlashCard(emoji = "-----").apply { isPlaceholder = true },
            FlashCard(emoji = "-----").apply { isPlaceholder = true },
            FlashCard(emoji = "-----").apply { isPlaceholder = true },
            FlashCard(emoji = "-----").apply { isPlaceholder = true },
            FlashCard(emoji = "-----").apply { isPlaceholder = true },

            )
    )
    val flashCards: StateFlow<List<FlashCard>>
        get() = _flashCards

    private val _questionCount: MutableStateFlow<Int> = MutableStateFlow(-1)

    val questionCount: StateFlow<Int> = _questionCount

    init {
        Log.d(TAG, "noteId: $studyNoteId")
        viewModelScope.launch(Dispatchers.IO) {
            delay(400)// for check placeholder anim
            noteRepository.studyNoteById(studyNoteId).collect { studyNote ->
                _studyNote.value = studyNote
            }
        }
        viewModelScope.launch(Dispatchers.IO) {
            delay(800)// for check placeholder anim
            noteRepository.flashCardsByStudyNoteId(studyNoteId).collect { fc ->
                _flashCards.value = fc
            }
        }
        viewModelScope.launch(Dispatchers.IO) {
            _questionCount.value = noteRepository.questionCount(studyNoteId)
        }
    }

    fun updatePinned(noteDetails: StudyNote) {
        viewModelScope.launch(Dispatchers.IO) {
            noteRepository.updatePinned(noteDetails.id, !noteDetails.isPinned)
        }
    }

    fun updateStarred(noteDetails: StudyNote) {
        viewModelScope.launch(Dispatchers.IO) {
            noteRepository.updateStarred(noteDetails.id, !noteDetails.isStarred)
        }
    }
}