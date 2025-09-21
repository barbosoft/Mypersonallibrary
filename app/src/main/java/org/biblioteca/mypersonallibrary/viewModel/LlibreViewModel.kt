package org.biblioteca.mypersonallibrary.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.biblioteca.mypersonallibrary.data.Llibre
import org.biblioteca.mypersonallibrary.data.LlibreRepository
import retrofit2.HttpException
import java.io.IOException

class LlibreViewModel(
    private val repository: LlibreRepository
) : ViewModel() {

    val llibres = repository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList()
    )
    fun delete(l: Llibre) = viewModelScope.launch {
        l.id?.let { repository.delete(it) }
    }
    init {
        viewModelScope.launch { repository.refreshAll() } // <-- omple Room des de l'API
    }

    private val _llibre = MutableStateFlow<Llibre?>(null)
    val llibre: StateFlow<Llibre?> get() = _llibre

    private val _missatge = MutableStateFlow<String?>(null)
    val missatge: StateFlow<String?> = _missatge

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _snackbar = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val snackbar = _snackbar // .asSharedFlow() si vols exposar-lo només lectura

    fun toast(message: String) = viewModelScope.launch {
        _snackbar.emit(message)
    }

    // Si el repo exposa Flow des de Room, fem-ho reactiu:
    val totsElsLlibres: StateFlow<List<Llibre>> =
        repository.observeAll()                       // Flow<List<Llibre>>
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val libraryIsbns = repository.observeLibraryIsbns()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptySet())

    private val _navegant = MutableStateFlow(false)
    val navegant: StateFlow<Boolean> = _navegant
    fun startNav() { _navegant.value = true }
    fun endNav()   { _navegant.value = false }

    val busy: StateFlow<Boolean> =
        combine(loading, navegant) { l, n -> l || n }
            .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    fun netejarMissatge() { _missatge.value = null }

    fun netejarForm() {
        _llibre.value = Llibre()
        _missatge.value = "Formulari netejat"
    }

    /** Si prefereixes un ‘pull’ explícit al backend, mantén aquesta funció */
    fun carregaTots(showLoader: Boolean = true) {
        viewModelScope.launch {
            if (showLoader) _loading.value = true
            try {
                // Si tens un ‘getTotsElsLlibres()’ que pega al backend:
                // _totsElsLlibres.value = repository.getTotsElsLlibres()
                // (no cal si uses observeAll())
                repository.refreshAll()
            } finally {
                if (showLoader) _loading.value = false
            }
        }
    }
/*
    // LlibreViewModel.kt
    fun refreshAll() = viewModelScope.launch {
        try {
            repository.refreshAll()   // fa el pull del backend i fa replace a Room
        } catch (_: Exception) { /* opcional: log */ }
    }

 */

/*
    fun desarEdicio(onSuccess: () -> Unit) {
        val actual = _llibre.value ?: run {
            _missatge.value = "Cap llibre per desar"; return
        }
        val id = actual.id ?: run {
            _missatge.value = "Aquest llibre no té ID (no es pot actualitzar)"; return
        }

        viewModelScope.launch {
            _loading.value = true
            repository.updateLlibre(actual).fold(
                onSuccess = {
                    _missatge.value = "Canvis desats"
                    onSuccess()
                },
                onFailure = { e ->
                    _missatge.value = when (e) {
                        is IOException   -> "Sense connexió. No s’han pogut desar els canvis."
                        is HttpException -> "Error del servidor (${e.code()})."
                        else             -> "No s’han pogut desar els canvis."
                    }
                }
            )
            _loading.value = false
        }
    }

 */
