package org.biblioteca.mypersonallibrary.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import kotlinx.coroutines.delay
import org.biblioteca.mypersonallibrary.viewModel.LlibreViewModel

@Composable
fun BusyOverlay(show: Boolean) {
    AnimatedVisibility(
        visible = show,
        enter = fadeIn() + scaleIn(),
        exit = fadeOut()
    ) {
        Box(
            modifier = androidx.compose.ui.Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.35f)),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}

/**
 * Retarda la mostra del “busy” per evitar parpelleig i
 * assegura un temps mínim visible quan arriba a aparèixer.
 */
@Composable
fun rememberSmartBusy(
    busy: Boolean,
    showDelayMs: Long = 120,  // espera abans de mostrar
    minShowMs: Long = 400     // temps mínim visible
): State<Boolean> {
    val visible: MutableState<Boolean> = remember { mutableStateOf(false) }
    val shownAt = remember { mutableStateOf<Long?>(null) }

    LaunchedEffect(busy) {
        if (busy) {
            delay(showDelayMs)
            if (busy) {
                visible.value = true
                shownAt.value = System.currentTimeMillis()
            }
        } else {
            if (visible.value) {
                val elapsed = System.currentTimeMillis() - (shownAt.value ?: 0L)
                val remain = (minShowMs - elapsed).coerceAtLeast(0)
                if (remain > 0) delay(remain)
            }
            visible.value = false
            shownAt.value = null
        }
    }
    return visible
}

@Composable
fun NavBusyBinder(nav: NavHostController, vm: LlibreViewModel) {
    DisposableEffect(nav) {
        val listener = NavController.OnDestinationChangedListener { _, _, _ ->
            vm.endNav() // qualsevol canvi de pantalla = deixa d’estar “navegant”
        }
        nav.addOnDestinationChangedListener(listener)
        onDispose { nav.removeOnDestinationChangedListener(listener) }
    }
}
