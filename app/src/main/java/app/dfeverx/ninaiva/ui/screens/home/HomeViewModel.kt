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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.dfeverx.ninaiva.datastore.CreditAndSubscriptionDataStore
import app.dfeverx.ninaiva.models.CreditAndSubscriptionInfo
import app.dfeverx.ninaiva.models.local.StudyNote
import app.dfeverx.ninaiva.models.remote.FunResponse
import app.dfeverx.ninaiva.models.remote.StudyNoteWithQuestionsFirestore
import app.dfeverx.ninaiva.repos.HomeRepository
import app.dfeverx.ninaiva.repos.StudyNoteRepository
import app.dfeverx.ninaiva.utils.TimePeriod
import app.dfeverx.ninaiva.utils.getTimePeriod
import com.google.firebase.Timestamp
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
//            GmsDocumentScannerOptions.RESULT_FORMAT_JPEG,
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

    /*    fun createStudyNoteFromPdf(pdfUri: Uri) {
    //        todo:must have auth before continue
            val currentMilliUnix = System.currentTimeMillis()

            val studyNoteRef =
                firestore
                    .collection("users")
                    .document(auth.currentUser?.uid!!)
                    .collection("notes").document()

            val studyNoteId = studyNoteRef.id
            val firebaseStoragePath =
                "uploads/${auth.currentUser?.uid}/docs/${studyNoteId}/${pdfUri.lastPathSegment}"

            Log.d(TAG, "addNote: $pdfUri")

            viewModelScope.launch(Dispatchers.IO) {
    //            insert to local db
                val r = homeRepository.addNote(studyNoteId, pdfUri)

    //            upload to firebase storage
                val storageReference =
                    storage.reference.child(firebaseStoragePath)

                val metadata = storageMetadata {
                    *//*setCustomMetadata("pages", " 3")
                setCustomMetadata("srcLng", "en,ml")*//*
            }

            val uploadTask = storageReference.putFile(pdfUri, metadata)
            uploadTask.addOnProgressListener { taskSnapshot ->
                val progress =
                    (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount).toInt()
                println("Upload is $progress% done")
            }
            val uploadResult = uploadTask.await()
            if (uploadResult.task.isSuccessful) {
                Log.d(TAG, "uploadPdf: success $uploadResult")

//              update to local status
                homeRepository.updateStatusOfStudyNote(studyNoteId, 0)
                val payload = hashMapOf(
                    "noteId" to studyNoteId,
                    "docUrl" to firebaseStoragePath,
                )
//             call function
                val processNoteCall = functions
                    .getHttpsCallable("processNote") {
//                        limitedUseAppCheckTokens = true

                    }

                try {

                    val result = processNoteCall.call(payload)
                        .await()


                    if (result.data == null) {
                        Log.d(TAG, "uploadPdf:processNote call no result found ")
                        return@launch
                    }
                    val hashMapResult = result.data
                    Log.d(TAG, "createStudyNoteFromPdf: response data $hashMapResult")
                    val json = gson.toJson(hashMapResult)
                    Log.d(TAG, "createStudyNoteFromPdf: response data json $json")
                    val resultStudyNoteWithQuestions =
                        gson.fromJson(json, StudyNoteWithQuestionsFirestore::class.java)
                    resultStudyNoteWithQuestions?.apply {
                        id = studyNoteId
                    }
                    studyNoteRepository.addStudyNoteAndQuestionsFromFirestore(
                        resultStudyNoteWithQuestions
                    )

                } catch (e: Exception) {
                    Log.d(TAG, "uploadPdf:Call processNote failed exception $e")
                    addFirestoreListenerForSixMinutes(
                        currentMilliUnix
                    )
                }

            }


        }
    }*/

    fun createStudyNoteFromPdf(pdfUri: Uri) {
        // Ensure the user is authenticated before proceeding
        val userId = auth.currentUser?.uid ?: throw IllegalStateException("User not authenticated")
        val currentMillis = System.currentTimeMillis()
        val studyNoteRef = firestore.collection("users")
            .document(userId)
            .collection("notes")
            .document()

        val studyNoteId = studyNoteRef.id
        val storagePath = "uploads/$userId/docs/$studyNoteId/${pdfUri.lastPathSegment}"

        Log.d(TAG, "addNote: $pdfUri")

        viewModelScope.launch(Dispatchers.IO) {
            // Insert note into local database
            homeRepository.addNote(studyNoteId, pdfUri)

            // Upload PDF to Firebase Storage
            val storageReference = storage.reference.child(storagePath)
            val uploadTask = storageReference.putFile(pdfUri)

            uploadTask.addOnProgressListener { taskSnapshot ->
                val progress =
                    (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount).toInt()
                Log.d(TAG, "Upload is $progress% done")
            }

            try {
                val uploadResult = uploadTask.await()
                if (uploadResult.task.isSuccessful) {
                    Log.d(TAG, "uploadPdf: success $uploadResult")

                    // Update local status
                    homeRepository.updateStatusOfStudyNote(studyNoteId, 0)

                    // Prepare payload for cloud function call
                    val payload = hashMapOf(
                        "noteId" to studyNoteId,
                        "docUrl" to storagePath
                    )

                    // Call cloud function to process the note
                    val processNoteCall = functions.getHttpsCallable("processNote")
                    val result = processNoteCall.call(payload).await()

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
                                    id = studyNoteId
                                    docUrl = storagePath
                                })
                                creditSubscriptionDataStore.update(
                                    CreditAndSubscriptionInfo(
                                        credit = funRes.credit,
                                        subscription = funRes.subscription
                                    )
                                )
                            }

                            400/*NOTE DOCUMENT NOT EXIST FOR RETRY*/ -> {
                                studyNoteRepository.markedAsFailed(studyNoteId)
                                creditSubscriptionDataStore.update(
                                    CreditAndSubscriptionInfo(
                                        credit = funRes.credit,
                                        subscription = funRes.subscription
                                    )
                                )
                            }

                            401 /*REQUEST INVALID,NO CREDIT INFO FOR THIS*/ -> {
                                studyNoteRepository.markedAsFailed(studyNoteId)
                            }

                            500 /*CREDIT LIMIT REACHED*/ -> {
                                studyNoteRepository.markedAsFailed(studyNoteId)
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
//                                todo: notify error,marked as failed
                                studyNoteRepository.markedAsFailed(studyNoteId)
                            }
                        }
                        /* val studyNoteWithQuestions =
                             gson.fromJson(json, StudyNoteWithQuestionsFirestore::class.java)
                                 ?.apply {
                                     id = studyNoteId
                                     docUrl = storagePath

                                 }
                         studyNoteWithQuestions?.let {
                             studyNoteRepository.addStudyNoteAndQuestionsFromFirestore(it)
                         }*/
                    } else {
                        Log.d(TAG, "uploadPdf: processNote call returned no result")
                    }
                }
            } catch (e: Exception) {
                Log.d(TAG, "uploadPdf: Call to processNote failed", e)
                addFirestoreListenerForSixMinutes(currentMillis)
            }
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

    fun retryStudyNoteGen(studyNote: StudyNote) {
        viewModelScope.launch(Dispatchers.IO) {
            studyNoteRepository.retrying(studyNote.id)
            try {
//               handle  the uploading failed

                // Prepare payload for cloud function call
                val payload = hashMapOf(
                    "noteId" to studyNote.id,
//                "docUrl" to studyNote.docUrl
                )

                // Call cloud function to process the note
                val processNoteCall = functions.getHttpsCallable("retryFromFailed")
                val result = processNoteCall.call(payload).await()

                if (result.data != null) {
                    /*val json = gson.toJson(result.data)
                    val studyNoteWithQuestions =
                        gson.fromJson(json, StudyNoteWithQuestionsFirestore::class.java)
                            ?.apply {
                                id = studyNote.id
                            }
                    studyNoteWithQuestions?.let {
                        studyNoteRepository.addStudyNoteAndQuestionsFromFirestore(it)
                    }*/
                    val json = gson.toJson(result.data)
                    Log.d(TAG, "createStudyNoteFromPdf: $json")
                    val funRes = gson.fromJson(json, FunResponse::class.java)
                    Log.d(TAG, "createStudyNoteFromPdf: $funRes")
                    when (funRes.statusCode) {
                        200 /*OK*/ -> {
                            studyNoteRepository.addStudyNoteAndQuestionsFromFirestore(funRes.data.apply {
                                id = studyNote.id
//                                 = studyNote.docLocalUrl
                            })
                            creditSubscriptionDataStore.update(
                                CreditAndSubscriptionInfo(
                                    credit = funRes.credit,
                                    subscription = funRes.subscription
                                )
                            )
                        }

                        400/*NOTE DOCUMENT NOT EXIST FOR RETRY*/ -> {
                            studyNoteRepository.markedAsFailed(studyNote.id)
                            creditSubscriptionDataStore.update(
                                CreditAndSubscriptionInfo(
                                    credit = funRes.credit,
                                    subscription = funRes.subscription
                                )
                            )
                        }

                        401 /*REQUEST INVALID,NO CREDIT INFO FOR THIS*/ -> {
                            studyNoteRepository.markedAsFailed(studyNote.id)
                        }

                        500 /*CREDIT LIMIT REACHED*/ -> {
                            studyNoteRepository.markedAsFailed(studyNote.id)
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
//                                todo: notify error,marked as failed
                            studyNoteRepository.markedAsFailed(studyNote.id)
                        }
                    }
                } else {
                    Log.d(TAG, "uploadPdf: processNote call returned no result")
                }

            } catch (e: Exception) {
                Log.d(TAG, "retryStudyNoteGen: error $e")
            }

        }
    }


}




