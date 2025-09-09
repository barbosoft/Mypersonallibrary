package org.biblioteca.mypersonallibrary.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.biblioteca.mypersonallibrary.data.Llibre
import org.biblioteca.mypersonallibrary.domain.Ordre
import org.biblioteca.mypersonallibrary.ui.components.BooksList
import org.biblioteca.mypersonallibrary.ui.components.ListFiltersBar
//import org.biblioteca.mypersonallibrary.ui.components.ListOrder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LlibreListScreenTestable(
    llibres: List<Llibre>,
    loading: Boolean,
    missatge: String?,
    onRefresh: () -> Unit,
    onEdit: (Llibre) -> Unit,
    onEliminar: (Llibre) -> Unit,
    onNouLlibre: () -> Unit
) {
    val snackbar = remember { SnackbarHostState() }
    LaunchedEffect(missatge) { missatge?.let { snackbar.showSnackbar(it) } }

    var query by remember { mutableStateOf("") }
    var order by remember { mutableStateOf(Ordre.AUTOR) }

    val listState = rememberLazyListState()
    val pullState = rememberPullToRefreshState()

    // Filtrat + ordre com a producció (sense dependències del mòdul domain)
    val llistaMostrada by remember(llibres, query, order) {
        mutableStateOf(
            run {
                val q = query.trim()
                val base = if (q.isBlank()) llibres else llibres.filter { l ->
                    (l.titol?.contains(q, ignoreCase = true) == true) ||
                            (l.autor?.contains(q, ignoreCase = true) == true) ||
                            (l.isbn?.contains(q, ignoreCase = true) == true) ||
                            (l.idioma?.contains(q, ignoreCase = true) == true)
                }
                when (order) {
                    Ordre.RECENT -> base.sortedByDescending { it.id ?: 0L }
                    Ordre.TITOL  -> base.sortedBy { it.titol ?: "~" }
                    Ordre.AUTOR -> base.sortedBy { it.autor ?: "~" }
                    Ordre.ISBN   -> base.sortedBy { it.isbn ?: "~" }
                }
            }
        )
    }

    // Scroll a dalt en canviar filtre/ordre
    LaunchedEffect(order, query) {
        if (listState.firstVisibleItemIndex != 0 || listState.firstVisibleItemScrollOffset != 0) {
            listState.animateScrollToItem(0)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = { CenterAlignedTopAppBar(title = { Text("Biblioteca") }) },
        floatingActionButton = { FloatingActionButton(onClick = onNouLlibre) { Text("+") } }
    ) { padding ->

        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // Barra de cerca + desplegable d’ordre (inclou testTag "searchField" dins SearchField)
            ListFiltersBar(
                query = query,
                onQueryChange = { query = it },
                order = order,
                onOrderChange = { order = it }
            )

            Spacer(Modifier.height(8.dp))

            PullToRefreshBox(
                isRefreshing = loading,
                onRefresh = onRefresh,
                state = pullState,
                modifier = Modifier.fillMaxSize()
            ) {
                if (llistaMostrada.isEmpty()) {
                    Text(
                        text = if (query.isBlank()) "No hi ha llibres." else "Cap resultat per \"$query\"",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(16.dp)
                    )
                } else {
                    BooksList(
                        llibres = llistaMostrada,
                        onEdit = onEdit,
                        onEliminar = onEliminar,
                        listState = listState
                    )
                }
            }
        }
    }
}
