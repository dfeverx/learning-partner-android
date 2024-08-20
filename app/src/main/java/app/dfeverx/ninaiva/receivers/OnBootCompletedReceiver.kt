package app.dfeverx.ninaiva.receivers

import android.app.AlarmManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import app.dfeverx.ninaiva.LearningPartnerApplication
import app.dfeverx.ninaiva.models.local.StudyNote
import app.dfeverx.ninaiva.repos.StudyNoteRepository
import app.dfeverx.ninaiva.utils.TimePeriod
import app.dfeverx.ninaiva.utils.getTimePeriod
import app.dfeverx.ninaiva.utils.scheduleNotificationForNextAttempt
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext


@AndroidEntryPoint
class OnBootCompletedReceiver : BroadcastReceiver() {
    private val TAG = "BootCompletedReceiver"

    @Inject
    lateinit var application: LearningPartnerApplication

    @Inject
    lateinit var studyNoteRepository: StudyNoteRepository

    @Inject
    lateinit var alaram: AlarmManager

    override fun onReceive(context: Context, intent: Intent?) = goAsync {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            // Reschedule alarms here if necessary
            Log.d(TAG, "onReceive: Boot completed")
            val allStudyNotes = studyNoteRepository.allStudyNoted()
            alaram.scheduleAlarm(allStudyNotes, application)
        }
    }


}




fun AlarmManager.scheduleAlarm(
    allStudyNotes: List<StudyNote>,
    application: LearningPartnerApplication
) {
    val TAG = "schedule alarm"
    Log.d(TAG, "streakValidation: ${allStudyNotes.size}")
    val futureNotes = allStudyNotes.map {
        Log.d(TAG, "streakValidation: ${getTimePeriod(it.nextLevelIn)}")
        Pair(getTimePeriod(it.nextLevelIn), it)
    }
        .filter { it.first != TimePeriod.PAST }
    Log.d(TAG, "streakValidation: $futureNotes")
    Log.d(TAG, "streakValidation: ${futureNotes.size}")
    if (futureNotes.isNotEmpty()) {
        futureNotes.map { fn ->
            this.scheduleNotificationForNextAttempt(
                application = application,
                nextAttemptUnix = fn.second.nextLevelIn,
                noteId = fn.second.id
            )
        }

    }
}


    fun BroadcastReceiver.goAsync(
        context: CoroutineContext = EmptyCoroutineContext,
        block: suspend CoroutineScope.() -> Unit
    ) {
        val pendingResult = goAsync()
        @OptIn(DelicateCoroutinesApi::class) // Must run globally; there's no teardown callback.
        GlobalScope.launch(context) {
            try {
                block()
            } finally {
                pendingResult.finish()
            }
        }
    }