package org.biblioteca.mypersonallibrary.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.biblioteca.mypersonallibrary.viewModel.WishlistItem
import org.biblioteca.mypersonallibrary.viewModel.WishlistViewModel
import org.biblioteca.mypersonallibrary.viewModel.LlibreViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WishlistScreen(
    llibresVM: LlibreViewModel,
    wishlistVM: WishlistViewModel,
    onBack: () -> Unit
) {
    val items by wishlistVM.items.collectAsState()
    val loading by wishlistVM.loading.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Per comprar") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Enrere")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ✅ key ha de ser Any: fem servir id o un hash estable
            items(
                items = items,
                key = { it.id ?: (it.isbn ?: it.titol ?: it.hashCode()).toString() }
            ) { it ->
                ElevatedCard {
                    Column(Modifier.padding(12.dp)) {
                        Text(
                            text = it.titol ?: it.isbn ?: "(Sense títol)",
                            style = MaterialTheme.typography.titleMedium
                        )
                        it.autor?.let { a ->
                            Text(a, style = MaterialTheme.typography.bodyMedium)
                        }
                        it.notes?.let { n ->
                            if (n.isNotBlank()) {
                                Text(n, style = MaterialTheme.typography.bodySmall)
                            }
                        }

                        Spacer(Modifier.height(8.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // ✅ Comprar -> crea Llibre i l'envia a guardar
                            Button(
                                onClick = {
                                    wishlistVM.purchase(
                                        item = it,
                                        onCreate = { llibre ->
                                            llibresVM.guardarLlibre(llibre) {
                                                // opcional: snack/refresh
                                            }
                                        }
                                    )
                                },
                                enabled = !loading
                            ) { Text("Comprat") }

                            // ✅ Eliminar de la wishlist
                            OutlinedButton(
                                onClick = { it.id?.let { id -> wishlistVM.remove(id) } },
                                enabled = !loading
                            ) { Text("Eliminar") }
                        }
                    }
                }
            }
        }
    }
}

