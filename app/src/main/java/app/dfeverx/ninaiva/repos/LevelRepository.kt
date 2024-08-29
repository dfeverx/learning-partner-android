package app.dfeverx.ninaiva.repos

import android.util.Log
import androidx.room.withTransaction
import app.dfeverx.ninaiva.db.AppDatabase
import app.dfeverx.ninaiva.models.local.Level
import app.dfeverx.ninaiva.models.local.StudyNote
import app.dfeverx.ninaiva.models.local.StudyNoteWithLevels
import app.dfeverx.ninaiva.models.local.genNextLevel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class LevelRepository @Inject constructor(private val appDatabase: AppDatabase) {
    private val TAG = "LevelRepository"


    fun studyNoteWithQuestions(studyNoteId: String): Flow<StudyNoteWithLevels?> {
        return appDatabase.levelDao().studyNoteWithLevels(studyNoteId)
    }


    //    use this only the note details and question are available in the db,return nextAttempt data so in vm schedule notification
    suspend fun createNewLevelOnlyIfNotExist(
        studyNoteId: String,
        score: Int,
        stage: Int,
        accuracy: Int,
        nextAttemptUnix: Long,
        isRevision: Boolean = false
    ): Long {
        return appDatabase.withTransaction {
            /*          val currentUnix = System.currentTimeMillis()
                      val newStage = stage + 1
          //            make sure that it is not double entry
                      val level = appDatabase.levelDao().levelByNoteId(studyNoteId)
                      val alreadyHaveLevel = level.find { lvl -> lvl.stage == newStage }
                      Log.d(TAG, "createNewLevel: $alreadyHaveLevel")
                      if (alreadyHaveLevel != null) {
                          Log.d(TAG, "createNewLevelOnlyIfNotExist: Double entry founded")
                          return@withTransaction -1
                      }
          //            find next interval
                      val attemptHistory = level.map { lvl -> lvl.accuracy }
                      val nextIntervalInDays =
                          calculateNextIntervalInDaysUsingSpaceRepetitionMinAccuracy40(
                              stage,
                              accuracy,
                              attemptHistory
                          )

                      Log.d(TAG, "createNewLevelOnlyIfNotExist: Next interval in days $nextIntervalInDays")
                      val nextAttemptUnix = currentUnix + nextIntervalInDays.days.inWholeMilliseconds
                      Log.d(TAG, "createNewLevelOnlyIfNotExist: nextAttempt unix $nextAttemptUnix")*/
//add current level accuracy to level
            appDatabase.levelDao().updateAccuracyAndScore(studyNoteId, stage, accuracy)

//            read study note details and all question belongs to that study note
            val studyNoteWithAllQuestions =
                appDatabase.studyNotesDao().studyNoteWithQuestions(studyNoteId)

            val isNextRevision = studyNoteWithAllQuestions?.studyNote?.totalLevel == stage

            Log.d(
                TAG,
                "createNewLevelOnlyIfNotExist: totalLevel ${studyNoteWithAllQuestions?.studyNote?.totalLevel}, stage $stage"
            )
            Log.d(TAG, "createNewLevelOnlyIfNotExist: $isNextRevision")
            val nextStage = if (isRevision) stage else stage + 1
//            update to study notes stage local db
            appDatabase
                .studyNotesDao()
                .updateStageNextAttemptScoreAccuracy(
                    studyNoteId = studyNoteWithAllQuestions!!.studyNote!!.id,
                    stage = nextStage,
                    nextLevelIn = if (isNextRevision) 0 else nextAttemptUnix, //unix timestamp
                    score = score,
                    isInLTM = isNextRevision,
                    accuracy = accuracy
                )
//            update note in the firestore
            updateStudyNoteInFirestore(
                studyNoteId = studyNoteWithAllQuestions.studyNote!!.id,
                stage = nextStage,
                nextLevelIn = if (isNextRevision) 0 else nextAttemptUnix,
                score = score,
                accuracy = accuracy
            )


//            create new level
//            only if it is not revision ,for revision it is create when added to the db
            return@withTransaction if (!isNextRevision && !isRevision) {
                appDatabase.levelDao()
                    .addLevel(

                        studyNoteWithAllQuestions.studyNote!!.genNextLevel(
                            allQuestions = studyNoteWithAllQuestions.questions!!,
                            stage = nextStage
                        )


                    )
            } else {
                -1
            }


        }
    }

    fun updateStudyNoteInFirestore(
        studyNoteId: String,
        stage: Int = 1,
        nextLevelIn: Long = 0,
        score: Int = 0,
        accuracy: Int = 0
    ) {
        val firestore = Firebase.firestore
        val uid = Firebase.auth.currentUser?.uid ?: return

        val hashMap: HashMap<String, *> = hashMapOf(
            Pair("currentStage", stage),
            Pair("nextLevelIn", nextLevelIn),
            Pair("score", score),
            Pair("accuracy", accuracy),
        )

        firestore
            .collection("users")
            .document(uid)
            .collection("notes")
            .document(studyNoteId)
            .update(
                hashMap
            )

    }

    suspend fun levelByNoteId(studyNoteId: String): List<Level> {
        return appDatabase.levelDao().levelByNoteId(studyNoteId)
    }

    suspend fun studyNoteById(noteId: String): StudyNote {
        return appDatabase.studyNotesDao().studyNoteByIdOneShot(noteId)
    }


}