package app.dfeverx.ninaiva.ui.screens.levels

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.dfeverx.ninaiva.R
import app.dfeverx.ninaiva.ui.components.ModernGrid
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition

@Composable
fun LevelNotReadyBottomSheetContent(modifier: Modifier, handleOk: () -> Unit) {
    Column(
        modifier = modifier
            .padding()
            .padding(horizontal = 32.dp)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally

    ) {
        val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.peeking))
        val logoAnimationState = animateLottieCompositionAsState(
            composition = composition, iterations = 100, restartOnPlay = false
        )
        ModernGrid(content = {
            LottieAnimation(modifier = Modifier
                .fillMaxSize(),
                composition = composition,
                progress = { logoAnimationState.progress })
        }) {}

        Text(
            modifier = Modifier.padding(top = 8.dp),
            text = "Locked !",
            style = MaterialTheme.typography.displaySmall
        )
        Text(
            modifier = Modifier.padding(vertical = 16.dp),
            text = "You have to unlock previous level to access this level,if you want to practice you can play at the top or you can play level that already unlocked"
        )
        Button(modifier = Modifier
            .padding(top = 16.dp, bottom = 8.dp)
            .fillMaxWidth(),
            onClick = { handleOk() }) {
            Text(text = "OK")
        }

    }
}