fun desarEdicio(onSuccess: () -> Unit) {
    val actual = _llibre.value ?: run {
        _missatge.value = "Cap llibre per desar"; return
    }
    val id = actual.id ?: run {
        _missatge.value = "Aquest llibre no té ID (no es pot actualitzar)"; return
    }

    viewModelScope.launch {
        _loading.value = true
        val now = System.currentTimeMillis()

        // 1) Escriu a Room de seguida (UI instantània)
        repository.upsertLocal(actual.copy(updatedAt = now))

        // 2) Prova backend; si va bé, normalitza; si falla, només avisem
        repository.updateLlibre(actual).fold(
            onSuccess = { server ->
                // opcional: normalitzar amb el que torni el servidor
                repository.upsertLocal(server.copy(updatedAt = System.currentTimeMillis()))
                _missatge.value = "Canvis desats"
                onSuccess()
            },
            onFailure = { e ->
                _missatge.value = when (e) {
                    is java.io.IOException -> "Sense connexió. Es sincronitzarà més tard."
                    is retrofit2.HttpException -> "Error del servidor (${e.code()})."
                    else -> "No s’han pogut desar els canvis."
                }
            }
        )

        _loading.value = false
    }
}


    fun netejarEdicio() {
        _llibre.value = Llibre()
    }

    private fun mergePreferA(a: Llibre?, b: Llibre?): Llibre? {
        if (a == null) return b
        if (b == null) return a
        return a.copy(
            titol         = a.titol ?: b.titol,
            autor         = a.autor ?: b.autor,
            editorial     = a.editorial ?: b.editorial,
            edicio        = a.edicio ?: b.edicio,
            isbn          = a.isbn ?: b.isbn,
            sinopsis      = a.sinopsis ?: b.sinopsis,
            pagines       = a.pagines?.takeIf { it > 0 } ?: b.pagines,
            imatgeUrl     = a.imatgeUrl ?: b.imatgeUrl,
            anyPublicacio = a.anyPublicacio ?: b.anyPublicacio,
            idioma        = a.idioma ?: b.idioma,
            categoria     = a.categoria ?: b.categoria,
            ubicacio      = a.ubicacio ?: b.ubicacio,
            llegit        = a.llegit ?: b.llegit,
            comentari     = a.comentari ?: b.comentari,
            puntuacio     = a.puntuacio ?: b.puntuacio,
            updatedAt     = maxOf(a.updatedAt, b.updatedAt)
        )
    }

    fun buscarPerIsbn(isbn: String) {
        viewModelScope.launch {
            _loading.value = true
            try {
                repository.fetchLlibreByIsbn(isbn).fold(
                    onSuccess = { fetched ->
                        if (fetched == null) {
                            _missatge.value = "No s'ha trobat cap llibre amb aquest ISBN"
                        } else {
                            _llibre.value = mergePreferA((_llibre.value ?: Llibre(isbn = isbn)), fetched)
                        }
                    },
                    onFailure = { e ->
                        _missatge.value = when (e) {
                            is IOException   -> "Sense connexió. Revisa la xarxa."
                            is HttpException -> "Error del servidor (${e.code()})."
                            else             -> "Error desconegut en cercar per ISBN."
                        }
                    }
                )
            } finally {
                _loading.value = false
            }
        }
    }

    fun prepararNouLlibreAmbIsbn(isbn: String) { _llibre.value = Llibre(isbn = isbn) }

    fun enriquirLlibrePerIsbn() {
        val isbn = _llibre.value?.isbn ?: return
        viewModelScope.launch {
            _loading.value = true
            try {
                repository.fetchLlibreByIsbn(isbn).fold(
                    onSuccess = { fetched -> if (fetched != null) _llibre.value = mergePreferA(_llibre.value, fetched) },
                    onFailure = { /* opcional */ }
                )
            } finally { _loading.value = false }
        }
    }

    fun actualitzarCamp(mod: (Llibre?) -> Llibre) { _llibre.value = mod(_llibre.value) }

    fun guardarLlibre(llibre: Llibre, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            // 1) Escriu local immediatament perquè la UI reaccioni i salti al top (RECENT)
            repository.upsertLocal(llibre.copy(updatedAt = System.currentTimeMillis()))
            onSuccess()

            // 2) Prova backend; si falla, marca pendent però NO llencem el procés
            _loading.value = true
            repository.createLlibre(llibre).fold(
                onSuccess = { server ->
                    repository.upsertLocal(server)   // normalitza amb el "canònic"
                    _missatge.value = "Llibre desat correctament"
                },
                onFailure = { e ->
                    _missatge.value = when (e) {
                        is IllegalStateException -> e.message
                        is IOException           -> "Sense connexió. Es desarà quan hi hagi xarxa."
                        is HttpException         -> "Error del servidor en desar (${e.code()})."
                        else                     -> "No s'ha pogut sincronitzar el llibre."
                    }
                    // IMPORTANT: marcar pendent dins d’una coroutine
                    llibre.id?.let { id ->
                        viewModelScope.launch { repository.markPendingSync(id) }
                    }
                }
            )
            _loading.value = false
        }
    }

    fun eliminarLlibre(l: Llibre) = viewModelScope.launch {
        val id = l.id ?: run {
            _missatge.value = "Aquest llibre no té ID (no es pot eliminar)"; return@launch
        }
        _loading.value = true
        repository.esborrarLlibre(id).fold(
            onSuccess = { _missatge.value = "Llibre eliminat" },
            onFailure = { e ->
                _missatge.value = when (e) {
                    is IOException   -> "Sense connexió. No s'ha pogut eliminar."
                    is HttpException -> "Error del servidor en eliminar (${e.code()})."
                    else             -> "No s'ha pogut eliminar el llibre."
                }
            }
        )
        _loading.value = false
    }

    fun obrirLlibre(llibre: Llibre) { _llibre.value = llibre }



    /** Escriu local immediat: fa que la llista es refresqui sense fer res més. */
    fun guardarLlibreLocal(llibre: Llibre, onSuccess: () -> Unit = {}) =
        viewModelScope.launch {
            repository.upsertLocal(llibre.copy(updatedAt = System.currentTimeMillis()))
            onSuccess()
        }

    /** Elimina local immediat (UI instantània). */
    fun eliminarLlibreLocal(id: Long) = viewModelScope.launch {
        repository.deleteLocal(id)
    }

    /** Pull opcional del backend per alinear cache local; pots cridar-la on vulguis. */
    fun refreshAll() = viewModelScope.launch {
        repository.refreshAll()
    }

}
