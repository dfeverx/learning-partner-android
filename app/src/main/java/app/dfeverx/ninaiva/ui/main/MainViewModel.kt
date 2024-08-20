package app.dfeverx.ninaiva.ui.main

import android.app.AlarmManager
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import androidx.annotation.Keep
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.dfeverx.ninaiva.LearningPartnerApplication
import app.dfeverx.ninaiva.datastore.CreditAndSubscriptionDataStore
import app.dfeverx.ninaiva.datastore.StreakDataStore
import app.dfeverx.ninaiva.di.IS_INITIAL_NOTES_FETCH
import app.dfeverx.ninaiva.models.CreditAndSubscriptionInfo
import app.dfeverx.ninaiva.models.remote.StudyNoteWithQuestionsFirestore
import app.dfeverx.ninaiva.receivers.scheduleAlarm
import app.dfeverx.ninaiva.repos.LevelRepository
import app.dfeverx.ninaiva.repos.StudyNoteRepository
import app.dfeverx.ninaiva.utils.TimePeriod
import app.dfeverx.ninaiva.utils.filterNotIn
import app.dfeverx.ninaiva.utils.getTimePeriod
import app.dfeverx.ninaiva.utils.hasPermission
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.ProductDetails.SubscriptionOfferDetails
import com.android.billingclient.api.Purchase
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.install.InstallState
import com.google.android.play.core.install.model.InstallStatus
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.get
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val application: LearningPartnerApplication,
    private val studyNoteRepository: StudyNoteRepository,
    private val levelRepository: LevelRepository,
    private val sharedPreferences: SharedPreferences,
    private val streakDataStore: StreakDataStore,
    private val creditSubscriptionDataStore: CreditAndSubscriptionDataStore,
    private val firestore: FirebaseFirestore,
    private val alarmManager: AlarmManager,
    private val remoteConfig: FirebaseRemoteConfig,
    private val gson: Gson,
    val auth: FirebaseAuth,
) : ViewModel() {
    companion object {
        private val _hasInternetConnection: MutableStateFlow<Boolean> = MutableStateFlow(true)
        private val networkCallback = object : ConnectivityManager.NetworkCallback() {
            // network is available for use
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                _hasInternetConnection.value = true
            }

            // Network capabilities have changed for the network
            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                super.onCapabilitiesChanged(network, networkCapabilities)
                val unmetered =
                    networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)
            }

            // lost network connection
            override fun onLost(network: Network) {
                super.onLost(network)
                _hasInternetConnection.value = false
            }
        }
    }


    val streakInfoFlow = streakDataStore.streakInfoFlow

    private val _studyNoteCount = MutableStateFlow(0)
    private val _creditAndSubscriptionInfo = MutableStateFlow(CreditAndSubscriptionInfo())


    init {
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
            .build()
        val connectivityManager =
            application.getSystemService(ConnectivityManager::class.java) as ConnectivityManager
        connectivityManager.requestNetwork(networkRequest, networkCallback)


        isInitialNotesPopulated()
        retryTheStudyNoteCurrentlyLoading()
        viewModelScope.launch {
            streakValidation()
        }
        viewModelScope.launch(Dispatchers.IO) {
            studyNoteRepository.allStudyNoteCount().collect {
                _studyNoteCount.value = it
            }
        }
        viewModelScope.launch(Dispatchers.IO) {
            creditSubscriptionDataStore.creditAndSubscriptionInfoFlow.collect {
                Log.d(TAG, "creditInfoFlow: ${gson.toJson(it)} ")

                _creditAndSubscriptionInfo.value = it
            }
        }
        /*alarmManager.scheduleNotificationForNextAttempt(
            application,
            (System.currentTimeMillis() + (1000 * 10)),
            "test"
        )*/
    }

    private suspend fun streakValidation() {
        Log.d(TAG, "streakValidation: ...")

        val allStudyNotes = studyNoteRepository.allStudyNoted()
        Log.d(TAG, "streakValidation: ${allStudyNotes.size}")
        val delayedNotes = allStudyNotes.map {
            Log.d(TAG, "streakValidation: ${getTimePeriod(it.nextLevelIn)}")
            Pair(getTimePeriod(it.nextLevelIn), it.id)
        }
            .filter { it.first == TimePeriod.PAST }
        Log.d(TAG, "streakValidation: $delayedNotes")
        Log.d(TAG, "streakValidation: ${delayedNotes.size}")
        if (delayedNotes.isNotEmpty()) {
            delayedNotes.map {
                studyNoteRepository.resetNoteProgress(it.second)
            }
            resetStreaks(delayedNotes)
        }
    }

    private suspend fun resetStreaks(delayedNotes: List<Pair<TimePeriod, String>>) {
//        firestore
//        sharedpref
        delayedNotes.map {
            levelRepository.updateStudyNoteInFirestore(it.second)
        }
        streakDataStore.resetStreak()
    }

    private fun isInitialNotesPopulated() {
        Log.d(TAG, "isInitialNotesPopulated: ...")
        val isFirstFetch = sharedPreferences.getBoolean(IS_INITIAL_NOTES_FETCH, true)
        Log.d(TAG, "isInitialNotesPopulated: $isFirstFetch")
        if (isFirstFetch && Firebase.auth.currentUser != null) {
            dataSyncOnGoogleAuthSuccessAndMarkingInitialFetch(FirebaseAuth.getInstance().uid!!)
        } else {
            Log.d(TAG, "isInitialNotesPopulated: already populated")
        }
    }


    private fun retryTheStudyNoteCurrentlyLoading() = viewModelScope.launch(Dispatchers.IO) {
        Log.d(TAG, "retryTheStudyNoteCurrentlyLoading: ")
//        fetch the note currently state is loading with ids
        val studyNotesWithLoading = studyNoteRepository.notesWithProssesing()

        if (studyNotesWithLoading.isEmpty()) {
            Log.d(TAG, "retryTheStudyNoteCurrentlyLoading: there is no processing not found")
            return@launch
        }
//        fetch that note one time fetch
        val uid = Firebase.auth.currentUser?.uid ?: return@launch

        val documentIds = studyNotesWithLoading.map { it.id }
        val query = firestore.collection("users")
            .document(uid)
            .collection("notes").whereIn("id", documentIds)

        query.get().addOnSuccessListener { result ->
            Log.d(TAG, "retryTheStudyNoteCurrentlyLoading: ${result.documents}")
            val fetchedNotes = firestoreStudyNotesResultToLocalDb(result)
            viewModelScope.launch(Dispatchers.IO) {
                studyNoteRepository.makeLoadingToRetry(documentIds.filterNotIn(fetchedNotes))
            }

        }.addOnFailureListener { exception ->
            // Handle query failure
            Log.d(TAG, "retryTheStudyNoteCurrentlyLoading: exception $exception")
            viewModelScope.launch(Dispatchers.IO) {
                studyNoteRepository.makeLoadingToRetry(documentIds)
            }
        }

    }

    fun dataSyncOnGoogleAuthSuccessAndMarkingInitialFetch(uid: String) {
        Log.d(TAG, "dataSync: ...")
//        sync streaks info
        viewModelScope.launch(Dispatchers.IO) {
            streakDataStore.sync()
            creditSubscriptionDataStore.sync()
        }
        firestore
            .collection("users")
            .document(uid)
            .collection("notes")
            .get()
            .addOnSuccessListener { result ->
                firestoreStudyNotesResultToLocalDb(result)
                sharedPreferences
                    .edit()
                    .putBoolean(IS_INITIAL_NOTES_FETCH, false)
                    .apply()
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting notes: ", exception)
            }
    }

    //also in home
    private fun firestoreStudyNotesResultToLocalDb(result: QuerySnapshot): List<String> {
        val studyNoteList = mutableListOf<StudyNoteWithQuestionsFirestore>()
        for (document in result) {
            if (document.data.containsKey("status")) {
                val note =
                    document.toObject(StudyNoteWithQuestionsFirestore::class.java)
                        .apply {
                            id = document.id

                        }
                studyNoteList.add(note)
                viewModelScope.launch {
                    val _r = studyNoteRepository.addStudyNoteAndQuestionsFromFirestore(note)
                    val allStudyNotes = studyNoteRepository.allStudyNoted()
                    alarmManager.scheduleAlarm(allStudyNotes, application)
                    streakValidation()
                }
            }

        }
        // debug log
        for (note in studyNoteList) {
            Log.d(TAG, "Id: ${note.id}, title: ${note.title}")
        }
        return studyNoteList.map { it.id }
    }

    private val TAG = "ActivityViewModel"

    val hasInternetConnection: StateFlow<Boolean>
        get() = _hasInternetConnection

    private val _inAppUpdateState: MutableStateFlow<InAppUpdateUiState> =
        MutableStateFlow(InAppUpdateUiState())
    val inAppUpdateState: StateFlow<InAppUpdateUiState>
        get() = _inAppUpdateState

    fun inAppUpdateListener(state: InstallState) {
        Log.d(TAG, "inAppUpdateListener: $state")
        when (state.installStatus()) {
            InstallStatus.DOWNLOADING -> {
                val bytesDownloaded = state.bytesDownloaded()
                val totalBytesToDownload = state.totalBytesToDownload()
                val progress = bytesDownloaded.toFloat() / totalBytesToDownload
                _inAppUpdateState.update { currentState ->
                    currentState.progress = progress
                    currentState.isReadyToInstall = false
                    currentState.isDownloading = true
                    return
                }
            }

            InstallStatus.DOWNLOADED -> {
                _inAppUpdateState.update { currentState ->
                    currentState.progress = 0f
                    currentState.isReadyToInstall = true
                    currentState.isDownloading = false
                    return
                }
            }

            else -> {}
        }
    }

    fun hasAPendingUpdate(appUpdateInfo: AppUpdateInfo) {
        Log.d(TAG, "inAppUpdateSuccessListener: $appUpdateInfo")
        if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
            _inAppUpdateState.update { currentState ->
                currentState.progress = 1f
                currentState.isReadyToInstall = true
                return
            }
        }
    }

    fun hasInAppUpdate(appUpdateInfo: AppUpdateInfo) {
        Log.d(TAG, "hasInAppUpdate: $appUpdateInfo")
        _inAppUpdateState.update { currentState ->
            currentState.appUpdateInfo = appUpdateInfo
            currentState.progress = 0f
            currentState.isReadyToInstall = false
            currentState.isDownloading = false
            return
        }
    }

    fun hasAlarmPermission(): Boolean {
        Log.d(TAG, "hasAlarmPermission: ${alarmManager.hasPermission()}")
        return alarmManager.hasPermission()
    }

    fun planDetails(planId: String): PremiumPlanFeatures {
        Log.d(TAG, "remoteConfigInit: ")
        val value = when (planId) {
            "quarterly" -> {
                val quarterly = remoteConfig[QUARTERLY_PLAN_FEATURES_AND_DETAILS].asString()
                gson.fromJson(quarterly, PremiumPlanFeatures::class.java)
            }

            "monthly" -> {
                val monthlyString = remoteConfig[MONTHLY_PLAN_FEATURES_AND_DETAILS].asString()
                gson.fromJson(monthlyString, PremiumPlanFeatures::class.java)
            }

            "yearly" -> {
                val yearlyString = remoteConfig[YEARLY_PLAN_FEATURES_AND_DETAILS].asString()
                gson.fromJson(yearlyString, PremiumPlanFeatures::class.java)
            }

            else -> {
                val quarterly = remoteConfig[QUARTERLY_PLAN_FEATURES_AND_DETAILS].asString()
                gson.fromJson(quarterly, PremiumPlanFeatures::class.java)
            }
        }
        Log.d(TAG, "planDetails: remoteConfig ${value.label} ${value.features?.first()}")
        return value


    }

    //Subscription start
    private val _subscriptionPlanDetails = MutableStateFlow<ProductDetails?>(null)
    val subscriptionPlanDetails: MutableStateFlow<ProductDetails?>
        get() = _subscriptionPlanDetails

    private val _selectedSubscriptionPlan = MutableStateFlow<SubscriptionOfferDetails?>(null)
    val selectedSubscriptionPlan: MutableStateFlow<SubscriptionOfferDetails?>
        get() = _selectedSubscriptionPlan

    fun updateProductDetails(subscriptionOfferDetails: ProductDetails) {
        _subscriptionPlanDetails.value = subscriptionOfferDetails
        if (_selectedSubscriptionPlan.value == null) {
            subscriptionOfferDetails.subscriptionOfferDetails?.first()?.let {
                _selectedSubscriptionPlan.value = it
            }
        }

    }

    fun updateSelectedSubscription(it: SubscriptionOfferDetails) {
        Log.d(TAG, "updateSelectedSubscription: $it")
        Log.d(TAG, "updateSelectedSubscription: ${it.basePlanId},${it.offerTags}")
        _selectedSubscriptionPlan.value = it
    }


    private fun checkCredit() {
        Log.d(TAG, "checkCredit: check")
        viewModelScope.launch(Dispatchers.IO) {
            /*  Log.d(TAG, "checkCredit: init")
              if (FirebaseAuth.getInstance().currentUser == null) {
                  Log.d(TAG, "checkCredit: returned because there is no current user")
                  return@launch
              }
              val uid = FirebaseAuth.getInstance().currentUser!!.uid
              Log.d(TAG, "checkCredit: $uid")
              val newCreditAndEndIn = _creditAndEndIn.value.checkCredit(
                  uid,
                  sharedPreferences
              )
              Log.d(TAG, "checkCredit: $newCreditAndEndIn")
              if (newCreditAndEndIn != null) {
                  _creditAndEndIn.value = newCreditAndEndIn
                  Log.d(TAG, "checkCredit: new credit $newCreditAndEndIn")
              }*/
        }
    }

    private val _isPro = MutableStateFlow(false)
    val isPro: StateFlow<Boolean> = _isPro

    fun updatePro(isPro: Boolean) {
        _isPro.value = isPro
        viewModelScope.launch(Dispatchers.IO) {
            delay(2000)
            if (isPro) {
                Log.d(TAG, "updatePro: ")
                checkCredit()
                creditSubscriptionDataStore.sync()
            }
        }

    }

    private val _purchaseAcknowledgement = MutableStateFlow<Purchase?>(null)
    val purchaseAcknowledgement: StateFlow<Purchase?> = _purchaseAcknowledgement
    fun updatePurchaseAcknowledgement(purchase: Purchase?) {
        _purchaseAcknowledgement.value = purchase
    }


    //Subscription end
