package app.dfeverx.ninaiva.datastore

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import app.dfeverx.ninaiva.models.CreditAndSubscriptionInfo
import app.dfeverx.ninaiva.models.CreditInfo
import app.dfeverx.ninaiva.models.SubscriptionInfo
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.sql.Time
import java.util.Date

class CreditAndSubscriptionDataStore(
    val context: Context,
    val firestore: FirebaseFirestore,
    val auth: FirebaseAuth
) {
    private val TAG = "CreditDataStore"
    private val Context.dataStore by preferencesDataStore(name = "ninaiva.credit.datastore")

    companion object {
        val SUBSCRIPTION_START = longPreferencesKey("subscription.start")
        val SUBSCRIPTION_END = longPreferencesKey("subscription.end")
        val SUBSCRIPTION_ID = stringPreferencesKey("subscription.id")

        val CREDIT_LAST_UPDATED = longPreferencesKey("lastUpdated")
        val CREDIT_MONTHLY_NOTE_COUNT = intPreferencesKey("monthlyNoteCount")
        val CREDIT_NOTE_COUNT = intPreferencesKey("noteCount")
        val CREDIT_IS_INITIAL_FETCH = booleanPreferencesKey("initialFetch")
    }

    val creditAndSubscriptionInfoFlow: Flow<CreditAndSubscriptionInfo> = context
        .dataStore
        .data
        .map { preferences ->
            CreditAndSubscriptionInfo(
                credit = CreditInfo(
                    noteCount = preferences[CREDIT_NOTE_COUNT] ?: 0,
                    monthlyNoteCount = preferences[CREDIT_MONTHLY_NOTE_COUNT] ?: 0,
                    lastUpdated = Timestamp(Date(preferences[CREDIT_LAST_UPDATED] ?: 0)),
                    isInitialFetch = preferences[CREDIT_IS_INITIAL_FETCH] ?: true
                ),
                subscription = SubscriptionInfo(
                    start = preferences[SUBSCRIPTION_START] ?: 0,
                    end = preferences[SUBSCRIPTION_END] ?: 0,
                    id = preferences[SUBSCRIPTION_ID] ?: "",
                )
            )
        }

    suspend fun sync(forced: Boolean = false) {
//        todo: check already synced
//        todo: forced sync
        Log.d(TAG, "sync: ...")
        if (auth.currentUser?.uid.isNullOrEmpty()) {
            return
        }
        val isAnonymous = auth.currentUser!!.isAnonymous
        try {
            val result =
                firestore
                    .collection("users")
                    .document(auth.currentUser!!.uid)
                    .collection("credit")
                    .document("v2")
                    .get()
                    .await()

            if (result.exists()) {
                val creditInfo = result.toObject(CreditAndSubscriptionInfo::class.java)
                creditInfo!!.toDataStore(context.dataStore)
            } else {
                val creditInfo =
                    CreditAndSubscriptionInfo()
                creditInfo.toDataStore(context.dataStore)
            }
        } catch (e: Exception) {
            Log.d(TAG, "sync: Exception $e")
        }

    }

    suspend fun update(creditAndSubscriptionInfo: CreditAndSubscriptionInfo) {
        Log.d(TAG, "update: $creditAndSubscriptionInfo")
        creditAndSubscriptionInfo.apply {
            credit.lastUpdated =
                Timestamp(Date(System.currentTimeMillis()))
        }.toDataStore(context.dataStore)
    }


}

