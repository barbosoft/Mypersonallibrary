package org.biblioteca.mypersonallibrary.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.biblioteca.mypersonallibrary.data.WishlistItem
import org.biblioteca.mypersonallibrary.domain.Ordre
import org.biblioteca.mypersonallibrary.ui.components.DiagonalRibbon
import org.biblioteca.mypersonallibrary.ui.components.ListFiltersBar
import org.biblioteca.mypersonallibrary.ui.components.PortadaLlibre
import org.biblioteca.mypersonallibrary.viewModel.LlibreViewModel
import org.biblioteca.mypersonallibrary.viewModel.WishlistViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WishlistScreen(
    wishlistVM: WishlistViewModel,
    llibresVM: LlibreViewModel,
    onBack: (() -> Unit)? = null
) {
    val items by wishlistVM.items.collectAsState()
    val loading by wishlistVM.loading.collectAsState()

    var query by rememberSaveable { mutableStateOf("") }
    var order by rememberSaveable { mutableStateOf(Ordre.RECENT) }

    val filtered = remember(items, query, order) {
        val q = query.trim()
        val base = if (q.isBlank()) items else items.filter {
            it.titol?.contains(q, true) == true ||
                    it.autor?.contains(q, true) == true ||
                    it.isbn?.contains(q, true) == true
        }
        when (order) {
            Ordre.RECENT -> base.sortedByDescending { it.id ?: 0L }
            Ordre.TITOL  -> base.sortedBy { it.titol ?: "~" }
            Ordre.AUTOR  -> base.sortedBy { it.autor ?: "~" }
            Ordre.ISBN   -> base.sortedBy { it.isbn ?: "~" }
        }
    }

    // Snackbar: escolta els missatges que emet la VM
    val snackbarHost = remember { SnackbarHostState() }
    LaunchedEffect(Unit) {
        wishlistVM.snackbar.collect { msg -> snackbarHost.showSnackbar(msg) }
    }

    // Per saber què ja tenim a la biblioteca i mostrar la cinta
    val libraryIsbns by llibresVM.libraryIsbns.collectAsState()
    val already = remember(libraryIsbns) { { isbn: String? -> isbn != null && libraryIsbns.contains(isbn) } }

    // Scroll a dalt quan canvien filtres/llista
    val listState = rememberLazyListState()
    LaunchedEffect(query, order, filtered.size) { listState.scrollToItem(0) }

    // WorkManager
    val appContext = LocalContext.current.applicationContext

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHost) },
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Per comprar") },
                navigationIcon = {
                    onBack?.let {
                        IconButton(onClick = it) {
                            Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Enrere")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // Barra de filtres/ordre (mateixa que a la llista principal)
            ListFiltersBar(
                query = query,
                onQueryChange = { query = it },
                order = order,
                onOrderChange = { order = it }
            )

            if (filtered.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        "No tens cap element a la llista de desitjos.\n" +
                                "Afegeix-ne des del formulari amb “Per comprar”."
                    )
                }
            } else {
                LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filtered, key = { it.id ?: it.hashCode().toLong() }) { item ->
                        WishlistItemCard(
                            item = item,
                            loading = loading,
                            alreadyInLibrary = already(item.isbn),
                            onBuy = { wish ->
                                if (already(wish.isbn)) {
                                    wishlistVM.toast("Ja és a la biblioteca")
                                } else {
                                    // La VM fa el purchase -> elimina de wishlist.
                                    wishlistVM.purchase(wish) { llibreCreat ->
                                        // Desa el llibre a la biblioteca
                                        llibresVM.guardarLlibre(llibreCreat)
                                        wishlistVM.toast("Afegit a la biblioteca: ${llibreCreat.titol ?: ""}")
                                        //llibresVM.refreshAll()
                                        wishlistVM.syncAra(appContext)
                                        //llibresVM.carregaTots()
                                    }
                                }


                                /*
                                // One-off sync (push/delete + pull)
                                WorkManager.getInstance(appContext)
                                    .enqueue(WishlistSyncWorker.oneOff())

                                 */
                            },
                            onDelete = { wish ->
                                wishlistVM.delete(wish)
                                wishlistVM.syncAra(appContext)
                                /*WorkManager.getInstance(appContext)
                                    .enqueue(WishlistSyncWorker.oneOff())

                                 */
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WishlistItemCard(
    item: WishlistItem,
    loading: Boolean,
    alreadyInLibrary: Boolean,
    onBuy: (WishlistItem) -> Unit,
    onDelete: (WishlistItem) -> Unit
) {
    ElevatedCard {
        Row(Modifier.padding(12.dp)) {

            // Caràtula amb la cinta en diagonal si ja existeix a la biblioteca
            val coverSize = 80.dp
            PortadaLlibre(
                imatgeUrl = item.imatgeUrl,
                contentDescription = "Caràtula",
                modifier = Modifier
                    .size(coverSize)
                    .padding(end = 12.dp),
                overlay = {
                    if (alreadyInLibrary) {
                        DiagonalRibbon(
                            text = "COMPRAT",
                            coverSize = coverSize,
                            modifier = Modifier.align(Alignment.Center)  // centrat i creuant tot
                        )
                    }
                }
            )


            Column(Modifier.weight(1f)) {
                Text(
                    text = item.titol ?: "(Sense títol)",
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                item.autor?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }

                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { onBuy(item) },
                        enabled = !loading && !alreadyInLibrary
                    ) { Text("Comprat") }

                    OutlinedButton(
                        onClick = { onDelete(item) },
                        enabled = !loading
                    ) { Text("Eliminar") }
                }
            }
        }
    }
}
