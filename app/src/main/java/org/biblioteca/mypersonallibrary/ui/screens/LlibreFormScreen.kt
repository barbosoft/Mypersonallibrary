package org.biblioteca.mypersonallibrary.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.work.WorkManager
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import org.biblioteca.mypersonallibrary.data.Llibre
import org.biblioteca.mypersonallibrary.data.sync.WishlistSyncWorker
import org.biblioteca.mypersonallibrary.viewModel.LlibreViewModel
import org.biblioteca.mypersonallibrary.viewModel.WishlistViewModel   // 🆕

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LlibreFormScreen(
    viewModel: LlibreViewModel,
    wishlistVM: WishlistViewModel,               // 🆕 injectem el VM de wishlist
    onSave: () -> Unit,
    onScanIsbn: () -> Unit
) {
    val llibre by viewModel.llibre.collectAsState()
    val missatge by viewModel.missatge.collectAsState()
    val loading by viewModel.loading.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val focus = LocalFocusManager.current
    val scope = rememberCoroutineScope()

    // Context per al WorkManager
    val appContext = LocalContext.current.applicationContext

    // 👉 Quan arribem a la pantalla, amaguem el loader de navegació
    LaunchedEffect(Unit) { viewModel.endNav() }
    DisposableEffect(Unit) { onDispose { viewModel.endNav() } }

    LaunchedEffect(missatge) {
        missatge?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.netejarMissatge()
        }
    }

    // Control del text d'entrada
    var isbnInput by rememberSaveable { mutableStateOf("") }
    LaunchedEffect(llibre?.isbn) { isbnInput = llibre?.isbn.orEmpty() }

    // 🆕 Estat per a la llista de compra
    var perComprar by rememberSaveable { mutableStateOf(false) }   // si es marca, es desa com a wishlist
    var notes by rememberSaveable { mutableStateOf("") }           // notes opcionals per a la compra

    // Error de longitud (per a feedback mentre s’escriu)
    val normalizedForLen = remember(isbnInput) { normalizeIsbn(isbnInput) }
    val lengthError: String? = remember(normalizedForLen) {
        if (normalizedForLen.isEmpty() || normalizedForLen.length == 10 || normalizedForLen.length == 13) null
        else "L’ISBN ha de tenir 10 o 13 dígits"
    }

    // Acció comuna de cerca amb validació completa (inclou checksum)
    fun performSearch() {
        val normalized = normalizeIsbn(isbnInput)
        val error = validateIsbn(normalized)
        if (error != null) {
            scope.launch { snackbarHostState.showSnackbar(error) }
            return
        }
        focus.clearFocus()
        // Desa l’ISBN validat a l’estat i cerca
        viewModel.actualitzarCamp { act -> (act ?: Llibre()).copy(isbn = normalized) }
        viewModel.buscarPerIsbn(normalized)
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
                    modifier = Modifier.fillMaxWidth().height(220.dp)
                )
                Spacer(Modifier.height(8.dp))
            }

            // ---------- ISBN (teclat numèric + validació + IME Search) ----------
            OutlinedTextField(
                value = isbnInput,
                onValueChange = { nou ->
                    // permet dígits, guions i espais mentre s’escriu
                    val filtrat = nou.filter { it.isDigit() || it == '-' || it == ' ' || it == 'x' || it == 'X' }
                    isbnInput = filtrat
                    viewModel.actualitzarCamp { act -> (act ?: Llibre()).copy(isbn = filtrat) }
                },
                label = { Text("ISBN") },
                placeholder = { Text("978…") },
                singleLine = true,
                isError = lengthError != null,
                supportingText = { lengthError?.let { Text(it) } },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,  // numèric (si cal ‘X’ s’hi pot escriure manualment)
                    imeAction = ImeAction.Search
                ),
                keyboardActions = KeyboardActions(
                    onSearch = { performSearch() }   // ENTER/Done → cercar
                ),
                trailingIcon = {
                    IconButton(
                        onClick = { performSearch() }
                    ) { Icon(Icons.Default.Search, contentDescription = "Cercar per ISBN") }
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            // ---------- Camps ----------
            OutlinedTextField(
                value = llibre?.titol ?: "",
                onValueChange = { nouTitol ->
                    viewModel.actualitzarCamp { act -> (act ?: Llibre()).copy(titol = nouTitol) }
                },
                label = { Text("Títol") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = llibre?.autor ?: "",
                onValueChange = { nouAutor ->
                    viewModel.actualitzarCamp { act -> (act ?: Llibre()).copy(autor = nouAutor) }
                },
                label = { Text("Autor") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            // 🆕 ------ Bloc "Afegir a la llista de compra" ------
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Checkbox(checked = perComprar, onCheckedChange = { perComprar = it })
                Text("Afegir a la llista de compra")
            }
            if (perComprar) {
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (opcional)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
            }
            // 🆕 ------ Fi bloc wishlist ------

            // ---------- Accions ----------
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        // 👉 Navegació a l’escàner: mostra/amaga loader segons convingui
                        viewModel.endNav()
                        onScanIsbn()
                    },
                    enabled = true,
                    modifier = Modifier.weight(1f)
                ) { Text("Escanejar ISBN") }

                // Desar (habilitat si tenim ISBN)
                val potDesar = !loading && !(llibre?.isbn.isNullOrBlank())
                Button(
                    onClick = {
                        viewModel.endNav()
                        val actual = viewModel.llibre.value
                            ?: Llibre(isbn = normalizeIsbn(isbnInput)) // salvaguarda


                        if (perComprar) {
                            // 🆕 Desa a la wishlist i torna
                            wishlistVM.addFromCurrentBook(actual, notes)

                            // 🔄 Sync immediat cap al backend
                            WorkManager.getInstance(appContext)
                                .enqueue(WishlistSyncWorker.oneOff())
                            onSave()
                        } else {
                            // Desa com a llibre normal
                            viewModel.guardarLlibre(actual) {
                                viewModel.netejarEdicio()
                                onSave()
                            }
                        }
                    },
                    enabled = potDesar,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (perComprar) "Desar a compra" else "Desar llibre")   // 🆕 etiqueta dinàmica
                }

                Button(
                    onClick = {
                        viewModel.endNav()
                        viewModel.netejarForm()
                    },
                    enabled = !loading,
                    modifier = Modifier.weight(1f)
                ) { Text("Netejar") }
            }
        }
    }
}

