package app.dfeverx.ninaiva.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import app.dfeverx.ninaiva.LearningPartnerApplication
import app.dfeverx.ninaiva.receivers.OnAlarmTriggeredReceiver
import java.util.Calendar

fun AlarmManager.scheduleNotificationForNextSevenDays(application: LearningPartnerApplication) {

    val currentTimeStamp = System.currentTimeMillis()
    val calendar = Calendar.getInstance()
    for (i in 1..7) {
        calendar.timeInMillis = currentTimeStamp
        calendar.add(Calendar.HOUR_OF_DAY, i)
        val notificationId = i
        val intent = Intent(application, OnAlarmTriggeredReceiver::class.java).apply {
            this.putExtra("appLevelNotification", i)
        }

        val pendingIntent =
            PendingIntent.getBroadcast(
                application, notificationId,
                intent,
                PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Log.d(
                "TAG",
                "scheduleNotificationForNextAttempt: ${this.canScheduleExactAlarms()}"
            )
            if (this.canScheduleExactAlarms()) {
                this.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            }
        } else {
            this.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        }
    }


}

fun onTriggerAlarmAppLevel(context: Context, times: Int = 0) {

}