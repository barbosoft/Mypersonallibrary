package org.biblioteca.mypersonallibrary.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import org.biblioteca.mypersonallibrary.data.Llibre
import org.biblioteca.mypersonallibrary.domain.*
import org.biblioteca.mypersonallibrary.ui.components.*

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

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = { CenterAlignedTopAppBar(title = { Text("Biblioteca") }) },
        floatingActionButton = { FloatingActionButton(onClick = onNouLlibre) { Text("+") } }
    ) { padding ->
        Column(Modifier.padding(padding).padding(16.dp)) {
            Row(Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SearchField(query, onQueryChange = { query = it }, modifier = Modifier.weight(1f))
                OrderDropdown(ordre = ordre, onOrdreChange = { ordre = it })
            }
            Spacer(Modifier.height(8.dp))
            SwipeRefresh(
                state = rememberSwipeRefreshState(isRefreshing = loading),
                onRefresh = onRefresh
            ) {
                if (llistaMostrada.isEmpty()) {
                    EmptyState(query, modifier = Modifier.padding(16.dp))
                } else {
                    BooksList(
                        llibres = llistaMostrada,
                        onEdit = onEdit,
                        onEliminar = onEliminar
                    )
                }
            }
        }
    }
}
