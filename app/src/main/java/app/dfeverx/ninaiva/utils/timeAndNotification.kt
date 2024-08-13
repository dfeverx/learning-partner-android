package app.dfeverx.ninaiva.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.util.Log
import app.dfeverx.ninaiva.LearningPartnerApplication
import app.dfeverx.ninaiva.receivers.OnAlarmTriggeredReceiver
import java.util.*

fun Long.getNotificationTimes(): List<Pair<Long, String>>? {
    val currentTime = System.currentTimeMillis()
    // If the given timestamp is in the past, return null
    if (this < currentTime) {
        Log.d("TAG", "getNotificationTimes: time is past")
        return null
    }

    val calendar = Calendar.getInstance()
    calendar.timeInMillis = this

    // Extract the year, month, and day from the input timestamp
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val day = calendar.get(Calendar.DAY_OF_MONTH)

    // Current time in milliseconds
    val now = Calendar.getInstance()

    // Check if today is the same day as the input timestamp
    val isToday = now.get(Calendar.YEAR) == year &&
            now.get(Calendar.MONTH) == month &&
            now.get(Calendar.DAY_OF_MONTH) == day

    if (isToday) {
        // Calculate the remaining time in the day
        val endOfDay = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis

        val remainingTime = endOfDay - now.timeInMillis

        if (remainingTime > 0) {
            // Distribute notification times evenly across the remaining time
            val interval = remainingTime / 3

            val firstTime = now.timeInMillis + interval
            val secondTime = firstTime + interval
            val thirdTime = secondTime + interval

            return listOf(
                Pair(firstTime, "m"),
                Pair(secondTime, "n"),
                Pair(thirdTime, "e"),
            )
        }
    }

    // If the input timestamp is not today, use standard morning, noon, and evening times
    val morningTime = Calendar.getInstance().apply {
        set(Calendar.YEAR, year)
        set(Calendar.MONTH, month)
        set(Calendar.DAY_OF_MONTH, day)
        set(Calendar.HOUR_OF_DAY, 9)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    val noonTime = Calendar.getInstance().apply {
        set(Calendar.YEAR, year)
        set(Calendar.MONTH, month)
        set(Calendar.DAY_OF_MONTH, day)
        set(Calendar.HOUR_OF_DAY, 12)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    val eveningTime = Calendar.getInstance().apply {
        set(Calendar.YEAR, year)
        set(Calendar.MONTH, month)
        set(Calendar.DAY_OF_MONTH, day)
        set(Calendar.HOUR_OF_DAY, 18)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    return listOf(
        Pair(morningTime, "m"),
        Pair(noonTime, "n"),
        Pair(eveningTime, "e")
    )
}

fun AlarmManager.scheduleNotificationForNextAttempt(
    application: LearningPartnerApplication,
    nextAttemptUnix: Long,
    noteId: String
) {

   /* val notificationId = (noteId ).hashCode()
    val intent = Intent(application, OnAlarmTriggeredReceiver::class.java).apply {
        this.putExtra("noteId", noteId)
        this.putExtra("period", "it.second")
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
               nextAttemptUnix,
                pendingIntent
            )
        }
    } else {
        this.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            nextAttemptUnix,
            pendingIntent
        )
    }*/

    nextAttemptUnix.getNotificationTimes()?.forEach {
        Log.d(
            "TAG",
            "scheduleNotificationForNextAttempt: noteId $noteId,time ${it.first} , period ${it.second}"
        )

        val notificationId = (noteId + it.second).hashCode()
        val intent = Intent(application, OnAlarmTriggeredReceiver::class.java).apply {
            this.putExtra("noteId", noteId)
            this.putExtra("period", it.second)
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
                    it.first,
                    pendingIntent
                )
            }
        } else {
            this.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                it.first,
                pendingIntent
            )
        }
    }


}