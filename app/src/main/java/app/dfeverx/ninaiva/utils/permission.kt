package app.dfeverx.ninaiva.utils

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings


fun AlarmManager.hasPermission(): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        this.canScheduleExactAlarms()
    } else {
        true
    }

}


fun Context.openNotificationSettings() {
    val intent = Intent()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        intent.setAction(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
        intent.putExtra(Settings.EXTRA_APP_PACKAGE, this.packageName)
    } else
        intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS")
        intent.putExtra("app_package", this.packageName)
        intent.putExtra("app_uid", this.applicationInfo.uid)
    this.startActivity(intent)
}