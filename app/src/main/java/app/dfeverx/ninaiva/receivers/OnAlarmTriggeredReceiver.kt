package app.dfeverx.ninaiva.receivers

import android.Manifest
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
import app.dfeverx.ninaiva.R
import app.dfeverx.ninaiva.ui.main.MainActivity

class OnAlarmTriggeredReceiver : BroadcastReceiver() {
    private val TAG = "OnNotificationReceiver"

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d(TAG, "onReceive: alarm triggered $context")
        context?.let {
            val noteId = intent?.getStringExtra("noteId")
            val taskDetailIntent = Intent(
                Intent.ACTION_VIEW,
                "ninaiva://app/noteId=${noteId}".toUri(),
                context,
                MainActivity::class.java
            )

            val pending: PendingIntent = TaskStackBuilder.create(context).run {
                addNextIntentWithParentStack(taskDetailIntent)
                getPendingIntent(
                    noteId.hashCode(),
                    PendingIntent.FLAG_IMMUTABLE/* or PendingIntent.FLAG_UPDATE_CURRENT*/
                )
            }

            val builder = NotificationCompat.Builder(
                it,
                getString(context, R.string.scheduled_notification_channel_id)
            )
                .setContentIntent(pending)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Time to revise lesson!")
                .setContentText("Unlock new level now , get streak and refresh your knowledge ")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setVibrate(longArrayOf(0, 500, 1000, 500))

            builder.setLights(Color.WHITE, 2000, 3000);
            builder.setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                .setAutoCancel(true)

            with(NotificationManagerCompat.from(it)) {
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
                notify(noteId.hashCode(), builder.build())
            }
        }
    }


}