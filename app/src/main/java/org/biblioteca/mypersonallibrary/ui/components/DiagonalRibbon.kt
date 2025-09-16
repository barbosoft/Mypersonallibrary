package org.biblioteca.mypersonallibrary.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.sqrt

@Composable
fun DiagonalRibbon(
    text: String,
    coverSize: Dp,                         // mida del quadrat (portada)
    modifier: Modifier = Modifier,
    angleDeg: Float = -45f,                // BL ➜ TR
    height: Dp = 22.dp
) {
    val density = LocalDensity.current
    // llargada mínima per cobrir la diagonal + un marge perquè toqui cantonades
    val width = with(density) { (coverSize.toPx() * sqrt(2f)).dp } + height

    Box(
        modifier = modifier
            .rotate(angleDeg)
            .width(width)
            .height(height),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(2.dp),
            color = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            tonalElevation = 2.dp,
            shadowElevation = 4.dp
        ) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
