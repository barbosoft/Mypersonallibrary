package org.biblioteca.mypersonallibrary.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import org.biblioteca.mypersonallibrary.domain.*
import org.biblioteca.mypersonallibrary.ui.components.*
import org.biblioteca.mypersonallibrary.viewModel.LlibreViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LlibreListScreen(
    viewModel: LlibreViewModel,
    onEdit: () -> Unit,
    onNouLlibre: () -> Unit
) {
    val llibres by viewModel.totsElsLlibres.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val missatge by viewModel.missatge.collectAsState()

    val snackbar = remember { SnackbarHostState() }
    LaunchedEffect(missatge) {
        missatge?.let { snackbar.showSnackbar(it); viewModel.netejarMissatge() }
    }

    var query by rememberSaveable { mutableStateOf("") }
    var ordre by rememberSaveable { mutableStateOf(Ordre.AUTOR) }

    val llistaMostrada = remember(llibres, query, ordre) {
        ordenaLlibres(filtreLlibres(llibres, query), ordre)
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = { CenterAlignedTopAppBar(title = { Text("Biblioteca") }) },
        floatingActionButton = { FloatingActionButton(onClick = onNouLlibre) { Text("+") } }
    ) { padding ->

        Column(Modifier.padding(padding).padding(16.dp)) {

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

            SwipeRefresh(
                state = rememberSwipeRefreshState(isRefreshing = loading),
                onRefresh = { viewModel.carregaTots() }
            ) {
                if (llistaMostrada.isEmpty()) {
                    EmptyState(query, modifier = Modifier.padding(16.dp))
                } else {
                    BooksList(
                        llibres = llistaMostrada,
                        onEdit = { l -> viewModel.obrirLlibre(l); onEdit() },
                        onEliminar = { l -> viewModel.eliminarLlibre(l) }
                    )
                }
            }
        }
    }
}