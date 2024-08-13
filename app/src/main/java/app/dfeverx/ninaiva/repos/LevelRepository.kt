package app.dfeverx.ninaiva.repos

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
        studyNoteId: String, score: Int, stage: Int, accuracy: Int, nextAttemptUnix: Long
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


//            update to study notes stage local db
            appDatabase
                .studyNotesDao()
                .updateStageNextAttemptScoreAccuracy(
                    studyNoteId = studyNoteWithAllQuestions!!.studyNote!!.id,
                    stage = stage + 1,
                    nextLevelIn = nextAttemptUnix, //unix timestamp
                    score = score,
                    accuracy = accuracy
                )
//            update note in the firestore
            updateStudyNoteInFirestore(
                studyNoteId = studyNoteWithAllQuestions.studyNote!!.id,
                stage = stage + 1,
                nextLevelIn = nextAttemptUnix,
                score = score,
                accuracy = accuracy
            )


//            create new level
            appDatabase.levelDao()
                .addLevel(
                    if (studyNoteWithAllQuestions.studyNote?.totalLevel == stage) {
                        Level(
                            stage = stage + 1,
                            studyNoteId = studyNoteId,
                            questionIds = studyNoteWithAllQuestions.questions?.map { it.id }
                                ?: listOf(),
                            isCompleted = false,
                            isRevision = true
                        )
                    } else {
                        studyNoteWithAllQuestions.studyNote!!.genNextLevel(
                            allQuestions = studyNoteWithAllQuestions.questions!!,
                            stage = stage + 1
                        )
                    }

                )


        }
    }

    private fun updateStudyNoteInFirestore(
        studyNoteId: String,
        stage: Int,
        nextLevelIn: Long,
        score: Int,
        accuracy: Int
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