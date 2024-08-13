package app.dfeverx.ninaiva.utils

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp


@Composable
fun TextT() {
    Box(modifier = Modifier.padding(10.dp)) {
        val text = """
            Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.
        """.trimIndent()

        val selectedParts = listOf(
            "consectetur adipiscing",
            "officia deserunt",
            "dolore magna aliqua. Ut enim ad minim veniam, quis nostrud",
            "consequat."
        )

        var selectedPartPaths by remember { mutableStateOf(emptyList<Path>()) }

        Text(
            text = text,
            style = MaterialTheme.typography.headlineSmall,
            onTextLayout = { layoutResult ->
                selectedPartPaths = selectedParts.map { part ->
                    val cornerRadius = CornerRadius(x = 20f, y = 20f)
                    Path().apply {
                        val startIndex = text.indexOf(part)
                        val endIndex = startIndex + part.length
                        val boundingBoxes = (startIndex until endIndex).map { charIndex ->
                            layoutResult.multiParagraph.getBoundingBox(charIndex)
                        }
                        for (i in boundingBoxes.indices) {
                            val boundingBox = boundingBoxes[i].apply {

                            }
                            val leftCornerRoundRect =
                                if (i == 0) cornerRadius else CornerRadius.Zero
                            val rightCornerRoundRect =
                                if (i == boundingBoxes.lastIndex) cornerRadius else CornerRadius.Zero
                            addRoundRect(
                                RoundRect(
                                    boundingBox.inflate(delta = -7f),
                                    topLeft = leftCornerRoundRect,
                                    topRight = rightCornerRoundRect,
                                    bottomRight = rightCornerRoundRect,
                                    bottomLeft = leftCornerRoundRect
                                )
                            )
                        }
                    }
                }
            },
            modifier = Modifier.drawBehind {
                selectedPartPaths.forEach { path ->
                    drawPath(path, color = Color.Blue.copy(alpha = 0.2f), style = Fill)
                    drawPath(path, color = Color.Blue, style = Stroke(width = 2f))
                }
            }
        )
    }
}

private fun Rect.inflate(delta: Float): Rect {
    return Rect(
        left = left - delta,
        top = top - delta,
        right = right + delta,
        bottom = bottom + delta
    )
}