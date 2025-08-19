package org.biblioteca.mypersonallibrary.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.lazy.rememberLazyListState
import org.biblioteca.mypersonallibrary.data.Llibre
import org.biblioteca.mypersonallibrary.domain.Ordre
import org.biblioteca.mypersonallibrary.domain.filtreLlibres
import org.biblioteca.mypersonallibrary.domain.ordenaLlibres
import org.biblioteca.mypersonallibrary.ui.components.BooksList
import org.biblioteca.mypersonallibrary.ui.components.OrderDropdown
import org.biblioteca.mypersonallibrary.ui.components.SearchField

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
    var ordre by remember { mutableStateOf(Ordre.AUTOR) }

    val llistaMostrada = remember(llibres, query, ordre) {
        ordenaLlibres(filtreLlibres(llibres, query), ordre)
    }

    val pullState = rememberPullToRefreshState()
    val listState = rememberLazyListState()

    LaunchedEffect(ordre, query) {
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
                .fillMaxSize()                 // ✅ BOUNDED HEIGHT
                .padding(padding)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SearchField(
                    query = query,
                    onQueryChange = { query = it },
                    modifier = Modifier.weight(1f)
                )
                OrderDropdown(
                    ordre = ordre,
                    onOrdreChange = { ordre = it }
                )
            }

            Spacer(Modifier.height(8.dp))

            PullToRefreshBox(
                isRefreshing = loading,
                onRefresh = onRefresh,
                state = pullState,
                modifier = Modifier.fillMaxSize()   // ✅ també bounded
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
