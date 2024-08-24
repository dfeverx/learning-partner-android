package app.dfeverx.ninaiva.ui.screens.statistics

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.dfeverx.ninaiva.R
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition


@Composable
fun StreakAddedCard(title: String, description: String) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.streak))
    val logoAnimationState = animateLottieCompositionAsState(
        composition = composition, iterations = 30, restartOnPlay = false
    )
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp,/* vertical = 16.dp*/),
    ) {
        Row(
            modifier = Modifier
                .padding(vertical = 16.dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LottieAnimation(modifier = Modifier
                .size(46.dp)
                .padding(vertical = 2.dp),
                composition = composition,
                progress = { logoAnimationState.progress })

            Column(modifier = Modifier
                .padding(start = 16.dp)
                .weight(1f)) {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = "$title Streaks",
                    style = MaterialTheme.typography.titleLarge
                )
                Text(modifier = Modifier.fillMaxWidth(), text = description)
            }

            /* Text(
                 modifier = Modifier,
                 text = "(+1)",
                 style = MaterialTheme.typography.titleLarge,
                 color = Color.Green
             )*/

        }
    }
}