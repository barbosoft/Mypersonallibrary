package org.biblioteca.mypersonallibrary.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import org.biblioteca.mypersonallibrary.R

@Composable
private fun NoCoverPlaceholder(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(8.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Outlined.Image,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.no_cover),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun CoverImageOrPlaceholder(
    url: String?,
    modifier: Modifier = Modifier
        .fillMaxWidth()
        .heightIn(min = 160.dp, max = 260.dp)
        .clip(RoundedCornerShape(12.dp)),
    contentScale: ContentScale = ContentScale.Fit
) {
    if (url.isNullOrBlank()) {
        NoCoverPlaceholder(modifier)
        return
    }

    val ctx = LocalContext.current
    val model = remember(url) {
        ImageRequest.Builder(ctx)
            .data(url)
            .crossfade(true)
            .build()
    }

    SubcomposeAsyncImage(
        model = model,
        contentDescription = stringResource(R.string.cover_desc),
        contentScale = contentScale,
        modifier = modifier,
        loading = { NoCoverPlaceholder(Modifier.matchParentSize()) },
        error   = { NoCoverPlaceholder(Modifier.matchParentSize()) }
    )
}
