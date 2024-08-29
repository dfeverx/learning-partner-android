package app.dfeverx.ninaiva.datastore

import android.app.AlarmManager
import android.content.Context
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import app.dfeverx.ninaiva.LearningPartnerApplication
import app.dfeverx.ninaiva.datastore.AppInfoDataStore.Companion.IS_NOTIFICATION_SCHEDULED
import app.dfeverx.ninaiva.datastore.StreakDataStore.Companion.STREAKS_COUNT
import app.dfeverx.ninaiva.datastore.StreakDataStore.Companion.STREAKS_FROM
import app.dfeverx.ninaiva.datastore.StreakDataStore.Companion.STREAKS_IS_INITIAL_FETCH
import app.dfeverx.ninaiva.datastore.StreakDataStore.Companion.STREAKS_RESET_IS_ACKNOWLEDGED
import app.dfeverx.ninaiva.models.CreditAndSubscriptionInfo
import app.dfeverx.ninaiva.models.CreditInfo
import app.dfeverx.ninaiva.models.SubscriptionInfo
import app.dfeverx.ninaiva.models.local.StudyNote
import app.dfeverx.ninaiva.receivers.scheduleAlarm
import app.dfeverx.ninaiva.utils.hasPermission
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.Date

class AppInfoDataStore(val context: Context) {
    private val TAG = "AppInfoDataStore"
    private val Context.dataStore by preferencesDataStore(name = "ninaiva.appinfo.datastore")

    companion object {
        val IS_NOTIFICATION_SCHEDULED = booleanPreferencesKey("appinfo.notificationScheduled")
        val FLASHCARDS_SCROLLED = intPreferencesKey("appinfo.flashCardsScrolled")
        val FLASHCARDS_REVEALED = intPreferencesKey("appinfo.flashCardsScrolledRevealed")
        val IS_FIRST_NOTE_SCANNING_GUIDELINES =
            booleanPreferencesKey("appinfo.noteScanningGuidelines")
    }

    val appOnboardInfoFlow: Flow<AppInfoOnboardInfo> = context.dataStore.data.map { preferences ->
        AppInfoOnboardInfo(preferences[IS_NOTIFICATION_SCHEDULED] ?: false)
    }


    suspend fun updateNotificationScheduled(
        application: LearningPartnerApplication,
        alarmManager: AlarmManager,
        allStudyNotes: List<StudyNote>

    ) {
        Log.d(TAG, "updateNotificationScheduled: ${allStudyNotes.size}")

        if (alarmManager.hasPermission() && NotificationManagerCompat.from(application)
                .areNotificationsEnabled() && allStudyNotes.isNotEmpty()
        ) {
            alarmManager.scheduleAlarm(allStudyNotes, application)
            AppInfoOnboardInfo(isNotificationScheduled = true).toDataStore(context.dataStore)
        }
    }


}

class AppInfoOnboardInfo(val isNotificationScheduled: Boolean = false) {
    suspend fun toDataStore(dataStore: DataStore<Preferences>) {
        dataStore.edit {
            it[IS_NOTIFICATION_SCHEDULED] = isNotificationScheduled
        }
    }
}