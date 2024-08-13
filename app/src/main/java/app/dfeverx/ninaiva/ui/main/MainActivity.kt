package app.dfeverx.ninaiva.ui.main

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import app.dfeverx.ninaiva.ui.Screens
import app.dfeverx.ninaiva.ui.appNavHost
import app.dfeverx.ninaiva.ui.theme.StudyPartnerTheme
import com.android.billingclient.api.AcknowledgePurchaseParams.*
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.InAppMessageParams
import com.android.billingclient.api.InAppMessageResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.ProductDetails.SubscriptionOfferDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.common.collect.ImmutableList
import com.google.firebase.Firebase
import com.google.firebase.appcheck.appCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.initialize
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext


@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()
    private lateinit var auth: FirebaseAuth


    private val inAppUpdateListener: InstallStateUpdatedListener = InstallStateUpdatedListener {
        viewModel.inAppUpdateListener(it)
    }


    companion object {
        private val TAG = "MainActivity"
        var appUpdateManager: AppUpdateManager? = null
        suspend fun Activity.startUpdate(appUpdateInfo: AppUpdateInfo): Int? {
            return appUpdateManager?.startUpdateFlow(
                appUpdateInfo, this, AppUpdateOptions.newBuilder(AppUpdateType.FLEXIBLE)
//                        .setAllowAssetPackDeletion(true)
                    .build()
            )?.await()
        }

        suspend fun installUpdate(): Void? {
            return appUpdateManager?.completeUpdate()?.await()
        }

        //        subscription start
        lateinit var billingClient: BillingClient

        fun launchBilling(
            activity: Activity,
            subscriptions: ProductDetails,
            selectedSubscription: SubscriptionOfferDetails,
            mainViewModel: MainViewModel
        ) {

            val productDetailsParamsList = listOf(
                BillingFlowParams.ProductDetailsParams.newBuilder()
                    // retrieve a value for "productDetails" by calling queryProductDetailsAsync()
                    .setProductDetails(subscriptions)


                    // to get an offer token, call ProductDetails.subscriptionOfferDetails()
                    // for a list of offers that are available to the user
                    .setOfferToken(selectedSubscription.offerToken).build()

            )
            if (mainViewModel.auth.currentUser == null) {
                Toast.makeText(
                    activity, "User not verified,Relaunch the app ", Toast.LENGTH_LONG
                ).show()
                return
            }
            val billingFlowParams = BillingFlowParams.newBuilder()
                .setObfuscatedProfileId(mainViewModel.auth.currentUser!!.uid)
                .setObfuscatedAccountId(mainViewModel.auth.currentUser!!.uid)
                .setProductDetailsParamsList(productDetailsParamsList).build()



            billingClient.launchBillingFlow(activity, billingFlowParams)
        }

        //        subscription end
        var REWARDED_INTERSTITIAL_AD: RewardedInterstitialAd? = null
        fun Activity.loadAd() {
            // Use the test ad unit ID to load an ad.
            RewardedInterstitialAd.load(this,
                "ca-app-pub-1705683037181913/3340903926",
                AdRequest.Builder().build(),
                object : RewardedInterstitialAdLoadCallback() {
                    override fun onAdLoaded(ad: RewardedInterstitialAd) {
                        Log.d(TAG, "Ad was loaded.")
                        REWARDED_INTERSTITIAL_AD = ad
                    }

                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        Log.d(TAG, loadAdError.toString())
                        REWARDED_INTERSTITIAL_AD = null
                    }
                })
        }

        fun Activity.showAd() {

            REWARDED_INTERSTITIAL_AD?.show(this) {
                this.loadAd()
            }
        }

        fun Purchase.acknowledged() {
            val params = newBuilder().setPurchaseToken(this.purchaseToken).build()
            billingClient.acknowledgePurchase(
                params
            ) {}

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val splashScreen = installSplashScreen()


        enableEdgeToEdge()
        auth = Firebase.auth
        setContent {
            splashScreen.setKeepOnScreenCondition { false }
            val navHostController = rememberNavController()

            StudyPartnerTheme {
                Surface(modifier = Modifier.background(MaterialTheme.colorScheme.surface)) {
                    appNavHost(
                        navController = navHostController,
                        startDestination = (if (auth.currentUser == null) Screens.Onboarding.route else Screens.Home.route)
                    )
                }
            }

        }
//        viewModel.dataSync()
        Firebase.initialize(context = this)
        Firebase.appCheck.installAppCheckProviderFactory(
            DebugAppCheckProviderFactory.getInstance(),
        )
        //        In-app update
        checkInAppUpdate()
//        Ad init
        val backgroundScope = CoroutineScope(Dispatchers.IO)
        backgroundScope.launch {
            // Initialize the Google Mobile Ads SDK on a background thread.
            MobileAds.initialize(this@MainActivity) {}
            withContext(Dispatchers.Main) {
                loadAd()
            }
        }

        val purchasesUpdatedListener = PurchasesUpdatedListener { billingResult, purchases ->
            // To be implemented in a later section.
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && !purchases.isNullOrEmpty()) {
                // Post new purchase List to _purchases
//                    _purchases.value = purchases

                viewModel.updatePro(true)
                // Then, handle the purchases
                for (purchase in purchases) {
                    purchase.orderId
                    purchase.purchaseState
                    Log.d(TAG, "onCreate: purchase update listener ${purchase.products}")
                    Log.d(TAG, "onCreate: purchase update listener ${purchase.originalJson}")
                    isThereAnyPurchaseToAcknowledged(purchase)
                }

            } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
                // Handle an error caused by a user cancelling the purchase flow.
                Log.e(TAG, "User has cancelled")
                Toast.makeText(this, "user cancelled", Toast.LENGTH_LONG).show()
            } else {
                Log.d(TAG, "purchasesUpdatedListener: not ok ")
                // Handle any other error codes.
            }
        }
        billingClient = BillingClient.newBuilder(this).setListener(purchasesUpdatedListener)
            .enablePendingPurchases()

            .build()

        startBillingConnection(viewModel)
    }

    private fun isThereAnyPurchaseToAcknowledged(purchase: Purchase) {
        if (!purchase.isAcknowledged) {
//there are purchase that not yet acknowledged
            viewModel.updatePurchaseAcknowledgement(purchase)
        } else {
            viewModel.updatePurchaseAcknowledgement(null)
        }
    }


    private fun checkInAppUpdate() {

        appUpdateManager = AppUpdateManagerFactory.create(this)
        // Returns an intent object that you use to check for an update.
        val appUpdateInfoTask = appUpdateManager?.appUpdateInfo

        appUpdateInfoTask?.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE && appUpdateInfo.isUpdateTypeAllowed(
                    AppUpdateType.FLEXIBLE
                )
            ) {
                appUpdateManager?.registerListener(inAppUpdateListener)
                viewModel.hasInAppUpdate(appUpdateInfo)
            } else {
                appUpdateManager?.unregisterListener(inAppUpdateListener)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        appUpdateManager?.appUpdateInfo?.addOnSuccessListener {
            viewModel.hasAPendingUpdate(it)
        }
    }

    //subscription start
    fun startBillingConnection(mainViewModel: MainViewModel) {

        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    // The BillingClient is ready. You can query purchases here.
                    Log.d(TAG, "Billing response OK")
                    // The BillingClient is ready. You can query purchases and product details here
                    queryProductDetails(mainViewModel)
//                        queryPurchases()
                    queryPurchases(mainViewModel)
//                    sub in-app messaging
                    showInAppMessages()


                }
            }

            override fun onBillingServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
                startBillingConnection(mainViewModel)
            }
        })
    }

    private fun showInAppMessages() {
        val inAppMessageParams = InAppMessageParams.newBuilder()
            .addInAppMessageCategoryToShow(InAppMessageParams.InAppMessageCategoryId.TRANSACTIONAL)
            .build()
        billingClient.showInAppMessages(
            this@MainActivity,
            inAppMessageParams
        ) { inAppMessageResult ->
            if (inAppMessageResult.responseCode == InAppMessageResult.InAppMessageResponseCode.NO_ACTION_NEEDED) {
                // The flow has finished and there is no action needed from developers.
            } else if (inAppMessageResult.responseCode
                == InAppMessageResult.InAppMessageResponseCode.SUBSCRIPTION_STATUS_UPDATED
            ) {
                // The subscription status changed. For example, a subscription
                // has been recovered from a suspend state. Developers should
                // expect the purchase token to be returned with this response
                // code and use the purchase token with the Google Play
                // Developer API.
            }
        }
    }

    private fun queryPurchases(mainViewModel: MainViewModel) {
        billingClient.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.SUBS).build()
        ) { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                if (purchases.size != 0) {
                    purchases.forEach {
                        when (it.purchaseState) {
                            //                                  todo      show appropriate msg
                            Purchase.PurchaseState.UNSPECIFIED_STATE -> {
                                mainViewModel.updatePro(false)
                            }

                            Purchase.PurchaseState.PENDING -> {
                                mainViewModel.updatePro(false)
                            }

                            Purchase.PurchaseState.PURCHASED -> {
                                mainViewModel.updatePro(true)
                            }
                        }
                    }
                }
            } else {

            }

            Log.d(TAG, "onBillingSetupFinished,Active purchases:$purchases ")
            Log.d(TAG, "onBillingSetupFinished,Active purchases:$billingResult ")


        }
        /* billingClient.queryPurchasesAsync(
             QueryPurchasesParams.newBuilder()
                 .setProductType(BillingClient.ProductType.SUBS).build()
         ) { billingResult, purchaseList ->
             Log.d("TAG", "queryPurchases: $purchaseList")
             if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                 purchaseList.forEach {
                     when (it.purchaseState) {
                         //                                  todo      show appropriate msg
                         Purchase.PurchaseState.UNSPECIFIED_STATE -> {
                             mainViewModel.updatePro(false)
                         }

                         Purchase.PurchaseState.PENDING -> {
                             mainViewModel.updatePro(false)
                         }

                         Purchase.PurchaseState.PURCHASED -> {
                             mainViewModel.updatePro(true)
                         }
                     }
                 }
             } else {

             }
         }*/
    }

    private fun queryProductDetails(mainViewModel: MainViewModel) {

        val queryProductDetailsParams = QueryProductDetailsParams.newBuilder().setProductList(
            ImmutableList.of(
                QueryProductDetailsParams.Product.newBuilder().setProductId(
                    "ninaiva_test_subscription"
                ).setProductType(BillingClient.ProductType.SUBS).build()
            )
        ).build()

        billingClient.queryProductDetailsAsync(queryProductDetailsParams) { billingResult, productDetailsList ->
            Log.d(TAG, "queryProductDetails: == $productDetailsList")
            val responseCode = billingResult.responseCode
            val debugMessage = billingResult.debugMessage
            when (responseCode) {
                BillingClient.BillingResponseCode.OK -> {
                    Log.d(TAG, "onProductDetailsResponse: $productDetailsList")
                    Log.d(TAG, "onProductDetailsResponse: ${productDetailsList.first()}")
                    mainViewModel.updateProductDetails(
                        productDetailsList.first()
                    )
                }

                else -> {
                    startBillingConnection(mainViewModel = mainViewModel)
                    Log.i(TAG, "onProductDetailsResponse: $responseCode $debugMessage")
                }
            }
        }


    }
//subscription end

}


