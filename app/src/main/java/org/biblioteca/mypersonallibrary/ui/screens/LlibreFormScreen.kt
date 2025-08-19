package org.biblioteca.mypersonallibrary.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import org.biblioteca.mypersonallibrary.data.Llibre
import org.biblioteca.mypersonallibrary.viewModel.LlibreViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LlibreFormScreen(
    viewModel: LlibreViewModel,
    onSave: () -> Unit,
    onScanIsbn: () -> Unit
) {
    // Estat “clàssic” del formulari (el que ja tenies)
    val llibre by viewModel.llibre.collectAsState()

    // Estat nou: llibre en edició (preparat pel flux d’escàner)
    // (Assegura’t d’haver afegit llibreEnEdicio al ViewModel)
    val llibreEnEdicio by viewModel.llibreEnEdicio.collectAsState()

    val missatge by viewModel.missatge.collectAsState()
    val loading by viewModel.loading.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    // Si arriba un llibre en edició (p.ex. ISBN escanejat), el fem servir com a base del formulari
    LaunchedEffect(llibreEnEdicio) {
        llibreEnEdicio?.let { vmLlibre -> viewModel.obrirLlibre(vmLlibre) }
    }

    LaunchedEffect(missatge) {
        missatge?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.netejarMissatge()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = { CenterAlignedTopAppBar(title = { Text("Nou llibre") }) }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {

            // Caràtula si hi ha URL
            llibre?.imatgeUrl?.takeIf { !it.isNullOrBlank() }?.let { url ->
                AsyncImage(
                    model = url,
                    contentDescription = "Caràtula",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                )
                Spacer(Modifier.height(8.dp))
            }

            // Formulari
            OutlinedTextField(
                value = llibre?.titol ?: "",
                onValueChange = { nouTitol ->
                    viewModel.actualitzarCamp { actual ->
                        actual?.copy(titol = nouTitol) ?: Llibre(titol = nouTitol)
                    }
                },
                label = { Text("Títol") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = llibre?.autor ?: "",
                onValueChange = { nouAutor ->
                    viewModel.actualitzarCamp { actual ->
                        actual?.copy(autor = nouAutor) ?: Llibre(autor = nouAutor)
                    }
                },
                label = { Text("Autor") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))

            // ➕ Nou: camp ISBN (editable)
            OutlinedTextField(
                value = llibre?.isbn ?: "",
                onValueChange = { nouIsbn ->
                    viewModel.actualitzarCamp { actual ->
                        actual?.copy(isbn = nouIsbn) ?: Llibre(isbn = nouIsbn)
                    }
                },
                label = { Text("ISBN") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Obrir escàner d’ISBN
                Button(
                    onClick = onScanIsbn,
                    enabled = !loading,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Escanejar ISBN")
                }

                // Desar llibre (habilitat si tenim ISBN)
                val potDesar = !loading && !(llibre?.isbn.isNullOrBlank())
                Button(
                    onClick = {
                        llibre?.let {
                            viewModel.guardarLlibre(it) {
                                // Neteja estat temporal i tanca
                                viewModel.netejarEdicio()
                                onSave()
                            }
                        }
                    },
                    enabled = potDesar,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Desar llibre")
                }

                // Netejar formulari (i estat d’edició si n’hi ha)
                Button(
                    onClick = {
                        viewModel.netejarForm()
                        viewModel.netejarEdicio()
                    },
                    enabled = !loading,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Netejar")
                }
            }
        }
    }
}
