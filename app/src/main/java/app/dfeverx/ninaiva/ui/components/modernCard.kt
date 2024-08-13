package app.dfeverx.ninaiva.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.dfeverx.ninaiva.R
import coil.compose.AsyncImage
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition

@Composable
fun ModernCard(
    modifier: Modifier = Modifier,
    model: Int? = R.drawable.ic_launcher_foreground,
    lottieAnim: Int? = null,
    handlePractice: () -> Unit
) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.quiz))
    val logoAnimationState = animateLottieCompositionAsState(
        composition = composition, iterations = 3, restartOnPlay = false
    )
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.background,
            disabledContainerColor = MaterialTheme.colorScheme.primary,
            disabledContentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Row(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp)
            ) {
                Text(
                    modifier = Modifier.padding(top = 16.dp),
                    text = "Check your understanding",
                    style = MaterialTheme.typography.displaySmall
                )

                Text(
                    modifier = Modifier
                        .padding(bottom = 8.dp)
                        .padding(top = 4.dp),
                    text = "Want to practice a quiz and understand what you need to understand"
                )

                Button(
                    modifier = Modifier.padding(vertical = 8.dp),
                    onClick = { handlePractice() }, colors = ButtonColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        contentColor = MaterialTheme.colorScheme.primary,
                        disabledContainerColor = MaterialTheme.colorScheme.primary,
                        disabledContentColor = MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    Text(text = "Play quiz now")
                }

            }
//            Spacer(modifier = Modifier.weight(1f))

            LottieAnimation(
                modifier = Modifier
                    .paint(
                        // Replace with your image id
                        painterResource(id = R.drawable.grid),
                        contentScale = ContentScale.FillBounds
                    )


        .size(146.dp)
        .padding(vertical = 2.dp),
        composition = composition,
        progress = { logoAnimationState.progress })
        /* AsyncImage(
             modifier = Modifier.size(146.dp),
             model = model,
             contentDescription = "Logic"
         )*/

    }
}

}


@Preview
@Composable
fun PreviewModernCard() {
    ModernCard {

    }
}