package app.dfeverx.ninaiva.ui.screens.flash

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.dfeverx.ninaiva.R
import app.dfeverx.ninaiva.models.local.FlashCard
import coil.compose.AsyncImage

enum class BoxState { Front, Back }

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FlippableCard(flashCard: FlashCard) {
    var rotated by remember {
        mutableStateOf(false)
    }
    val interactionSource = remember { MutableInteractionSource() }
    val transitionData = updateTransitionData(
        if (rotated) BoxState.Back else BoxState.Front
    )
    Card(
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shape = MaterialTheme.shapes.large,
        elevation = 16.dp,
        modifier = Modifier
            .padding(32.dp)
            .aspectRatio(2f / 3f)
            .fillMaxWidth()

            .graphicsLayer {
                rotationY = transitionData.rotation
                cameraDistance = 8 * density
            }
//            .clickable { onRotate(!rotated) }
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = { },
                onDoubleClick = { rotated = !rotated },
            ),
        backgroundColor = transitionData.color
    )
    {
        Column(
            Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (!rotated) {
                Box(contentAlignment = Alignment.Center) {

                    AsyncImage(
                        modifier = Modifier
                            .size(120.dp)
                            .graphicsLayer {
                                alpha =
                                    if (rotated) transitionData.animateBack else transitionData.animateFront
                                rotationY = transitionData.rotation
                            },
                        model = R.drawable.grid,
                        contentDescription = ""
                    )
                    Text(
                        text = flashCard.emoji,
                        style = MaterialTheme.typography.displayLarge,
                        modifier = Modifier
//                        .height(168.dp)
                            .graphicsLayer {
                                alpha =
                                    if (rotated) transitionData.animateBack else transitionData.animateFront
                                rotationY = transitionData.rotation
                            }, textAlign = TextAlign.Center
                    )
                }

            }

            Spacer(modifier = Modifier.padding(vertical = 16.dp))

            Text(
                text = if (rotated) flashCard.info else flashCard.prompt,
                textAlign = if (rotated) TextAlign.Left else TextAlign.Center,
                modifier = Modifier
                    .graphicsLayer {
                        alpha =
                            if (rotated) transitionData.animateBack else transitionData.animateFront
                        rotationY =
                            if (rotated) -transitionData.rotation else transitionData.rotation
                    },
                style = if (rotated) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.displaySmall
            )
        }

    }
}


private class TransitionData(
    color: State<Color>,
    rotation: State<Float>,
    animateFront: State<Float>,
    animateBack: State<Float>
) {
    val color by color
    val rotation by rotation
    val animateFront by animateFront
    val animateBack by animateBack
}


@Composable
private fun updateTransitionData(boxState: BoxState): TransitionData {
    val transition = updateTransition(boxState, label = "")
    val color = transition.animateColor(
        transitionSpec = {
            tween(500)
        },
        label = ""
    ) { state ->
        when (state) {
            BoxState.Front -> MaterialTheme.colorScheme.surface
            BoxState.Back -> MaterialTheme.colorScheme.surface
        }
    }
    val rotation = transition.animateFloat(
        transitionSpec = {
            tween(500)
        },
        label = ""
    ) { state ->
        when (state) {
            BoxState.Front -> 0f
            BoxState.Back -> 180f
        }
    }

    val animateFront = transition.animateFloat(
        transitionSpec = {
            tween(500)
        },
        label = ""
    ) { state ->
        when (state) {
            BoxState.Front -> 1f
            BoxState.Back -> 0f
        }
    }
    val animateBack = transition.animateFloat(
        transitionSpec = {
            tween(500)
        },
        label = ""
    ) { state ->
        when (state) {
            BoxState.Front -> 0f
            BoxState.Back -> 1f
        }
    }

    return remember(transition) { TransitionData(color, rotation, animateFront, animateBack) }
}


@Preview

@Composable
fun PreviewFCard() {
    FlippableCard(
        flashCard = FlashCard(
            emoji = "✅",
            prompt = "Laws of motions",
            info = "Newtons laws of motion state that ... "
        )
    )
}