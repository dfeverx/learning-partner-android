package app.dfeverx.ninaiva.ui.screens.home

import android.net.Uri
import android.util.Log
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.AutoDelete
import androidx.compose.material.icons.outlined.AutoFixHigh
import androidx.compose.material.icons.outlined.LibraryBooks
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.StickyNote2
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.dfeverx.ninaiva.datastore.CreditAndSubscriptionDataStore
import app.dfeverx.ninaiva.models.CreditAndSubscriptionInfo
import app.dfeverx.ninaiva.models.local.NOTE_PROCESSING_FUN_CALLING
import app.dfeverx.ninaiva.models.local.NOTE_PROCESSING_LOCAL_FILE_CORRUPTED
import app.dfeverx.ninaiva.models.local.NOTE_PROCESSING_UPLOADING_PDF
import app.dfeverx.ninaiva.models.local.NOTE_PROCESSING_UPLOADING_PDF_FAILED
import app.dfeverx.ninaiva.models.local.StudyNote
import app.dfeverx.ninaiva.models.remote.FunResponse
import app.dfeverx.ninaiva.models.remote.StudyNoteWithQuestionsFirestore
import app.dfeverx.ninaiva.repos.HomeRepository
import app.dfeverx.ninaiva.repos.StudyNoteRepository
import app.dfeverx.ninaiva.utils.FileUtils
import app.dfeverx.ninaiva.utils.TimePeriod
import app.dfeverx.ninaiva.utils.getTimePeriod
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.storage.FirebaseStorage
import com.google.gson.Gson
import com.google.mlkit.vision.documentscanner.GmsDocumentScanner
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.ocpsoft.prettytime.PrettyTime
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val homeRepository: HomeRepository,
    private val studyNoteRepository: StudyNoteRepository,
    private val prettyTime: PrettyTime, private val gson: Gson,
    private val auth: FirebaseAuth,
    private val storage: FirebaseStorage,
    private val functions: FirebaseFunctions,
    private val firestore: FirebaseFirestore,
    private val creditSubscriptionDataStore: CreditAndSubscriptionDataStore
) : ViewModel() {
    private val TAG = "HomeViewModel"

    class RepetitionSchedule(
        val date: Int,
        val month: String,
        val to: String,
        val timePeriod: TimePeriod
    )

    private val _repetitionSchedules: MutableStateFlow<List<RepetitionSchedule>> = MutableStateFlow(
        listOf()
    )

    val repetitionSchedules: StateFlow<List<RepetitionSchedule>>
        get() = _repetitionSchedules

    private val _studyNotes: MutableStateFlow<List<StudyNote>> =
        MutableStateFlow(
            (0..8).toList().map {
                StudyNote(
                    id = ""
                ).apply {
                    isPlaceholder = true
                    isProcessing = false
                }
            }

        )
    val studyNotes: StateFlow<List<StudyNote>>
        get() = _studyNotes

    private val _subjectsAndGrouping =
        MutableStateFlow(
            (0..8).toList().map {
                TextIcon(Icons.Outlined.LibraryBooks, "All", "").apply {
                    isPlaceholder = true
                }
            }
        )
    val subjectAndGrouping = _subjectsAndGrouping.asStateFlow()

    private val _selectedCategory = MutableStateFlow("all")
    val selectedCategory = _selectedCategory.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {

            _selectedCategory
                .flatMapLatest { category ->
                    homeRepository.studyNotes(category)
                }
                .collect { notes ->
                    _studyNotes.value = notes
                    val repetitionSchedules =
                        notes.filter { nt -> nt.nextLevelIn > 0 }.sortedBy { it.nextLevelIn }
                            .map { nt ->
                                val calendar = Calendar.getInstance().apply {
                                    timeInMillis = nt.nextLevelIn
                                }
                                // Extract the day of the month
                                val dateInt = calendar.get(Calendar.DAY_OF_MONTH)
                                // Extract the month as a string name
                                val monthStr = calendar.getDisplayName(
                                    Calendar.MONTH,
                                    Calendar.LONG,
                                    Locale.getDefault()
                                )

                                RepetitionSchedule(
                                    date = dateInt,
                                    month = monthStr ?: "",
                                    timePeriod = getTimePeriod(nt.nextLevelIn),
                                    to = nt.id
                                )
                            }
                    _repetitionSchedules.value = repetitionSchedules

                }
        }
        viewModelScope.launch(Dispatchers.IO) {
            homeRepository.subjects.collect { subjects ->
                delay(1000)
                val newSubjectAndGrouping = mutableListOf<TextIcon>()
                newSubjectAndGrouping.addAll(
                    listOf(
                        TextIcon(Icons.Outlined.LibraryBooks, "All", "all"),
                        TextIcon(Icons.Outlined.Star, "Starred", "starred"),
                        TextIcon(Icons.Outlined.MenuBook, "Learning", "learning"),
                        TextIcon(Icons.Outlined.Archive, "Archive", "archived"),

                        )
                )
//   todo:             make sure the already added item value not repeated in the list
                newSubjectAndGrouping.addAll(subjects.map { sub ->
                    if (sub == "") {
                        TextIcon(
                            Icons.Outlined.AutoFixHigh,
                            "Magic..",
                            sub
                        )
                    } else {
                        TextIcon(
                            Icons.Outlined.StickyNote2,
                            sub,
                            sub
                        )
                    }

                })
                newSubjectAndGrouping.add(TextIcon(Icons.Outlined.AutoDelete, "Trash", "trash"))
                _subjectsAndGrouping.value = newSubjectAndGrouping
            }
        }
    }

    fun isAnonymousUser(): Boolean {
        return auth.currentUser?.isAnonymous ?: true
    }

    fun getScanner(isPro: Boolean): GmsDocumentScanner {
        val options = GmsDocumentScannerOptions.Builder()
            .setGalleryImportAllowed(isPro)
            .setPageLimit(if (isAnonymousUser()) 1 else if (isPro) 3 else 2)
            .setResultFormats(
                GmsDocumentScannerOptions.RESULT_FORMAT_JPEG,
                GmsDocumentScannerOptions.RESULT_FORMAT_PDF
            )
            .setScannerMode(GmsDocumentScannerOptions.SCANNER_MODE_BASE)
            .build()

        return GmsDocumentScanning.getClient(options)
    }

    fun formatTime(time: Long): String {
        val date = Date(time)
        return prettyTime.format(date)
    }

    fun handleCategorySelection(value: String) {
        if (_selectedCategory.value == value) {
            _selectedCategory.value = "all"
            return
        }
        _selectedCategory.value = value
    }

    fun initCreateNote(pdfUri: Uri) = viewModelScope.launch(Dispatchers.IO) {
        val userId = auth.currentUser?.uid ?: throw IllegalStateException("User not authenticated")
        val noteId = firestore
            .collection("users")
            .document(userId)
            .collection("notes")
            .document().id
        homeRepository.addNote(noteId, pdfUri)
        val docUrl = uploadingPdfToStorage(userId, noteId, pdfUri)
            ?: //          failed to upload snackbar msg
            return@launch
        processFun(noteId = noteId, docUrl = docUrl)

    }

    fun retryCreateNote(studyNote: StudyNote) = viewModelScope.launch(Dispatchers.IO) {
        val userId = auth.currentUser?.uid ?: throw IllegalStateException("User not authenticated")
        val docUrl = if (studyNote.docUrl == "") {
//            upload pdf
            uploadingPdfToStorage(userId, studyNote.id, pdfUri = studyNote.docLocalUrl.toUri())
                ?: return@launch

        } else {
//            already uploaded
            studyNote.docUrl

        }
        processFun(noteId = studyNote.id, docUrl = docUrl, funName = "retryFromFailed")

    }


    private suspend fun uploadingPdfToStorage(
        userId: String,
        noteId: String,
        pdfUri: Uri,
        pdfName: String = "first.pdf"
    ): String? {


        val file = FileUtils.getFileFromUri(pdfUri.toString())
//                may the file is outdated there for check
        if (file?.exists() != true) {
//            make it as corrupted file
            studyNoteRepository.updateNoteStatus(
                noteId = noteId,
                status = NOTE_PROCESSING_LOCAL_FILE_CORRUPTED,
                isProcessing = false
            )
            return null
        }
        studyNoteRepository.updateNoteStatus(
            noteId = noteId,
            status = NOTE_PROCESSING_UPLOADING_PDF,
            isProcessing = true
        )
        val storagePath = "uploads/$userId/docs/$noteId/${pdfName}"
        val storageReference = storage.reference.child(storagePath)
        val uploadTask = storageReference.putFile(pdfUri)
        uploadTask.addOnProgressListener { taskSnapshot ->
            val progress =
                (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount).toInt()
            Log.d(TAG, "Upload is $progress% done")
//            update progress in ui
        }
        try {
            val uploadResult = uploadTask.await()
            if (uploadResult.task.isSuccessful) {
//                mark it as status uploaded
                studyNoteRepository.updatePdfStoragePath(noteId = noteId, storagePath = storagePath)
                return storagePath
            }

        } catch (_: Exception) {

        }
        studyNoteRepository.updateNoteStatus(
            noteId = noteId,
            status = NOTE_PROCESSING_UPLOADING_PDF_FAILED,
            isProcessing = false
        )
        return null
    }

    private suspend fun processFun(
        noteId: String,
        docUrl: String,
        funName: String = "processNote"
    ) {
        studyNoteRepository.updateNoteStatus(
            noteId = noteId,
            status = NOTE_PROCESSING_FUN_CALLING,
            isProcessing = true
        )
        // Prepare payload for cloud function call
        val payload = hashMapOf(
            "noteId" to noteId,
            "docUrl" to docUrl
        )
        Log.d(TAG, "processFun: payload $payload")
        // Call cloud function to process the note
        val processNoteCall = functions.getHttpsCallable(funName)
        val result = try {
            processNoteCall.call(payload).await()
        } catch (e: Exception) {
            Log.d(TAG, "processFun: $e")
            null
        }
        if (result == null) {
            studyNoteRepository.markedAsFailed(noteId)
            return
        }

        if (result.data != null) {
            val json = gson.toJson(result.data)
            Log.d(TAG, "createStudyNoteFromPdf: $json")
            println(json)
            val funRes = gson.fromJson(json, FunResponse::class.java)
            Log.d(TAG, "createStudyNoteFromPdf: $funRes")
            Log.d(TAG, "createStudyNoteFromPdf: ${funRes.credit.lastUpdated}")
            when (funRes.statusCode) {
                200 /*OK*/ -> {
                    studyNoteRepository.addStudyNoteAndQuestionsFromFirestore(funRes.data.apply {
                        this.id = noteId
                        this.docUrl = "storagePath"
                    })
                    creditSubscriptionDataStore.update(
                        CreditAndSubscriptionInfo(
                            credit = funRes.credit,
                            subscription = funRes.subscription
                        )
                    )
                }

                400/*NOTE DOCUMENT NOT EXIST FOR RETRY*/ -> {
                    studyNoteRepository.markedAsFailed(noteId)
                    creditSubscriptionDataStore.update(
                        CreditAndSubscriptionInfo(
                            credit = funRes.credit,
                            subscription = funRes.subscription
                        )
                    )
                }

                401 /*REQUEST INVALID,NO CREDIT INFO FOR THIS*/ -> {
                    studyNoteRepository.markedAsFailed(noteId)
                }

                500 /*CREDIT LIMIT REACHED*/ -> {
                    studyNoteRepository.markedAsFailed(noteId)
                    creditSubscriptionDataStore.update(
                        CreditAndSubscriptionInfo(
                            credit = funRes.credit,
                            subscription = funRes.subscription
                        )
                    )
                }

                else -> {
                    Log.d(
                        TAG,
                        "uploadPdf: processNote call failed ${funRes.statusCode}",
                    )
//     todo: notify error,marked as failed
                    studyNoteRepository.markedAsFailed(noteId)
                }
            }

        } else {
            Log.d(TAG, "funCall: processNote call returned no result")
            studyNoteRepository.markedAsFailed(noteId)
        }

    }




    private fun addFirestoreListenerForSixMinutes(form: Long) {
        firestore
            .collection("users")
            .document(auth.currentUser?.uid!!)
            .collection("notes")
            .whereGreaterThanOrEqualTo("createdAt", form)
            .addSnapshotListener { value, e ->
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e)
                    return@addSnapshotListener
                }

                value?.let { firestoreStudyNotesResultToLocalDb(it) }
                Log.d(TAG, "Realtime: ${value?.toHashSet()}")
            }


    }

    //    also in mainviewmodel
    private fun firestoreStudyNotesResultToLocalDb(result: QuerySnapshot) {
        val studyNoteList = mutableListOf<StudyNoteWithQuestionsFirestore>()
        for (document in result) {
            if (document.data.containsKey("status")) {
                val note =
                    document.toObject(StudyNoteWithQuestionsFirestore::class.java)
                        .apply {
                            id = document.id

                        }
                studyNoteList.add(note)
                viewModelScope.launch {
                    studyNoteRepository.addStudyNoteAndQuestionsFromFirestore(note)
                }
            }

        }
        // debug log
        for (note in studyNoteList) {
            Log.d(TAG, "Id: ${note.id}, title: ${note.title}")
        }
    }




}




