package app.dfeverx.ninaiva.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        progress = levelProgress
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

@Preview
@Composable
fun Preview() {
    ProgressCard(isPlayButtonVisible = true, levelStage = 3) {

    }

}