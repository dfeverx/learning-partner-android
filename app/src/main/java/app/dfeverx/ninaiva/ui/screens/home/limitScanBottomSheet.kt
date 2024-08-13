package app.dfeverx.ninaiva.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import app.dfeverx.ninaiva.R
import app.dfeverx.ninaiva.ui.Screens
import app.dfeverx.ninaiva.ui.components.ContinueWithGoogleButton
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition

@Composable
fun LimitScanBottomSheetContent(
    isAnonymous: Boolean = true,
    isPro: Boolean = false,
    navigateTo: (String) -> Unit,
    noteCount: Int = 1,
    onError: (String) -> Unit,
    isEligibleForNoteCreation: Boolean = false,
    launchScanner: () -> Unit,
) {
    if (isAnonymous) {
//        anonymous
        if (isEligibleForNoteCreation) {
            LimitScanContent(
                title = "Anonymous?",
                description = "You are currently using the app anonymously. To create more notes, please authenticate.",
                content = {
                    ContinueWithGoogleButton(modifier = Modifier.fillMaxWidth(),
                        onSuccess = {
                            launchScanner()
                        },
                        onFailure = {
                            onError("Failed to authenticate")
                        }) {

                    }
                },
                toNext = launchScanner
            )
        } else {
            LimitScanContent(
                title = "Authentication Required",
                description = "You must be authenticated to create more notes.",
                content = {
                    ContinueWithGoogleButton(modifier = Modifier.fillMaxWidth(), onSuccess = {
                        launchScanner()
                    },
                        onFailure = {
                            onError("Failed to authenticate")
                        }) {

                    }
                },
                toNext = launchScanner,
                isEnd = true
            )
        }

    } else if (isPro) {
//        auth+pro
        if (isEligibleForNoteCreation) {
            LimitScanContent(
                title = "Quota expire soon !",
                description = "You have reached your scan limit for this month. Upgrade to Premium for unlimited scans.",
                isEnd = true,
                toNext = launchScanner
            )
        } else {
            LimitScanContent(
                title = "Quota Exceeded",
                description = "You have reached your scan limit for this month. Upgrade to Premium for unlimited scans.",
                isEnd = true,
                toNext = launchScanner
            )
        }

    } else {
//        auth
        if (isEligibleForNoteCreation) {
            LimitScanContent(
                title = "⚡Upgrade now",
                description = "Enjoy unlimited scans and other premium features also support the app.",
                toNext = launchScanner,
                content = {
                    Button(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        onClick = { navigateTo(Screens.Upgrade.route) }
                    ) {
                        Text(text = "View plans")
                    }
                },
                list = listOf(
                    "Free" to "Pro",
                    "Ad" to "Ad free",
                    "3 notes/month" to "30 notes/month"
                )
            )
        } else {

            LimitScanContent(
                title = "Quota Exceeded",
                description = "You have reached your scan limit for this month. Upgrade to Premium or wait for the next month .",
                toNext = launchScanner,
                content = {
                    Button(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        onClick = { navigateTo(Screens.Upgrade.route) }
                    ) {
                        Text(text = "View plans")
                    }
                },
                list = listOf(
                    "Free" to "Pro",
                    "Ad" to "Ad free",
                    "3 notes/month" to "30 notes/month"
                ), isEnd = true
            )
        }
    }


}

@Composable
fun LimitScanContent(
    title: String,
    description: String,
    toNext: () -> Unit,
    list: List<Pair<String, String>>? = null,
    content: (@Composable () -> Unit?)? = null,
    isEnd: Boolean = false
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .padding(bottom = 32.dp)
            .fillMaxWidth()
            .verticalScroll(scrollState)
            .padding(horizontal = 32.dp),
    ) {
        val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.peeking))
        val logoAnimationState = animateLottieCompositionAsState(
            composition = composition, iterations = 100, restartOnPlay = false
        )
        /* ModernGrid(content = {
             LottieAnimation(modifier = Modifier
                 .fillMaxSize(),
                 composition = composition,
                 progress = { logoAnimationState.progress })
         }) {}*/

        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            text = title,
            style = MaterialTheme.typography.displaySmall,

            )
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .padding(bottom = 8.dp),
            text = description,
//            style = MaterialTheme.typography.titleMedium,

        )

        list?.forEachIndexed { index, item ->
            Row(
                modifier = Modifier
                    .padding(bottom = 4.dp)
                    .clip(MaterialTheme.shapes.medium)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    modifier = Modifier
                        .weight(1f)
                        .padding(4.dp),
                    text = item.first,
                    style = if (index == 0) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyLarge
                )

                Text(
                    modifier = Modifier
                        .weight(1f)
                        .padding(4.dp),
                    text = item.second,
                    style = if (index == 0) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyLarge
                )
            }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            content?.let { it() }
            if (!isEnd) {
                OutlinedButton(
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .padding(bottom = 16.dp)
//                        .padding(vertical = 4.dp)
                        .fillMaxWidth(),
                    onClick = { toNext() }) {
                    Text(text = "Skip for now")
                }
            }
        }
    }
}