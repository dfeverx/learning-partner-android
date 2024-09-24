package app.dfeverx.ninaiva.models

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import app.dfeverx.ninaiva.datastore.CreditAndSubscriptionDataStore.Companion.CREDIT_IS_INITIAL_FETCH
import app.dfeverx.ninaiva.datastore.CreditAndSubscriptionDataStore.Companion.CREDIT_LAST_UPDATED
import app.dfeverx.ninaiva.datastore.CreditAndSubscriptionDataStore.Companion.CREDIT_MONTHLY_NOTE_COUNT
import app.dfeverx.ninaiva.datastore.CreditAndSubscriptionDataStore.Companion.CREDIT_NOTE_COUNT
import app.dfeverx.ninaiva.datastore.CreditAndSubscriptionDataStore.Companion.SUBSCRIPTION_END
import app.dfeverx.ninaiva.datastore.CreditAndSubscriptionDataStore.Companion.SUBSCRIPTION_ID
import app.dfeverx.ninaiva.datastore.CreditAndSubscriptionDataStore.Companion.SUBSCRIPTION_START
import com.google.firebase.Timestamp
import com.google.firebase.firestore.Exclude
import java.util.Date

class CreditInfo(

    var lastUpdated: Timestamp = Timestamp(0, 0),
    var monthlyNoteCount: Int = 0,
    var noteCount: Int = 0,

    @get:Exclude
    val isInitialFetch: Boolean = false,
) {
    suspend fun toDataStore(dataStore: DataStore<Preferences>) {
        dataStore.edit {
            it[CREDIT_LAST_UPDATED] = lastUpdated.toDate().time
            it[CREDIT_MONTHLY_NOTE_COUNT] = monthlyNoteCount
            it[CREDIT_NOTE_COUNT] = noteCount
            it[CREDIT_IS_INITIAL_FETCH] = isInitialFetch
        }
    }
}

class SubscriptionInfo(
    var start: Long = 0,
    var end: Long = 0,
    var id: String = "",
) {
    suspend fun toDataStore(dataStore: DataStore<Preferences>) {
        dataStore.edit {
            it[SUBSCRIPTION_START] = start
            it[SUBSCRIPTION_END] = end
            it[SUBSCRIPTION_ID] = id

        }
    }
}

class CreditAndSubscriptionInfo(
    var credit: CreditInfo = CreditInfo(),
    var subscription: SubscriptionInfo = SubscriptionInfo()
) {
    suspend fun toDataStore(dataStore: DataStore<Preferences>) {
        Log.d("TAG", "toDataStore: ${this.credit.toString()} ")
        credit.toDataStore(dataStore)
        subscription.toDataStore(dataStore)
    }
}
