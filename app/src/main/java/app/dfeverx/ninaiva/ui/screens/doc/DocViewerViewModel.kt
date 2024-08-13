package app.dfeverx.ninaiva.ui.screens.doc

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.dfeverx.ninaiva.models.local.StudyNote
import app.dfeverx.ninaiva.repos.StudyNoteRepository
import app.dfeverx.ninaiva.utils.FileUtils
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class DocViewerViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle, val noteRepository: StudyNoteRepository
) : ViewModel() {
    private val TAG = "DocViewerViewModel"

    private val studyNoteId: String = checkNotNull(savedStateHandle["noteId"])

    private val _studyNote: MutableStateFlow<StudyNote> = MutableStateFlow(StudyNote("", ""))
    val studyNote: StateFlow<StudyNote>
        get() = _studyNote

    private val _docFile: MutableStateFlow<FileState> = MutableStateFlow(FileState.Progress())
    val docFile
        get() = _docFile

    private var isTrying = false


    init {
        Log.d(TAG, "noteId: $studyNoteId")
        viewModelScope.launch(Dispatchers.IO) {
            noteRepository.studyNoteById(studyNoteId).collect { studyNote ->
                val file = FileUtils.getFileFromUri(studyNote.docLocalUrl)
                _studyNote.value = studyNote
//                may the file is outdated there for check
                if (file?.exists() == true) {
                    Log.d(TAG, "file exists: ")
//                    todo
//                    delay(500)//for avoid navigation  stuck effect
                    _docFile.value = FileState.Success(file)

                } else {
                    Log.d(TAG, "studyNote else: $studyNote")
                    cachedDoc(studyNote)

                }
            }
        }
    }

    fun retryDocFetch() {
        viewModelScope.launch(Dispatchers.IO) {
            cachedDoc(_studyNote.value)
        }
    }

    private fun cachedDoc(studyNote: StudyNote) {
        if (isTrying || studyNote.docUrl == "") {
            return
        }
        isTrying = true
        /*_docFile.value =
            FileState.Progress()*/
        // Create a storage reference from our app
        val storageRef = Firebase.storage.reference
        val studyNoteDocRef = storageRef.child(studyNote.docUrl)
        val localFile = File.createTempFile(studyNote.id, ".pdf")
        Log.d(TAG, "cachedDoc: local file ${localFile.toURI()}")

        studyNoteDocRef.getFile(localFile).addOnProgressListener {
            Log.d(TAG, "cachedDoc: progress total ${it.totalByteCount}")
            Log.d(TAG, "cachedDoc: progress transfered ${it.bytesTransferred}")
            _docFile.value =
                FileState.Progress(it.bytesTransferred.toFloat() / it.totalByteCount.toFloat())
        }.addOnSuccessListener {
            // Local temp file has been created
            Log.d(TAG, "cachedDoc: absolutePath ${localFile.absolutePath}")
            viewModelScope.launch(Dispatchers.IO) {
                noteRepository.updateStudyNoteLocalDoc(
                    studyNoteId, localFile.toURI().toString()
                )
            }
            isTrying = false

        }.addOnFailureListener {
            // Handle any errors
            Log.d(TAG, "cachedDoc: err $it")
            _docFile.value =
                FileState.Error
            isTrying = false
        }
    }
}


sealed class FileState {
    data class Progress(val progress: Float? = null) : FileState()
    data object Error : FileState()
    data class Success(val file: File) : FileState()
}