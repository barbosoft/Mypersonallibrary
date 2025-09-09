package org.biblioteca.mypersonallibrary.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import coil.request.ImageRequest

/**
 * Construeix i memoritza un ImageRequest per a Coil amb crossfade.
 * Ús: AsyncImage(model = rememberCoilModel(url), ...)
 */
@Composable
fun rememberCoilModel(data: Any?): ImageRequest {
    val context = LocalContext.current
    return remember(data) {
        ImageRequest.Builder(context)
            .data(data)
            .crossfade(true)
            .build()
    }
}

