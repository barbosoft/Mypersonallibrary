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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.work.WorkManager
import coil.compose.AsyncImage
import org.biblioteca.mypersonallibrary.data.WishlistItem
import org.biblioteca.mypersonallibrary.data.sync.WishlistSyncWorker
import org.biblioteca.mypersonallibrary.domain.Ordre
import org.biblioteca.mypersonallibrary.ui.components.ListFiltersBar
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
        val q = query.trim().lowercase()
        val base = if (q.isBlank()) items else items.filter {
            it.titol?.contains(q, true) == true ||
                    it.autor?.contains(q, true) == true ||
                    it.isbn?.contains(q, true) == true
        }
        when (order) {
            Ordre.RECENT -> base.sortedByDescending { it.id ?: 0L }
            Ordre.TITOL  -> base.sortedBy { it.titol ?: "~" }
            Ordre.AUTOR -> base.sortedBy { it.autor ?: "~" }
            Ordre.ISBN -> base.sortedBy { it.isbn ?: "~"}
        }
    }

    // 🔄 Context per al WorkManager
    val appContext = LocalContext.current.applicationContext

    Scaffold(
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
            // 🔁 Mateix component de filtres que a la llista principal
            ListFiltersBar(
                query = query,
                onQueryChange = { query = it },
                order = order,
                onOrderChange = { order = it }
            )

            if (filtered.isEmpty()) {
                Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No tens cap element a la llista de desitjos.\n" +
                                "Afegeix-ne des del formulari amb “Per comprar”."
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filtered, key = { it.id ?: it.hashCode().toLong() }) { it ->
                        WishlistItemCard(
                            item = it,
                            loading = loading,
                            onBuy = { item ->
                                wishlistVM.purchase(item) { llibre ->
                                    // desa al backend via VM principal
                                    llibresVM.guardarLlibre(llibre) {
                                        // sense navegació aquí; només feedback/refresh intern
                                    }
                                }
                                // 🔄 Sync immediat (push delete de wishlist i, si cal, pull)
                                WorkManager.getInstance(appContext)
                                    .enqueue(WishlistSyncWorker.oneOff())
                            },
                            onRemove = { id ->
                                wishlistVM.remove(id)
                                wishlistVM.remove(id)
                                // 🔄 Sync immediat per propagar l’eliminació
                                WorkManager.getInstance(appContext)
                                    .enqueue(WishlistSyncWorker.oneOff())
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
    onBuy: (WishlistItem) -> Unit,
    onRemove: (Long) -> Unit
) {
    ElevatedCard {
        Row(Modifier.padding(12.dp)) {
            AsyncImage(
                model = item.imatgeUrl,
                contentDescription = "Caràtula",
                modifier = Modifier
                    .size(80.dp)
                    .padding(end = 12.dp)
            )

            Column(Modifier.weight(1f)) {
                Text(
                    text = item.titol ?: "(Sense títol)",
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                item.autor?.let {
                    Text(it, style = MaterialTheme.typography.bodyMedium)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item.isbn?.let { Text("ISBN: $it", style = MaterialTheme.typography.bodySmall) }
                    item.idioma?.let { Text("· $it", style = MaterialTheme.typography.bodySmall) }
                    item.pagines?.takeIf { it > 0 }?.let {
                        Text("· ${it}p", style = MaterialTheme.typography.bodySmall)
                    }
                }
                item.notes?.takeIf { it.isNotBlank() }?.let {
                    Spacer(Modifier.height(4.dp))
                    Text(it, style = MaterialTheme.typography.bodySmall, maxLines = 3, overflow = TextOverflow.Ellipsis)
                }

                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { onBuy(item) },
                        enabled = !loading
                    ) { Text("Comprat") }

                    OutlinedButton(
                        onClick = { item.id?.let(onRemove) },
                        enabled = !loading
                    ) { Text("Eliminar") }
                }
            }
        }
    }
}
