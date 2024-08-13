package app.dfeverx.ninaiva.ui.screens.upgrade

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import app.dfeverx.ninaiva.R
import app.dfeverx.ninaiva.ui.components.ModernGrid
import app.dfeverx.ninaiva.ui.components.PrivacyPolicyLinks
import app.dfeverx.ninaiva.ui.main.MainActivity
import app.dfeverx.ninaiva.ui.main.MainViewModel
import app.dfeverx.ninaiva.utils.activityViewModel
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.android.billingclient.api.ProductDetails


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Upgrade(navController: NavController) {

    val activity = LocalContext.current as Activity
    val upgradeViewModel = hiltViewModel<UpgradeViewModel>()
    val mainViewModel = activityViewModel<MainViewModel>()
    val lazyGridState = rememberLazyListState()
    val scrollBehavior =
        TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    Scaffold(modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection), topBar = {
        TopAppBar(
            navigationIcon = {
                Icon(
                    Icons.Outlined.Close,
                    "Go back to previous",
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .clip(MaterialTheme.shapes.extraLarge)
                        .clickable {
                            navController.navigateUp()
                        }
                        .padding(8.dp),
                )
            },
            title = {

            },
            scrollBehavior = scrollBehavior
        )

    }, content = { paddingValues ->
        LazyColumn(
            state = lazyGridState,
            modifier = Modifier
                .padding(paddingValues)

        ) {

            item {
                UpgradeHeader()
            }
            item {
                PriceList(
                    productDetails = mainViewModel.subscriptionPlanDetails.collectAsState(),
                    selectedSubscription = mainViewModel.selectedSubscriptionPlan.collectAsState(),
                    mainViewModel = mainViewModel
                ) {
                    mainViewModel.updateSelectedSubscription(it)
                }
            }
            item {
                FeaturesList(
                    mainViewModel.selectedSubscriptionPlan.collectAsState(),
                    mainViewModel
                )
            }
            item {
                PrivacyPolicyLinks(activity)
            }
        }
    },
        bottomBar = {
            Button(modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(), onClick = {
                mainViewModel.selectedSubscriptionPlan.value?.let {
                    mainViewModel.subscriptionPlanDetails.value?.let { selectedSubscription ->
                        MainActivity.launchBilling(
                            activity = activity,
                            subscriptions = selectedSubscription,
                            selectedSubscription = it, mainViewModel = mainViewModel
                        )
                    }
                }
            }) {
                Text(text = "Continue")
            }
        })

}


@Composable
fun FeaturesList(
    collectAsState: State<ProductDetails.SubscriptionOfferDetails?>,
    mainViewModel: MainViewModel
) {


    val (firstHalf, secondHalf) = splitList(
        mainViewModel.planDetails(
            collectAsState.value?.basePlanId ?: "weekly"
        ).features
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier.weight(1f)

        ) {


            firstHalf.forEach {
                FeaturesItem(it)
            }
        }
        Column(
            modifier = Modifier.weight(1f)

        ) {


            secondHalf.forEach {
                FeaturesItem(it)
            }
        }
    }

}

@Composable
private fun FeaturesItem(it: String) {
    Row(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            text = it,
            modifier = Modifier.padding(start = 8.dp),
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
fun UpgradeHeader() {
    Column(modifier = Modifier) {
        val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.rocket))
        val logoAnimationState = animateLottieCompositionAsState(
            composition = composition, iterations = 100, restartOnPlay = false
        )

        Text(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(top = 8.dp),
            text = "Unlock full potential",
            style = MaterialTheme.typography.displayMedium
        )
        Text(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(vertical = 8.dp),
            text = "Unlock full potential with ninaiva advanced AI model",
            style = MaterialTheme.typography.titleLarge
//            style = MaterialTheme.typography.titleSmall
        )
        ModernGrid(
            modifier = Modifier.padding(vertical = 8.dp),
            content = {
                LottieAnimation(modifier = Modifier
                    .fillMaxSize(),
                    composition = composition,
                    progress = { logoAnimationState.progress })
            }) {}


    }
}


@Composable
fun PriceList(
    productDetails: State<ProductDetails?>,
    selectedSubscription: State<ProductDetails.SubscriptionOfferDetails?>,
    mainViewModel: MainViewModel,
    updateSelectedSubscription: (ProductDetails.SubscriptionOfferDetails) -> Unit,
) {


    Row(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .padding(vertical = 16.dp)
    ) {
        productDetails.value?.subscriptionOfferDetails?.forEachIndexed { index, subscriptionOfferDetails ->
            Column(modifier = Modifier
                .padding(horizontal = 4.dp)
                .clip(MaterialTheme.shapes.medium)
                .weight(1f)
                .clickable {

                    subscriptionOfferDetails
                        ?.let { updateSelectedSubscription(it) }

                }
                .border(
                    width = if (subscriptionOfferDetails?.basePlanId == selectedSubscription.value?.basePlanId) 4.dp else 1.dp,
                    color = if (subscriptionOfferDetails?.basePlanId == selectedSubscription.value?.basePlanId) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    shape = MaterialTheme.shapes.medium
                ),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally) {
                val visibilityLabelModifier =
                    Modifier.alpha(if (subscriptionOfferDetails?.basePlanId == selectedSubscription.value?.basePlanId) 1f else 0f)
                Row(modifier = Modifier.fillMaxWidth()) {
                    RadioButton(
                        selected = (subscriptionOfferDetails?.basePlanId == selectedSubscription.value?.basePlanId),
                        onClick = {
                            subscriptionOfferDetails
                                ?.let { updateSelectedSubscription(it) }
                        },
                        modifier = visibilityLabelModifier

                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = mainViewModel.planDetails(
                            selectedSubscription.value?.basePlanId ?: ""
                        ).label + "",
                        modifier = visibilityLabelModifier
                            /*.padding(top = 8.dp)
                            .padding(end = 8.dp)*/
                            .clip(MaterialTheme.shapes.small)
                            .background(MaterialTheme.colorScheme.primary)
                            .padding(4.dp),
                        color = MaterialTheme.colorScheme.background,
                        fontSize = 10.sp
                    )
                }
                val paddingModifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                Text(
                    text = subscriptionOfferDetails.pricingPhases.pricingPhaseList.first()?.formattedPrice
                        ?: "",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = paddingModifier,
                    textAlign = TextAlign.End
                )
                Text(
                    text = subscriptionOfferDetails.basePlanId.replace(Regex("\\d+"), ""),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = paddingModifier.padding(bottom = 8.dp)
                )
            }

        }
    }


}

fun splitList(inputList: List<String>): Pair<List<String>, List<String>> {
    val middleIndex = (inputList.size + 1) / 2
    return inputList.subList(0, middleIndex) to inputList.subList(middleIndex, inputList.size)
}