//    limiting fun start


    val NOTE_CREATTION_LIMT_PREMIUM = 50
    val NOTE_CREATTION_LIMT_NON_PREMIUM_AUTH_MONTHLY = 5
    val NOTE_CREATTION_LIMT_ANONYMOUS = 3


    fun isAdEnabled(): Boolean {
        return if (_isPro.value) {
            return false
        } else {
            _creditAndSubscriptionInfo.value.credit.noteCount > 2
        }
    }

    fun directScanEligible(): Boolean {
//        return true
        Log.d(TAG, "directScanEligible: ...")
        Log.d(
            TAG,
            "directScanEligible: ${_creditAndSubscriptionInfo.value.credit.monthlyNoteCount}"
        )
        Log.d(TAG, "directScanEligible: ${_creditAndSubscriptionInfo.value.toString()}")

        return if (auth.currentUser?.isAnonymous == true) {
//only first two scan
            Log.d(TAG, "directScanEligible: ${_studyNoteCount.value}")
            (_studyNoteCount.value < 2)
        } else if (isPro.value) {

//            premium user
//up to 45
// message only five note left for this month
            Log.d(
                TAG,
                "directScanEligible: ${_creditAndSubscriptionInfo.value.credit.monthlyNoteCount <= (NOTE_CREATTION_LIMT_PREMIUM - 3)} "
            )
            _creditAndSubscriptionInfo.value.credit.monthlyNoteCount <= (NOTE_CREATTION_LIMT_PREMIUM - 3)


        } else {
//            non premium auth user
            _creditAndSubscriptionInfo.value.credit.monthlyNoteCount <= (NOTE_CREATTION_LIMT_NON_PREMIUM_AUTH_MONTHLY - 2)
        }

    }

    fun isEligibleForNoteCreation(): Boolean {
//        return true
        return if (auth.currentUser?.isAnonymous == true) {
//            up to 3 note lifetime
            _studyNoteCount.value <= NOTE_CREATTION_LIMT_ANONYMOUS
        } else if (_isPro.value) {
//            premium user
//            up to 50 note per month
            _creditAndSubscriptionInfo.value.credit.monthlyNoteCount <= NOTE_CREATTION_LIMT_PREMIUM || (!_creditAndSubscriptionInfo.value.credit.lastUpdated.toDate().time.isSameMonth())
        } else {
//            auth user non premium
//            up to 5 notes per month
            _creditAndSubscriptionInfo.value.credit.monthlyNoteCount <= NOTE_CREATTION_LIMT_NON_PREMIUM_AUTH_MONTHLY || (!_creditAndSubscriptionInfo.value.credit.lastUpdated.toDate().time.isSameMonth())
        }
    }
