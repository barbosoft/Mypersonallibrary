package org.biblioteca.mypersonallibrary.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.biblioteca.mypersonallibrary.data.Llibre
import org.biblioteca.mypersonallibrary.domain.Ordre
import org.biblioteca.mypersonallibrary.viewModel.LlibreViewModel
import org.biblioteca.mypersonallibrary.ui.components.ListFiltersBar
import org.biblioteca.mypersonallibrary.ui.components.PortadaLlibre
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LlibreListScreen(
    viewModel: LlibreViewModel,
    onEdit: (Llibre) -> Unit,
    onNouLlibre: () -> Unit,
    onOpenWishList: () -> Unit
) {
    val llibres by viewModel.totsElsLlibres.collectAsState()

    // 1) Pull inicial en obrir la pantalla
    LaunchedEffect(Unit) {
        viewModel.refreshAll()
    }

    var query by rememberSaveable { mutableStateOf("") }
    var order by rememberSaveable { mutableStateOf(Ordre.RECENT) }

    val filtered = remember(llibres, query, order) {
        val q = query.trim().lowercase()
        val base = if (q.isBlank()) llibres else llibres.filter { l ->
            (l.titol?.contains(q, true) == true) ||
                    (l.autor?.contains(q, true) == true) ||
                    (l.isbn?.contains(q, true) == true) ||
                    (l.idioma?.contains(q, true) == true)
        }
        when (order) {
            Ordre.RECENT -> base.sortedByDescending { it.id ?: 0L }
            Ordre.TITOL  -> base.sortedBy { it.titol ?: "~" }
            Ordre.AUTOR  -> base.sortedBy { it.autor ?: "~" }
            Ordre.ISBN   -> base.sortedBy { it.isbn ?: "~" }
        }
    }

    // endreçar llistat automàticament
    val listState = rememberLazyListState()

    // cada canvi d'ítem o de filtre ho posa a dalt
    LaunchedEffect(query, order, filtered.size) {
        listState.scrollToItem(0)
    }

    // 2) Pull cada cop que la pantalla torna al davant (back, després d’editar, després de “comprar”, etc.)
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshAll()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(Unit) {
        viewModel.refreshAll() // si falla la xarxa, no esborra res
    }

    Scaffold(
topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Biblioteca") },
                actions = {
                    IconButton(onClick = onOpenWishList) {
                        Icon(Icons.Outlined.ShoppingCart, contentDescription = "Per comprar")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNouLlibre) {
                Icon(Icons.Outlined.Add, contentDescription = "Afegir")
            }
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // 🔁 Mateix component de filtres que a la Wishlist
            ListFiltersBar(
                query = query,
                onQueryChange = { query = it },
                order = order,
                onOrderChange = { order = it }
            )

            if (filtered.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No hi ha llibres per mostrar.")
                }
            } else {
                LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filtered, key = { it.id ?: it.hashCode().toLong() }) { l ->
                        LlibreRow(l, onClick = { onEdit(l) })
                    }
                }
            }
        }
    }
}

@Composable
private fun LlibreRow(l: Llibre, onClick: () -> Unit) {
    ElevatedCard(
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Row(Modifier.padding(12.dp)) {

            // ✅ ÚNIC CANVI: fem servir PortadaLlibre amb fallback “Sense portada”
            PortadaLlibre(
                imatgeUrl = l.imatgeUrl,
                contentDescription = "Caràtula",
                modifier = Modifier
                    .size(80.dp)
                    .padding(end = 12.dp)
            )

            Column(Modifier.weight(1f)) {
                Text(
                    text = l.titol ?: "(Sense títol)",
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                l.autor?.let {
                    Text(it, style = MaterialTheme.typography.bodyMedium)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    l.isbn?.let { Text("ISBN: $it", style = MaterialTheme.typography.bodySmall) }
                    l.idioma?.let { Text("· $it", style = MaterialTheme.typography.bodySmall) }
                    l.pagines?.takeIf { it > 0 }?.let {
                        Text("· ${it}p", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}
