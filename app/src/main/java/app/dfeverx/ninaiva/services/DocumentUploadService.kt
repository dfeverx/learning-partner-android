package app.dfeverx.ninaiva.services

import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.net.Uri
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import app.dfeverx.ninaiva.R
import app.dfeverx.ninaiva.models.local.StudyNote
import app.dfeverx.ninaiva.models.remote.StudyNoteWithQuestionsFirestore
import app.dfeverx.ninaiva.repos.StudyNoteRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.getHttpsCallable
import com.google.firebase.functions.ktx.functions
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.storageMetadata
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject


@AndroidEntryPoint
class DocumentUploadService : Service() {
    private val TAG = "DocumentUploadService"

    @Inject
    lateinit var studyNoteRepo: StudyNoteRepository
    private lateinit var storageReference: StorageReference
    private lateinit var notificationManager: NotificationManager
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var functions: FirebaseFunctions
    private var noteListenerRegistration: ListenerRegistration? = null
    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)
    override fun onCreate() {
        super.onCreate()
        Log.d("DocumentUploadService created", "")
        db = FirebaseFirestore.getInstance()
        firebaseAuth = Firebase.auth
        functions = Firebase.functions
        storageReference = FirebaseStorage.getInstance().reference
        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val pdfUri: Uri? = intent?.data // Get the PDF Uri from the intent

        if (pdfUri != null && firebaseAuth.currentUser != null) {
            uploadPdf(pdfUri, firebaseAuth.currentUser!!)
        }

        return START_NOT_STICKY
    }

    private fun uploadPdf(pdfUri: Uri, currentUser: FirebaseUser) {

        Log.d(TAG, "uploadPdf: $currentUser/$pdfUri ")
        val noteId = System.currentTimeMillis()
        //     1.   insert local first
        scope.launch {
            studyNoteRepo.addStudyNote(StudyNote(id = noteId.toString()).apply {
                docLocalUrl = pdfUri.toString()
            })
        }

        val firebaseStoragePath =
            "uploads/${currentUser.uid}/docs/${noteId}/${pdfUri.lastPathSegment}"
        val fileReference =
            storageReference.child(firebaseStoragePath)

        val metadata = storageMetadata {
            setCustomMetadata("pages", " 3")
            setCustomMetadata("srcLng", "en,ml")
        }

        val uploadTask = fileReference.putFile(pdfUri, metadata)

        // Create an initial notification
        val notification =
            NotificationCompat.Builder(
                this,
                getString(R.string.study_note_uploading_notification_channel_id)
            )
                .setContentTitle("PDF Upload")
                .setContentText("Uploading...")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setProgress(100, 0, true)
                .build()

        startForeground(1, notification)

        uploadTask
            .addOnProgressListener { taskSnapshot ->
                val progress =
                    (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount).toInt()
                println("Upload is $progress% done")
                updateNotification(progress)
            }
            .addOnSuccessListener {

//create payload
                val payload = hashMapOf(
                    "docUrl" to firebaseStoragePath,
                )

                val processNoteCall = functions
                    .getHttpsCallable("processNote") {
                        limitedUseAppCheckTokens = true
                    }

                try {
                    scope.launch {
                        val result = processNoteCall.call()
                            .await()


                        if (result.data == null) {
                            Log.d(TAG, "uploadPdf:processNote call no result found ")
                            return@launch
                        }
                        val resultStudyNoteWithQuestions =
                            result.data as StudyNoteWithQuestionsFirestore
                    }
                } catch (e: Exception) {
                    Log.d(TAG, "uploadPdf:Call processNote failed exception $e")
                }


                /*                Log.d(TAG, "uploadPdf: success $it")
                //                notificationManager.cancel(1)
                                updateNotificationComplete()
                //                stopSelf()
                                val docRef =
                                    db
                                        .collection("users")
                                        .document(firebaseAuth.currentUser!!.uid)
                                        .collection("notes")
                                        .document(noteId.toString())
                                docRef.set(
                                    hashMapOf(
                                        "docUrl" to filePathInStorage,

                                    )
                                )
                                noteListenerRegistration?.remove()
                                noteListenerRegistration = noteRealtimeListener(
                                    firestore = db,
                                    uid = currentUser.uid,
                                    noteId = noteId,//todo null check handle
                                    update = { snapshot, e ->
                                        if (e != null) {
                                            Log.d(TAG, "uploadPdf: Something went wrong " + e.message)

                                        }
                                        if (snapshot?.data?.containsKey("status") == true) {
                                            snapshot.toObject(StudyNoteWithQuestionsFirestore::class.java)
                                                ?.apply { id = snapshot.id }?.let {
                                                    scope.launch {
                                                        val k =
                                                            studyNoteRepo.addStudyNoteAndQuestionsFromFirestore(it)
                                                        noteListenerRegistration?.remove()
                                                        notificationManager.cancel(1)
                                                        stopSelf()
                                                    }
                                                }
                                        }


                                        Log.d(
                                            TAG, "updatexx : realtimeListener $snapshot"
                                        )
                                    }
                                )*/
            }.addOnFailureListener {
                noteListenerRegistration?.remove()
                notificationManager.cancel(1)
                Log.d(TAG, "uploadPdf: failed $it")
                updateNotificationFailure()
                stopSelf()
            }

    }

    private fun updateNotification(progress: Int) {
        val notification =
            NotificationCompat.Builder(
                this,
                getString(R.string.study_note_uploading_notification_channel_id)
            )
                .setContentTitle("PDF Upload")
                .setContentText("Uploading...")
                .setOngoing(true)
                .setSilent(true)
                .setSmallIcon(R.drawable.ic_launcher_foreground) // Replace with your icon
                .setProgress(100, progress, false)
                .build()

        notificationManager.notify(1, notification)
    }

    private fun updateNotificationComplete() {
        Log.d(TAG, "updateNotificationComplete: ")
        val notification =
            NotificationCompat.Builder(
                this,
                getString(R.string.study_note_uploading_notification_channel_id)
            )
                .setContentTitle("PDF Upload")
                .setContentText("Upload complete")
                .setSmallIcon(R.drawable.ic_launcher_foreground) // Replace with your icon
                .setOngoing(false)
                .build()

        notificationManager.notify(2, notification)
    }

    private fun updateNotificationFailure() {
        val notification =
            NotificationCompat.Builder(
                this,
                getString(R.string.study_note_uploading_notification_channel_id)
            )
                .setContentTitle("PDF Upload")
                .setContentText("Upload failed")
                .setSmallIcon(R.drawable.ic_launcher_foreground) // Replace with your icon
                .build()

        notificationManager.notify(2, notification)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        noteListenerRegistration?.remove()
        job.cancel()
    }
}

fun noteRealtimeListener(
    firestore: FirebaseFirestore,
    uid: String, noteId: Long,
    update: (DocumentSnapshot?, FirebaseFirestoreException?) -> Unit
): ListenerRegistration {
    val docRef =
        firestore
            .collection("users")
            .document(uid)
            .collection("notes")
            .document(noteId.toString())
    return docRef
        .addSnapshotListener { snapshot, e ->
            Log.d("firestore", "realtimeListener: error  $e")
            Log.d("firestore", "realtimeListener:  ${snapshot?.data}")
            update(snapshot, e)
        }

}