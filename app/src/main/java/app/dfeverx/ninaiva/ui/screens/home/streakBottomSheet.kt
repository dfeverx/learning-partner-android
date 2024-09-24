package app.dfeverx.ninaiva.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.dfeverx.ninaiva.R
import app.dfeverx.ninaiva.datastore.StreakInfo
import app.dfeverx.ninaiva.ui.components.ModernGrid
import app.dfeverx.ninaiva.utils.TimePeriod
import app.dfeverx.ninaiva.utils.relativeTime
import app.dfeverx.ninaiva.utils.requestPinWidget
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition


@Composable
fun StreakBottomSheetContent(
    schedules: List<HomeViewModel.RepetitionSchedule>,
    streakInfo: State<StreakInfo>,
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.streak))
    val logoAnimationState = animateLottieCompositionAsState(
        composition = composition, iterations = 100, restartOnPlay = false
    )
    Column(
        modifier = Modifier
            .padding(bottom = 16.dp)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        ModernGrid(content = {
            LottieAnimation(modifier = Modifier
                .fillMaxSize(),
                composition = composition,
                progress = { logoAnimationState.progress })
        }) {}

        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            text = "${streakInfo.value.count} Streak",
            style = MaterialTheme.typography.displaySmall,
            textAlign = TextAlign.Center
        )
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            text = "${streakInfo.value.from.relativeTime()} continues",
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )
        OutlinedCard(onClick = { requestPinWidget(context) }) {
            Text(text = "Add widget to home screen", modifier = Modifier
                .padding(horizontal = 16.dp))
        }
        if (!streakInfo.value.isAcknowledgedRest) {
            UtilityCard(
                modifier = Modifier.padding(horizontal = 16.dp),
                title = "You lost!",
                description = "You missed an attempt that result in lost every streak you earned",
                askText = null,
                icon = Icons.Outlined.Warning
            ) {

            }
        }


        if (schedules.isNotEmpty()) {
            Text(
                modifier = Modifier
                    .padding(8.dp)
                    .padding(start = 16.dp)
                    .padding(top = 16.dp),
                text = "Repetition schedules"
            )
            LazyRow(
                modifier = Modifier.padding(bottom = 16.dp),
                contentPadding = PaddingValues(horizontal = 8.dp)
            ) {
                items(schedules) {
                    StreakCalenderItem(it.date, it.month, it.timePeriod == TimePeriod.TODAY)
                }

            }
        }


    }
}

@Composable
fun StreakCalenderItem(day: Int, month: String, isToday: Boolean) {
    Column(
        modifier = Modifier.padding(8.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            modifier = Modifier
                .size(42.dp)
                .padding(4.dp)
                .clip(MaterialTheme.shapes.medium)
                .background(if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                .padding(4.dp),
            color = if (isToday) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
            text = day.toString(), textAlign = TextAlign.Center
        )
        Text(
            modifier = Modifier.padding(4.dp),
            text = month,
            style = MaterialTheme.typography.labelSmall
        )

    }
}