/* ---------- Helpers de validació ---------- */

private fun normalizeIsbn(raw: String): String =
    raw.replace("-", "").replace(" ", "").uppercase()

private fun validateIsbn(isbn: String): String? {
    if (isbn.isBlank()) return "Introdueix un ISBN."
    if (isbn.length != 10 && isbn.length != 13) return "L’ISBN ha de tenir 10 o 13 dígits."
    return when (isbn.length) {
        10 -> if (isValidIsbn10(isbn)) null else "ISBN-10 invàlid (checksum)."
        13 -> if (isValidIsbn13(isbn)) null else "ISBN-13 invàlid (checksum)."
        else -> "ISBN invàlid."
    }
}

private fun isValidIsbn10(isbn10: String): Boolean {
    if (!isbn10.substring(0, 9).all { it.isDigit() }) return false
    if (!(isbn10.last().isDigit() || isbn10.last() == 'X')) return false
    var sum = 0
    for (i in 0 until 9) sum += (10 - i) * (isbn10[i] - '0')
    val last = if (isbn10[9] == 'X') 10 else (isbn10[9] - '0')
    sum += last
    return sum % 11 == 0
}

private fun isValidIsbn13(isbn13: String): Boolean {
    if (!isbn13.all { it.isDigit() }) return false
    var sum = 0
    for (i in 0 until 12) {
        val d = isbn13[i] - '0'
        sum += if (i % 2 == 0) d else d * 3
    }
    val check = (10 - (sum % 10)) % 10
    return check == (isbn13[12] - '0')
}

