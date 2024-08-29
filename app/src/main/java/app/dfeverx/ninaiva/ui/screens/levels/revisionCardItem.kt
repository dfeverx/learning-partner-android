package app.dfeverx.ninaiva.ui.screens.levels

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import app.dfeverx.ninaiva.R
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition


@Composable
fun RevisionCardItem(isPlayable: Boolean = false, onClickPlay: () -> Unit) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.bulb_flash))
    val logoAnimationState = animateLottieCompositionAsState(
        composition = composition, iterations = 100, restartOnPlay = false
    )
    OutlinedCard(
        modifier = Modifier
            .padding(16.dp)
            .clip(MaterialTheme.shapes.small)
            .clickable { onClickPlay() }
            .fillMaxWidth(), border = BorderStroke(4.dp, MaterialTheme.colorScheme.surfaceVariant)

    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {

            LottieAnimation(modifier = Modifier
                .padding(vertical = 8.dp)
                .size(92.dp),
                composition = composition,
                progress = { logoAnimationState.progress })

            Text(
                modifier = Modifier,
                text = "The final touch!",
                style = MaterialTheme.typography.headlineLarge
            )


            Text(
                modifier = Modifier.padding(vertical = 16.dp),
                text = "Attempt this revision and make sure that everything flash in your brain!",
                style = MaterialTheme.typography.bodyLarge
            )


            Row(
                modifier = Modifier
                    .padding(8.dp)
                    .clip(MaterialTheme.shapes.extraLarge)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(vertical = 8.dp, horizontal = 16.dp)
            ) {
                Icon(imageVector = Icons.Outlined.MenuBook, contentDescription = "")
                Text(modifier = Modifier.padding(start = 16.dp), text = "Revision")
            }

        }
    }
}