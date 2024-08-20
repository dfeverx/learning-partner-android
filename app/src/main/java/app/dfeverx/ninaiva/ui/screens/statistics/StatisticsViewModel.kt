package app.dfeverx.ninaiva.ui.screens.statistics

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.dfeverx.ninaiva.LearningPartnerApplication
import app.dfeverx.ninaiva.datastore.StreakDataStore
import app.dfeverx.ninaiva.receivers.OnAlarmTriggeredReceiver
import app.dfeverx.ninaiva.repos.LevelRepository
import app.dfeverx.ninaiva.utils.TimePeriod
import app.dfeverx.ninaiva.utils.getTimePeriod
import app.dfeverx.ninaiva.utils.scheduleNotificationForNextAttempt
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.pow
import kotlin.time.Duration.Companion.days

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val levelRepository: LevelRepository,
    private val alarmManager: AlarmManager,
    private val application: LearningPartnerApplication,
    private val streakDataStore: StreakDataStore
) : ViewModel() {
    private val TAG = "StatisticsViewModel"
    private val MINIMUM_THRESHOLD_ACCURACY_FOR_NEXT_LEVEL = 60
    private val noteId: String = checkNotNull(savedStateHandle["noteId"])
    private val currentLevelId: Long = checkNotNull(savedStateHandle["levelId"])
    private val score: Int = checkNotNull(savedStateHandle["score"])
    private val isRevision: Boolean = checkNotNull(savedStateHandle["isRevision"])
    private val attemptCount: Int =
        checkNotNull(savedStateHandle["attemptCount"])//totalQuestionAttemptedCount
    private val totalNumberOfQuestions: Int =
        checkNotNull(savedStateHandle["totalNumberOfQuestions"])
    private val stage: Int =
        checkNotNull(savedStateHandle["stage"])
    private val accuracy = ((score.toFloat() / totalNumberOfQuestions) * 100).toInt()


    private val _uiState =
        MutableStateFlow(
            StatisticsUiState(
                stage = stage,
                score = score,
                accuracy = accuracy,
                isSuccessfulAttempt = accuracy >= MINIMUM_THRESHOLD_ACCURACY_FOR_NEXT_LEVEL,
//                levelOverAllProgress =((noteDetails.currentStage - 1)) / noteDetails.totalLevel.toFloat()
            )
        )
    val uiState: StateFlow<StatisticsUiState> = _uiState.asStateFlow()


    init {
        viewModelScope.launch {
//            minimum accuracy threshold for creating new level is 60% accuracy other wise show failed retry
            if (accuracy >= MINIMUM_THRESHOLD_ACCURACY_FOR_NEXT_LEVEL) {

                val nextAttemptUnix = calculateNextAttempt()
                if (nextAttemptUnix == null) {
                    Log.d(TAG, "nextAttemptUnix: $nextAttemptUnix")
                    return@launch
                }
                try {
                    giveStreak(noteId)
                } catch (e: Exception) {
                    Log.d(TAG, "giveStreak Error: $e     ")
                }

                val levelCreationResult =
                    levelRepository.createNewLevelOnlyIfNotExist(
                        studyNoteId = noteId,
                        stage = stage,
                        score = score,
                        accuracy = accuracy,
                        nextAttemptUnix = nextAttemptUnix
                    )
//                todo: show the level creation message if failed option to retry
//               clear all notification and create  schedule notification
                /*  scheduleNotificationForNextAttempt(*//*nextAttemptUnix*//*
                    System.currentTimeMillis() + (60 * 1000),
                    noteId = noteId
                )*/
                alarmManager.scheduleNotificationForNextAttempt(
                    application = application,
                    nextAttemptUnix = nextAttemptUnix,
                    noteId = noteId
                )
                Log.d(TAG, "Init: level creation $levelCreationResult")


            }
        }

        viewModelScope.launch (Dispatchers.IO){
            val studyNote = levelRepository.studyNoteById(noteId)
            _uiState.value = _uiState.value.copy(levelOverAllProgress = ((studyNote.currentStage - 1)) / studyNote.totalLevel.toFloat())
        }
    }


    private suspend fun giveStreak(noteId: String) {
//        todo: update local , firestore
        Log.d(TAG, "giveStreak: ...")
        val studyNote = levelRepository.studyNoteById(noteId)
        if (studyNote.currentStage == stage && getTimePeriod(studyNote.nextLevelIn) == TimePeriod.TODAY) {
            Log.d(TAG, "giveStreak: eligible for streak")
            streakDataStore.incrementStreak()
        } else {
            Log.d(TAG, "giveStreak: not eligible for streak")
        }
    }


    private suspend fun calculateNextAttempt(): Long? {
        val currentUnix = System.currentTimeMillis()
        val newStage = stage + 1
//            make sure that it is not double entry
        val level = levelRepository.levelByNoteId(noteId)
        val alreadyHaveLevel = level.find { lvl -> lvl.stage == newStage }
        Log.d(TAG, "createNewLevel: $alreadyHaveLevel")
        if (alreadyHaveLevel != null) {
            Log.d(TAG, "createNewLevelOnlyIfNotExist: Double entry founded")
            return null
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
        Log.d(TAG, "createNewLevelOnlyIfNotExist: nextAttempt unix $nextAttemptUnix")
        return nextAttemptUnix
    }

    fun playRetry(): String {
        return "/${noteId}/${currentLevelId}/${stage}/${isRevision}"
    }

    //    minimum  40 required
    private fun calculateNextIntervalInDaysUsingSpaceRepetitionMinAccuracy40(
        currentLevel: Int,
        accuracy: Int,
        userPerformance: List<Int>
    ): Int {
        if (accuracy !in 40..100) {
            throw IllegalArgumentException("accuracy must be between 10 and 100")
        }

        // Calculate base interval with exponential growth
        val baseInterval = 2.0.pow((currentLevel /*-1*/).toDouble()).toInt()

        // Determine accuracy factor
        val accuracyFactor = when (accuracy) {
            in 40..49 -> 0.5
            in 50..59 -> 0.75
            in 60..69 -> 1.0
            in 70..79 -> 1.25
            in 80..89 -> 1.5
            in 90..100 -> 2.0
            else -> throw IllegalArgumentException("Invalid accuracy")
        }

        // Calculate user performance factor
        val averagePerformance = userPerformance.average()
        val performanceFactor = when (averagePerformance) {
            in 0.0..59.9 -> 0.5
            in 60.0..69.9 -> 0.75
            in 70.0..79.9 -> 1.0
            in 80.0..89.9 -> 1.25
            in 90.0..100.0 -> 1.5
            else -> throw IllegalArgumentException("Invalid average performance")
        }

        // Introduce a decay factor for poor performance over time
        val decayFactor = if (userPerformance.any { it < 50 }) 0.75 else 1.0

        // Calculate final interval
        return (baseInterval * accuracyFactor * performanceFactor * decayFactor).toInt()
            .coerceAtLeast(1)
    }
}

