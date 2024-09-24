package app.dfeverx.ninaiva.receivers

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.provider.Settings
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat.getString
import androidx.core.net.toUri
import app.dfeverx.ninaiva.LearningPartnerApplication
import app.dfeverx.ninaiva.R
import app.dfeverx.ninaiva.repos.StudyNoteRepository
import app.dfeverx.ninaiva.ui.main.MainActivity
import app.dfeverx.ninaiva.utils.onTriggerAlarmAppLevel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class OnAlarmTriggeredReceiver : BroadcastReceiver() {
    private val TAG = "OnNotificationReceiver"

    @Inject
    lateinit var application: LearningPartnerApplication

    @Inject
    lateinit var studyNoteRepository: StudyNoteRepository

    @Inject
    lateinit var alaram: AlarmManager

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d(TAG, "onReceive: alarm triggered $context")
        context?.let {
            val noteId = intent?.getStringExtra("noteId")
//            note review notification
            if (noteId != null) {
                showNotification(
                    context = context,
                    title = "Time to revise lesson!",
                    description = "Unlock new level now , get streak and refresh your knowledge ",
                    toRouteInNavGraph = "ninaiva://app/noteId=${noteId}",
                    notificationId = noteId
                )
            }

            val appLevelNotification = intent?.getIntExtra("appLevelNotification", -1)
            var notificationId = "appLevelNotification"

//            app level notification
            if (appLevelNotification != null && appLevelNotification > -1) {
                GlobalScope.launch {
                    val unAttemptedNote = studyNoteRepository.getAllUnattemptedStudyNotes()
                    val notificationInfo = if (hasNoNotes() == 0) {
                        Triple(
                            "Scan your first note now!",
                            "Unlock better way to learn",
                            if (Firebase.auth.currentUser == null) "ninaiva://app/onboarding" else "ninaiva://app/home"
                        )
                    } else if (unAttemptedNote.isNotEmpty()) {
                        val firstNote = unAttemptedNote.first()
                        notificationId = firstNote.id
                        Triple(
                            "Attempt the quizzes that you haven't started",
                            "Complete ${firstNote.title} quiz and check your understanding and learn better",
                            "ninaiva://app/noteId=${firstNote.id}",

                            )
                    } else {
                        Triple(
                            "Review  flash cards now",
                            "Repetition is the key to master anything",
                            "ninaiva://app/home"
                        )
                    }
                    try {
                        withContext(Dispatchers.Main) {
                            showNotification(
                                context = context,
                                title = notificationInfo.first,
                                description = notificationInfo.second,
                                toRouteInNavGraph = notificationInfo.third,
                                notificationId = notificationId
                            )
                        }
                    } finally {

                    }
                }

            }
        }
    }

    private suspend fun hasNoNotes() = studyNoteRepository.allStudyNoteCount().first()


    private fun showNotification(
        title: String,
        description: String,
        toRouteInNavGraph: String,
        context: Context,
        notificationId: String
    ) {


        val taskDetailIntent = Intent(
            Intent.ACTION_VIEW,
            toRouteInNavGraph.toUri(),
            context,
            MainActivity::class.java
        )

        val pending: PendingIntent = TaskStackBuilder.create(context).run {

            addNextIntentWithParentStack(taskDetailIntent)

            getPendingIntent(
                notificationId.hashCode(),
                PendingIntent.FLAG_IMMUTABLE
                /* or PendingIntent.FLAG_UPDATE_CURRENT*/
            )
        }

        val builder = NotificationCompat.Builder(
            context,
            getString(context, R.string.scheduled_notification_channel_id)
        )
            .setContentIntent(pending)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(description)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVibrate(longArrayOf(0, 500, 1000, 500))

        builder.setLights(Color.WHITE, 2000, 3000);
        builder
            .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
            notify(notificationId.hashCode(), builder.build())
        }
    }


}