package app.dfeverx.ninaiva.utils

import androidx.compose.foundation.border
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlin.math.min
import kotlin.math.sqrt

@Composable
fun Modifier.borderBottom(
    lineThicknessDp: Dp = 1.dp,
    color: Color = MaterialTheme.colorScheme.surfaceVariant
): Modifier {
    val lineThicknessPx = with(LocalDensity.current) { lineThicknessDp.toPx() }
    val bottomLineShape = GenericShape { size, _ ->
        // 1) Bottom-left corner
        moveTo(0f, size.height)
        // 2) Bottom-right corner
        lineTo(size.width, size.height)
        // 3) Top-right corner
        lineTo(size.width, size.height - lineThicknessPx)
        // 4) Top-left corner
        lineTo(0f, size.height - lineThicknessPx)
    }
    val newM = Modifier.border(
        width = lineThicknessDp,
        color = color,
        shape = bottomLineShape
    )
    this.then(newM)
    return newM
}
class HexagonShape : Shape {

    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        return Outline.Generic(
            path = drawCustomHexagonPath(size)
        )
    }
}

fun drawCustomHexagonPath(size: Size): Path {
    return Path().apply {
        val radius = min(size.width / 2f, size.height / 2f)
        customHexagon(radius, size)
    }
}

fun Path.customHexagon(radius: Float, size: Size) {
    val triangleHeight = (sqrt(3.0) * radius / 2)
    val centerX = size.width / 2
    val centerY = size.height / 2

    moveTo(centerX, centerY + radius)
    lineTo((centerX - triangleHeight).toFloat(), centerY + radius/2)
    lineTo((centerX - triangleHeight).toFloat(), centerY - radius/2)
    lineTo(centerX, centerY - radius)
    lineTo((centerX + triangleHeight).toFloat(), centerY - radius/2)
    lineTo((centerX + triangleHeight).toFloat(), centerY + radius/2)

    close()
}