//    limiting fun end


    val quotaExceeds = _creditAndSubscriptionInfo.map {
        when {
            auth.currentUser?.isAnonymous == true -> {
                _studyNoteCount.value <= NOTE_CREATTION_LIMT_ANONYMOUS
            }

            _isPro.value -> {
                _creditAndSubscriptionInfo.value.credit.monthlyNoteCount <= NOTE_CREATTION_LIMT_PREMIUM || (!_creditAndSubscriptionInfo.value.credit.lastUpdated.toDate().time.isSameMonth())
            }

            else -> {
                _creditAndSubscriptionInfo.value.credit.monthlyNoteCount <= NOTE_CREATTION_LIMT_NON_PREMIUM_AUTH_MONTHLY || (!_creditAndSubscriptionInfo.value.credit.lastUpdated.toDate().time.isSameMonth())

            }
        }
    }


}

fun Long.isSameMonth(): Boolean {
    val currentCalendar = Calendar.getInstance()
    currentCalendar.timeInMillis = System.currentTimeMillis()

    val givenCalendar = Calendar.getInstance()
    givenCalendar.timeInMillis = this

    // Compare year and month
    val sameYear = currentCalendar.get(Calendar.YEAR) == givenCalendar.get(Calendar.YEAR)
    val sameMonth = currentCalendar.get(Calendar.MONTH) == givenCalendar.get(Calendar.MONTH)

    return sameYear && sameMonth
}

@Keep
const val QUARTERLY_PLAN_FEATURES_AND_DETAILS = "QUARTERLY"

@Keep
const val MONTHLY_PLAN_FEATURES_AND_DETAILS = "MONTHLY"

@Keep
const val YEARLY_PLAN_FEATURES_AND_DETAILS = "YEARLY"

@Keep
data class PremiumPlanFeatures(
    @Keep
    var label: String = "",
    @Keep
    var features: List<String> = listOf()
)

