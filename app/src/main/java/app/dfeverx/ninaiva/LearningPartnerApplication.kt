package app.dfeverx.ninaiva

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.os.StrictMode
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class LearningPartnerApplication : Application() {


    override fun onCreate() {
        super.onCreate()
//        study note upload related channel
        createNotificationChannel(
            notificationChannelId = getString(
                R.string.study_note_uploading_notification_channel_id
            ),
            name = getString(R.string.study_note_uploading_notification_name),
            description = getString(R.string.study_note_uploading_notification_description)
        )
//        scheduled attempt related channel
        createNotificationChannel(
            notificationChannelId = getString(
                R.string.scheduled_notification_channel_id
            ),
            name = getString(R.string.scheduled_notification_name),
            description = getString(R.string.scheduled_notification_description)
        )
        val builder = StrictMode.VmPolicy.Builder()
        StrictMode.setVmPolicy(builder.build())
    }

    private fun createNotificationChannel(
        notificationChannelId: String,
        name: String,
        description: String
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                notificationChannelId,
                name,
                NotificationManager.IMPORTANCE_DEFAULT
            )
            serviceChannel.description = description
            val manager = getSystemService(
                NotificationManager::class.java
            )
            manager.createNotificationChannel(serviceChannel)
        }
    }
}