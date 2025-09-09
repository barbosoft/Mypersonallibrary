package org.biblioteca.mypersonallibrary.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import org.biblioteca.mypersonallibrary.data.Llibre
import org.biblioteca.mypersonallibrary.viewModel.LlibreViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LlibreEditScreen(
    viewModel: LlibreViewModel,
    onDone: () -> Unit,
    onCancel: () -> Unit
) {
    val llibre by viewModel.llibre.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val missatge by viewModel.missatge.collectAsState()

    // Acabem “nav busy” en entrar i en sortir
    LaunchedEffect(Unit) { viewModel.endNav() }
    DisposableEffect(Unit) { onDispose { viewModel.endNav() } }

    val snackbar = remember { SnackbarHostState() }
    LaunchedEffect(missatge) {
        missatge?.let { snackbar.showSnackbar(it); viewModel.netejarMissatge() }
    }

    // Si no hi ha llibre (o sense ID) oferim tornar
    if (llibre?.id == null) {
        Scaffold(topBar = { CenterAlignedTopAppBar({ Text("Editar llibre") }) }) { padding ->
            Column(Modifier.padding(padding).padding(16.dp)) {
                Text("Cap llibre carregat per editar.")
                Spacer(Modifier.height(8.dp))
                Button(onClick = { viewModel.endNav(); onCancel() }) { Text("Tornar") }
            }
        }
        return
    }

    // Photo Picker (Android 13+ sense permís)
    val pickImage = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let { viewModel.actualitzarCamp { (it ?: Llibre()).copy(imatgeUrl = uri.toString()) } }
    }

    // Diàleg confirmació eliminar
    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = { CenterAlignedTopAppBar(title = { Text("Editar llibre") }) },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .imePadding()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = { viewModel.endNav(); onCancel() },
                    enabled = !loading
                ) { Text("Cancel·lar") }

                Button(
                    onClick = {
                        // mentre desa i torna enrere, permetre mostrar overlay de nav si cal
                        viewModel.endNav()
                        viewModel.desarEdicio(onDone)
                    },
                    enabled = !loading,
                    modifier = Modifier.weight(1f)
                ) { Text("Desar canvis") }

                // Botó eliminar (destructiu)
                Button(
                    onClick = { showDeleteDialog = true },
                    enabled = !loading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) { Text("Eliminar") }
            }
        }
    ) { innerPadding ->

        // Llista scrollable per al formulari
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top))
                .imePadding(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // --- Portada amb fallback / loading / error ---
            item {
                PortadaLlibre(
                    imageUrl = llibre?.imatgeUrl,
                    contentDescription = "Caràtula",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 160.dp, max = 260.dp)
                )
            }

            // Botons imatge
            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = {
                            pickImage.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        enabled = !loading
                    ) { Text("Selecciona imatge") }

                    OutlinedButton(
                        onClick = { viewModel.actualitzarCamp { (it ?: Llibre()).copy(imatgeUrl = null) } },
                        enabled = !loading && !llibre?.imatgeUrl.isNullOrBlank()
                    ) { Text("Treure imatge") }
                }
            }

            // Títol
            item {
                var titol by rememberSaveable(llibre?.id) { mutableStateOf(llibre?.titol.orEmpty()) }
                LaunchedEffect(llibre?.titol) { titol = llibre?.titol.orEmpty() }

                OutlinedTextField(
                    value = titol,
                    onValueChange = { v -> viewModel.actualitzarCamp { (it ?: Llibre()).copy(titol = v) } },
                    label = { Text("Títol") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Autor
            item {
                OutlinedTextField(
                    value = llibre?.autor.orEmpty(),
                    onValueChange = { v -> viewModel.actualitzarCamp { (it ?: Llibre()).copy(autor = v) } },
                    label = { Text("Autor") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // ISBN (read-only en edició)
            item {
                OutlinedTextField(
                    value = llibre?.isbn.orEmpty(),
                    onValueChange = { /* noop */ },
                    readOnly = true,
                    label = { Text("ISBN") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Editorial
            item {
                OutlinedTextField(
                    value = llibre?.editorial.orEmpty(),
                    onValueChange = { v -> viewModel.actualitzarCamp { (it ?: Llibre()).copy(editorial = v) } },
                    label = { Text("Editorial") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Edició
            item {
                OutlinedTextField(
                    value = llibre?.edicio.orEmpty(),
                    onValueChange = { v -> viewModel.actualitzarCamp { (it ?: Llibre()).copy(edicio = v) } },
                    label = { Text("Edició") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Pàgines (numèric)
            item {
                var paginesText by rememberSaveable(llibre?.id) {
                    mutableStateOf(llibre?.pagines?.toString().orEmpty())
                }
                LaunchedEffect(llibre?.pagines) {
                    paginesText = llibre?.pagines?.toString().orEmpty()
                }

                OutlinedTextField(
                    value = paginesText,
                    onValueChange = { new ->
                        val digitsOnly = new.filter { it.isDigit() }
                        paginesText = digitsOnly
                        viewModel.actualitzarCamp { cur ->
                            (cur ?: Llibre()).copy(pagines = digitsOnly.toIntOrNull())
                        }
                    },
                    label = { Text("Pàgines") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Idioma
            item {
                OutlinedTextField(
                    value = llibre?.idioma.orEmpty(),
                    onValueChange = { v -> viewModel.actualitzarCamp { (it ?: Llibre()).copy(idioma = v) } },
                    label = { Text("Idioma") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Sinopsis
            item {
                OutlinedTextField(
                    value = llibre?.sinopsis.orEmpty(),
                    onValueChange = { v -> viewModel.actualitzarCamp { (it ?: Llibre()).copy(sinopsis = v) } },
                    label = { Text("Sinopsis") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Llegit + comentari
            item {
                val llegit = llibre?.llegit == true
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = llegit,
                        onCheckedChange = { chk ->
                            viewModel.actualitzarCamp { (it ?: Llibre()).copy(llegit = chk) }
                        },
                        modifier = Modifier.semantics { contentDescription = "readCheckbox" }
                    )
                    Text("Ja l’he llegit")
                }
            }

            if (llibre?.llegit == true) {
                item {
                    OutlinedTextField(
                        value = llibre?.comentari.orEmpty(),
                        onValueChange = { v ->
                            viewModel.actualitzarCamp { (it ?: Llibre()).copy(comentari = v) }
                        },
                        label = { Text("Comentari") },
                        minLines = 3,
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics { contentDescription = "commentField" }
                    )
                }
            }

            // Valoració (0..5)
            item {
                val rating = rememberSaveable(llibre?.id) { mutableStateOf(llibre?.puntuacio ?: 0) }
                LaunchedEffect(llibre?.puntuacio) { rating.value = llibre?.puntuacio ?: 0 }

                Text("Valoració")
                RatingBar(
                    rating = rating.value,
                    onRatingChange = { new ->
                        rating.value = new
                        viewModel.actualitzarCamp { (it ?: Llibre()).copy(puntuacio = new) }
                    },
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            item { Spacer(Modifier.height(8.dp)) }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Eliminar llibre") },
            text = { Text("Segur que vols eliminar aquest llibre?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        llibre?.let { viewModel.eliminarLlibre(it) }
                        onCancel()
                    }
                ) { Text("Eliminar") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel·lar") }
            }
        )
    }
}

@Composable
private fun RatingBar(
    rating: Int,
    onRatingChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val filledColor = MaterialTheme.colorScheme.primary
    val emptyColor = MaterialTheme.colorScheme.onSurfaceVariant

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        (1..5).forEach { i ->
            val filled = i <= rating
            val scale by animateFloatAsState(targetValue = if (filled) 1.15f else 1f, label = "")
            Icon(
                imageVector = if (filled) Icons.Filled.Star else Icons.Outlined.Star,
                contentDescription = "$i estrelles",
                tint = if (filled) filledColor else emptyColor,
                modifier = Modifier
                    .graphicsLayer(scaleX = scale, scaleY = scale)
                    .clickable { onRatingChange(i) }
                    .semantics { contentDescription = "star-$i-${if (filled) "filled" else "empty"}" }
            )
        }
        Spacer(Modifier.width(8.dp))
        Text("$rating/5", style = MaterialTheme.typography.bodyMedium)
    }
}
