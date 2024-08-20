package app.dfeverx.ninaiva.ui.screens.home

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.BlendMode.Companion.Screen
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import app.dfeverx.ninaiva.ui.Screens
import app.dfeverx.ninaiva.ui.components.ClickableItem
import app.dfeverx.ninaiva.ui.components.PrivacyPolicyLinks
import app.dfeverx.ninaiva.ui.main.MainViewModel
import app.dfeverx.ninaiva.ui.screens.home.profile.ProfileInfo
import app.dfeverx.ninaiva.utils.activityViewModel
import com.google.firebase.auth.FirebaseUser


@Composable
fun UserProfileBottomSheetContent(
    userProfile: FirebaseUser?,
    navHostController: NavHostController,
    creditRemain: Int = 0,
    isUserProfile: Boolean,
    viewStreak: () -> Unit,
    actionDone: () -> Unit,
) {
//    val userProfile = FirebaseAuth.getInstance().currentUser
    val mainViewModel = activityViewModel<MainViewModel>()

    val activity = LocalContext.current as Activity
    val listItems = if (/*artWorkViewModel.isPremiumPlanVisible()*/isUserProfile) {
        listOf(
//            ClickableItem(title = "History", icon = Icons.Outlined.History, route = ""),


            if (mainViewModel.isPro.collectAsState().value && mainViewModel.auth.currentUser?.isAnonymous != true) ClickableItem(
                title = "Subscription",
                icon = Icons.Outlined.Payment,
                route = ""
            ) else if (mainViewModel.auth.currentUser?.isAnonymous != true)
                ClickableItem(
                    title = "Upgrade",
                    icon = Icons.Outlined.ElectricBolt,
                    route = ""
                ) else ClickableItem(title = "", icon = Icons.Outlined.ElectricBolt, route = ""),
            ClickableItem(title = "Feedback", icon = Icons.Outlined.Feedback, route = ""),
            ClickableItem(title = "Share", icon = Icons.Outlined.Share, route = ""),
        )
    } else {
        listOf(
            ClickableItem(
                title = "New Conversation",
                icon = Icons.Outlined.ChatBubbleOutline,
                route = ""
            ),
            ClickableItem(title = "Feedback", icon = Icons.Outlined.Feedback, route = ""),
            ClickableItem(title = "Share", icon = Icons.Outlined.Share, route = ""),
        )
    }
    LazyColumn(
        modifier = Modifier

            .padding(bottom = 32.dp)
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            ProfileInfo(
                userProfile,
                isUserProfile,
                viewStreak = viewStreak,
                creditRemain = creditRemain,
                authSuccess = {
                    actionDone()
                    Toast.makeText(activity, "Google authentication successful", Toast.LENGTH_LONG)
                        .show()
                },
//                artWorkViewModel.isPro.collectAsState(),
//                isPremiumPlanVisible = artWorkViewModel.isPremiumPlanVisible(),
                /*creditRemain = artWorkViewModel.creditAndEndIn.collectAsState()*/
                modifier = Modifier.padding(horizontal = 32.dp),
                navHostController = navHostController,
                onFailureAuth = {
                    Toast.makeText(activity, it, Toast.LENGTH_LONG).show()
                }
            )
//            Divider()
        }

        items(listItems) { item ->
            ClickableItem(item = item, navigateTo = {
                actionDone()
                when (item.title) {


                    "Subscription" -> {
                        val browserIntent = Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://play.google.com/store/account/subscriptions")
                        )
                        activity.startActivity(browserIntent)
                    }

                    "Upgrade" -> {
                        navHostController.navigate(Screens.Upgrade.route)
                        /*    launchBilling(activity)*/
//                        navHostController.navigate(Screen.SubscriptionScreen.route)
                    }

                    "Feedback" -> {
                        try {
                            activity.startActivity(
                                Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("market://details?id=${activity.packageName}")
                                )
                            )
                        } catch (e: ActivityNotFoundException) {
                            activity.startActivity(
                                Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("https://play.google.com/store/apps/details?id=${activity.packageName}")
                                )
                            )
                        }
                    }

                    "Share" -> {
                        /*Create an ACTION_SEND Intent*/

                        /*Create an ACTION_SEND Intent*/
                        val intent1 = Intent(Intent.ACTION_SEND)
                        /*This will be the actual content you wish you share.*/
                        /*This will be the actual content you wish you share.*/
                        val shareBody =
                            "Try this amazing AI chat bot  https://play.google.com/store/apps/details?id=${activity.packageName}"
                        /*The type of the content is text, obviously.*/
                        /*The type of the content is text, obviously.*/intent1.type = "text/plain"
                        /*Applying information Subject and Body.*/
                        /*Applying information Subject and Body.*/intent1.putExtra(
                            Intent.EXTRA_SUBJECT,
                            " Share this app"
                        )
                        intent1.putExtra(Intent.EXTRA_TEXT, shareBody)
                        /*Fire!*/
                        /*Fire!*/activity.startActivity(
                            Intent.createChooser(
                                intent1, "Share this app"
                            )
                        )
                    }

                    else -> {}
                }
            })
        }

        item {
            PrivacyPolicyLinks(activity)
        }
    }
}






