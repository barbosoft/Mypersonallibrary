package org.biblioteca.mypersonallibrary.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun ScannerOverlay(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width; val h = size.height
        val bw = w * 0.75f; val bh = h * 0.30f
        val left = (w - bw) / 2f; val top = (h - bh) / 3f

        drawRect(color = Color(0x99000000))
        drawRect(
            color = Color.Transparent,
            topLeft = Offset(left, top),
            size = Size(bw, bh),
            blendMode = BlendMode.Clear
        )

        val stroke = Stroke(width = 4.dp.toPx(), pathEffect = PathEffect.cornerPathEffect(8.dp.toPx()))
        drawRoundRect(
            color = Color.White,
            topLeft = Offset(left, top),
            size = Size(bw, bh),
            cornerRadius = CornerRadius(12.dp.toPx(), 12.dp.toPx()),
            style = stroke
        )
    }
}
