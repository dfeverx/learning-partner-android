package app.dfeverx.ninaiva.datastore

import android.content.Context
import android.util.Log
import androidx.compose.ui.res.booleanResource
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import app.dfeverx.ninaiva.datastore.StreakDataStore.Companion.STREAKS_COUNT
import app.dfeverx.ninaiva.datastore.StreakDataStore.Companion.STREAKS_FROM
import app.dfeverx.ninaiva.datastore.StreakDataStore.Companion.STREAKS_IS_INITIAL_FETCH
import app.dfeverx.ninaiva.datastore.StreakDataStore.Companion.STREAKS_RESET_IS_ACKNOWLEDGED
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class StreakDataStore(
    val context: Context, val firestore: FirebaseFirestore, val auth: FirebaseAuth
) {
    private val TAG = "StreakDataStore"
    private val Context.dataStore by preferencesDataStore(name = "ninaiva.streak.datastore")

    companion object {
        val STREAKS_COUNT = intPreferencesKey("count")
        val STREAKS_FROM = longPreferencesKey("from")
        val STREAKS_IS_INITIAL_FETCH = booleanPreferencesKey("initial")
        val STREAKS_RESET_IS_ACKNOWLEDGED = booleanPreferencesKey("ack")
    }

    val streakInfoFlow: Flow<StreakInfo> = context.dataStore.data.map { preferences ->
        StreakInfo(preferences[STREAKS_COUNT] ?: 0, preferences[STREAKS_FROM] ?: 0)
    }

    suspend fun sync() {
//        todo: check already synced
        Log.d(TAG, "sync: ...")
        if (auth.currentUser?.uid == null) {
            return
        }
        try {
            val result =
                firestore.collection("users").document(auth.currentUser!!.uid).collection("streak")
                    .document("v1").get().await()

            if (result.exists()) {
                val streakInfo = result.toObject(StreakInfo::class.java)
                streakInfo!!.toDataStore(context.dataStore)
            }
        } catch (e: Exception) {
            Log.d(TAG, "sync: Exception $e")
        }


    }

    suspend fun incrementStreak() {
        Log.d(TAG, "incrementStreak: ...")
        context.dataStore.edit { preferences ->
            val prevStreaks = preferences[STREAKS_COUNT] ?: 0
            preferences[STREAKS_COUNT] = prevStreaks + 1
            preferences[STREAKS_RESET_IS_ACKNOWLEDGED] = true
            var prevFrom = preferences[STREAKS_FROM]
            Log.d(TAG, "incrementStreak: $prevFrom")
            if (preferences[STREAKS_FROM] == null) {
                prevFrom = System.currentTimeMillis()
                preferences[STREAKS_FROM] = prevFrom
            }
        }
        val streakInfoFlow = context.dataStore.data.map { preferences ->
            StreakInfo(preferences[STREAKS_COUNT] ?: 0, preferences[STREAKS_FROM] ?: 0)
        }.first()

        firestore
            .collection("users")
            .document(auth.currentUser!!.uid)
            .collection("streak")
            .document("v1")
            .set(
                streakInfoFlow

            ).await()


    }

    suspend fun resetStreak() {
        Log.d(TAG, "resetStreak: ...")
        context.dataStore.edit {
            it[STREAKS_COUNT] = 0
            it[STREAKS_FROM] = System.currentTimeMillis()
            it[STREAKS_RESET_IS_ACKNOWLEDGED] = false
        }
        //        todo update to firestore
        firestore
            .collection("users")
            .document(auth.currentUser!!.uid)
            .collection("streak")
            .document("v1")
            .set(
                StreakInfo(0, System.currentTimeMillis())

            ).await()
    }
}

class StreakInfo(
    val count: Int = 0, val from: Long = 0,
    @get:Exclude
    val isAcknowledged: Boolean = true
) {

    suspend fun toDataStore(dataStore: DataStore<Preferences>) {
        dataStore.edit {
            it[STREAKS_COUNT] = count
            it[STREAKS_FROM] = from
            it[STREAKS_IS_INITIAL_FETCH] = false
            it[STREAKS_RESET_IS_ACKNOWLEDGED] = isAcknowledged
        }
    }


}