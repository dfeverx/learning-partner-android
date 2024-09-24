package app.dfeverx.ninaiva.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.dfeverx.ninaiva.R
import com.valentinilk.shimmer.ShimmerBounds
import com.valentinilk.shimmer.rememberShimmer
import com.valentinilk.shimmer.shimmer

@Composable
fun ProgressCard(
    modifier: Modifier = Modifier,
    isPlayButtonVisible: Boolean = false,
    score: Int = 0,
    levelStage: Int = 1,
    levelProgress: Float = .1f,
    accuracy: Int = 21,
    continuePlay: () -> Unit
) {
    OutlinedCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Row(
            modifier = Modifier
                .padding(top = 16.dp)
                .padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                ImageVector.vectorResource(id = R.drawable.ic_trophy),
                "Go back to previous",
                modifier = Modifier
                    .size(42.dp)
                    .clip(MaterialTheme.shapes.extraLarge)
                    .clickable {
                    }
                    .padding(8.dp),
            )
            Column {
                Text(
                    modifier = Modifier,
                    text = score.toString(),
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    modifier = Modifier,
                    text = "Score ",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            VerticalDivider(
                modifier = Modifier
                    .padding(start = 16.dp, end = 8.dp)
                    .height(32.dp),
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.surfaceVariant
            )
            Icon(
                ImageVector.vectorResource(id = R.drawable.ic_stairs),
                "Go back to previous",
                modifier = Modifier
                    .size(42.dp)
                    .clip(MaterialTheme.shapes.extraLarge)
                    .clickable {
                    }
                    .padding(8.dp),
            )
            Column {
                Text(
                    modifier = Modifier,
                    text = levelStage.toString() + if (levelStage == 1) "st" else if (levelStage == 2) "nd" else if (levelStage == 3) "rd" else "th ",
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    modifier = Modifier,
                    text = "Level",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            VerticalDivider(
                modifier = Modifier
                    .padding(start = 16.dp, end = 8.dp)
                    .height(32.dp),
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.surfaceVariant
            )
            Icon(
                ImageVector.vectorResource(id = R.drawable.ic_target),
                "Go back to previous",
                modifier = Modifier
                    .size(42.dp)
                    .clip(MaterialTheme.shapes.extraLarge)
                    .clickable {
                    }
                    .padding(8.dp),
            )
            Column {
                Text(
                    modifier = Modifier,
                    text = "$accuracy% ",
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    modifier = Modifier,
                    text = "Accuracy",
                    style = MaterialTheme.typography.bodyMedium
                )
            }


        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp), verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    modifier = Modifier.padding(bottom = 8.dp),
                    text = "Overall Progress",
                    style = MaterialTheme.typography.titleLarge
                )
                Row(modifier = Modifier, verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = buildString {
                            append((levelProgress * 100).toInt().toString())
                            append("%")
                        },
                        style = MaterialTheme.typography.labelSmall
                    )
                    LinearProgressIndicator(
                        progress = {
                            levelProgress
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                    )
                }
            }


        }
        if (isPlayButtonVisible) {

            Button(onClick = { continuePlay() }, modifier = Modifier.padding(16.dp)) {
                Icon(
                    Icons.Outlined.PlayArrow,
                    contentDescription = "",
                    modifier = Modifier.padding(end = 16.dp)
                )
                Text(text = "Practice now")
            }
        }

    }
}

@Composable
fun ProgressCardPh(
    modifier: Modifier = Modifier,
    isPlayButtonVisible: Boolean = false,
    score: Int = 0,
    levelStage: Int = 1,
    levelProgress: Float = .1f,
    accuracy: Int = 21,
    continuePlay: () -> Unit,
    isPlaceholder: Boolean = false
) {
    val shimmerInstance = rememberShimmer(shimmerBounds = ShimmerBounds.Window)

    OutlinedCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .then(if (isPlaceholder) Modifier.shimmer(shimmerInstance) else Modifier),
    ) {
        Row(
            modifier = Modifier
                .padding(top = 16.dp)
                .padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically
        ) {
            PlaceholderIcon(isPlaceholder, icon = R.drawable.ic_trophy)
            PlaceholderColumn(
                isPlaceholder = isPlaceholder,
                primaryText = score.toString(),
                secondaryText = "Score"
            )
            VerticalDivider(
                modifier = Modifier
                    .padding(start = 16.dp, end = 8.dp)
                    .height(32.dp),
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.surfaceVariant
            )
            PlaceholderIcon(isPlaceholder, icon = R.drawable.ic_stairs)
            PlaceholderColumn(
                isPlaceholder = isPlaceholder,
                primaryText = levelStage.toString() + if (levelStage == 1) "st" else if (levelStage == 2) "nd" else if (levelStage == 3) "rd" else "th ",
                secondaryText = "Level"
            )
            VerticalDivider(
                modifier = Modifier
                    .padding(start = 16.dp, end = 8.dp)
                    .height(32.dp),
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.surfaceVariant
            )
            PlaceholderIcon(isPlaceholder, icon = R.drawable.ic_target)
            PlaceholderColumn(
                isPlaceholder = isPlaceholder,
                primaryText = "$accuracy%",
                secondaryText = "Accuracy"
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp), verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {

                Text(
                    modifier = Modifier.padding(bottom = 8.dp),
                    text = "Overall Progress",
                    style = MaterialTheme.typography.titleLarge
                )

                Row(modifier = Modifier, verticalAlignment = Alignment.CenterVertically) {
                    if (isPlaceholder) {
                        Box(
                            modifier = Modifier
//                                .fillMaxWidth(0.2f)
                                .height(16.dp)
                                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))
                        )
                    } else {
                        Text(
                            text = buildString {
                                append((levelProgress * 100).toInt().toString())
                                append("%")
                            },
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                    LinearProgressIndicator(
                        progress = {
                            if (isPlaceholder) 0f else levelProgress
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                    )
                }
            }
        }

        if (isPlayButtonVisible) {
            if (isPlaceholder) {
                Box(
                    modifier = Modifier
                        .padding(16.dp)
                        .clip(MaterialTheme.shapes.extraLarge)
                        .fillMaxWidth(0.4f)
                        .height(38.dp)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                )
            } else {
                Button(onClick = { continuePlay() }, modifier = Modifier.padding(16.dp)) {
                    Icon(
                        Icons.Outlined.PlayArrow,
                        contentDescription = "",
                        modifier = Modifier.padding(end = 16.dp)
                    )
                    Text(text = "Practice now")
                }
            }
        }
    }
}

@Composable
fun PlaceholderIcon(isPlaceholder: Boolean, icon: Int) {
    if (isPlaceholder) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))
                .padding(8.dp)
        )
    } else {
        Icon(
            ImageVector.vectorResource(id = icon),
            "Icon",
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .clickable { }
                .padding(8.dp)
        )
    }
}

@Composable
fun PlaceholderColumn(isPlaceholder: Boolean, primaryText: String, secondaryText: String) {
    Column(modifier = Modifier.padding(start = 8.dp)) {
        if (isPlaceholder) {
            Box(
                modifier = Modifier
                    .clip(MaterialTheme.shapes.medium)
                    .width(32.dp)
                    .height(24.dp)
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))
            )
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .clip(MaterialTheme.shapes.medium)
                    .width(46.dp)
                    .height(16.dp)
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))
            )
        } else {
            Text(
                modifier = Modifier,
                text = primaryText,
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                modifier = Modifier,
                text = secondaryText,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}


@Preview
@Composable
fun Preview() {
    ProgressCard(isPlayButtonVisible = true, levelStage = 3) {

    }

}