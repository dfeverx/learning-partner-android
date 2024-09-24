package app.dfeverx.ninaiva.utils

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast

import app.dfeverx.ninaiva.ui.glance.StreakWidget
import app.dfeverx.ninaiva.ui.glance.StreakWidgetReceiver
import app.dfeverx.ninaiva.ui.main.MainActivity


@SuppressLint("O check already in first if statement")
fun requestPinWidget(context: Context) {
    val appWidgetManager = AppWidgetManager.getInstance(context)
    val componentName = ComponentName(context, StreakWidgetReceiver::class.java)

    // Check if the device supports widget pinning
    if (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            appWidgetManager.isRequestPinAppWidgetSupported
        } else {
            false
        }
    ) {
        // Create a PendingIntent to send when the widget is pinned
        /* val successCallback = PendingIntent.getActivity(
             context, 0,
             Intent(context, MainActivity::class.java),
             PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
         )*/

        // Request the widget to be pinned

        appWidgetManager.requestPinAppWidget(componentName, null, null)
    } else {
        // Handle the case where widget pinning is not supported
        Toast.makeText(
            context,
            "Pinning widgets is not supported on this device",
            Toast.LENGTH_SHORT
        ).show()
    }
}