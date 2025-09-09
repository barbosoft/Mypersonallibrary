// LlibreViewModel.kt
package org.biblioteca.mypersonallibrary.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.biblioteca.mypersonallibrary.data.Llibre
import org.biblioteca.mypersonallibrary.data.LlibreRepository
import retrofit2.HttpException
import java.io.IOException

open class LlibreViewModel : ViewModel() {

    private val repository = LlibreRepository()

    private val _llibre = MutableStateFlow<Llibre?>(null)
    val llibre: StateFlow<Llibre?> get() = _llibre

    private val _missatge = MutableStateFlow<String?>(null)
    val missatge: StateFlow<String?> = _missatge

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _totsElsLlibres = MutableStateFlow<List<Llibre>>(emptyList())
    open val totsElsLlibres: StateFlow<List<Llibre>> get() = _totsElsLlibres

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

    init { carregaTots() }

    open fun carregaTots(showLoader: Boolean = true) {
        viewModelScope.launch {
            if (showLoader) _loading.value = true
            try {
                _totsElsLlibres.value = repository.getTotsElsLlibres()
            } finally {
                if (showLoader) _loading.value = false
            }
        }
    }
    fun desarEdicio(onSuccess: () -> Unit) {
        // 1) Validacions bàsiques
        val actual = _llibre.value ?: run {
            _missatge.value = "Cap llibre per desar"
            return
        }
        val id = actual.id ?: run {
            _missatge.value = "Aquest llibre no té ID (no es pot actualitzar)"
            return
        }

        // 2) Crida de xarxa
        viewModelScope.launch {
            _loading.value = true
            repository.updateLlibre(actual).fold(
                onSuccess = {
                    _missatge.value = "Canvis desats"
                    // refresca llista però sense ensenyar el loader general
                    carregaTots(showLoader = false)
                    onSuccess()
                },
                onFailure = { e ->
                    _missatge.value = when (e) {
                        is java.io.IOException -> "Sense connexió. No s’han pogut desar els canvis."
                        is retrofit2.HttpException -> "Error del servidor (${e.code()})."
                        else -> "No s’han pogut desar els canvis."
                    }
                }
            )
            _loading.value = false
        }
    }
    /** Neteja l'estat d'edició/creació després de desar o cancel·lar. */
    fun netejarEdicio() {
        // Si vols deixar el formulari buit, posa Llibre() en lloc de null
        _llibre.value = Llibre()
        // (Opcional) si tens un estat separat per a escaneig/edició, neteja'l també:
        // _llibreEnEdicio.value = null
    }


    /** Helper per fusionar dos llibres: es respecten els camps ja informats a 'a' */
    private fun mergePreferA(a: Llibre?, b: Llibre?): Llibre? {
        if (a == null) return b
        if (b == null) return a
        return a.copy(
            // identificador es queda el d''a' (si és nou, serà null)
            titol          = a.titol          ?: b.titol,
            autor          = a.autor          ?: b.autor,
            editorial      = a.editorial      ?: b.editorial,
            edicio         = a.edicio         ?: b.edicio,
            isbn           = a.isbn           ?: b.isbn,
            sinopsis       = a.sinopsis       ?: b.sinopsis,
            pagines        = a.pagines?.takeIf { it > 0 } ?: b.pagines,
            imatgeUrl      = a.imatgeUrl      ?: b.imatgeUrl,
            anyPublicacio  = a.anyPublicacio  ?: b.anyPublicacio,
            idioma         = a.idioma         ?: b.idioma,
            categoria      = a.categoria      ?: b.categoria,
            ubicacio       = a.ubicacio       ?: b.ubicacio,
            llegit         = a.llegit         ?: b.llegit,
            comentari      = a.comentari      ?: b.comentari,
            puntuacio      = a.puntuacio      ?: b.puntuacio
        )
    }

    /** Cerca manual per ISBN al formulari. Fusiona *tots* els camps retornats. */
    fun buscarPerIsbn(isbn: String) {
        viewModelScope.launch {
            _loading.value = true
            try {
                repository.fetchLlibreByIsbn(isbn).fold(
                    onSuccess = { fetched ->
                        if (fetched == null) {
                            _missatge.value = "No s'ha trobat cap llibre amb aquest ISBN"
                        } else {
                            // Ens quedem amb el que ja tingui l’usuari + el que arribi del servei
                            _llibre.value = mergePreferA(
                                (_llibre.value ?: Llibre(isbn = isbn)),
                                fetched
                            )
                        }
                    },
                    onFailure = { e ->
                        _missatge.value = when (e) {
                            is IOException -> "Sense connexió. Revisa la xarxa."
                            is HttpException -> "Error del servidor (${e.code()})."
                            else -> "Error desconegut en cercar per ISBN."
                        }
                    }
                )
            } finally {
                _loading.value = false
            }
        }
    }

    /** Flux d’escaneig: crea l’objecte amb l’ISBN i enriqueix TOTS els camps. */
    fun prepararNouLlibreAmbIsbn(isbn: String) {
        // El formulari observa 'llibre', per tant inicialitza directament aquest estat
        _llibre.value = Llibre(isbn = isbn)
    }

    fun enriquirLlibrePerIsbn() {
        val isbn = _llibre.value?.isbn ?: return
        viewModelScope.launch {
            _loading.value = true
            try {
                repository.fetchLlibreByIsbn(isbn).fold(
                    onSuccess = { fetched ->
                        if (fetched != null) {
                            _llibre.value = mergePreferA(_llibre.value, fetched)
                        }
                    },
                    onFailure = { /* opcional: set missatge */ }
                )
            } finally {
                _loading.value = false
            }
        }
    }

    fun actualitzarCamp(modificar: (Llibre?) -> Llibre) {
        _llibre.value = modificar(_llibre.value)
    }

    fun guardarLlibre(llibre: Llibre, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _loading.value = true
            repository.createLlibre(llibre).fold(
                onSuccess = {
                    _missatge.value = "Llibre desat correctament"
                    // refresca (sense spinner a pantalla)
                    carregaTots(showLoader = false)
                    onSuccess()
                },
                onFailure = { e ->
                    _missatge.value = when (e) {
                        is IllegalStateException -> e.message
                        is IOException -> "Sense connexió. No s'ha pogut desar."
                        is HttpException -> "Error del servidor en desar (${e.code()})."
                        else -> "No s'ha pogut desar el llibre."
                    }
                }
            )
            _loading.value = false
        }
    }

    open fun eliminarLlibre(l: Llibre) = viewModelScope.launch {
        val id = l.id ?: run {
            _missatge.value = "Aquest llibre no té ID (no es pot eliminar)"
            return@launch
        }
        _loading.value = true
        repository.esborrarLlibre(id).fold(
            onSuccess = {
                _missatge.value = "Llibre eliminat"
                carregaTots(showLoader = false)
            },
            onFailure = { e ->
                _missatge.value = when (e) {
                    is IOException -> "Sense connexió. No s'ha pogut eliminar."
                    is HttpException -> "Error del servidor en eliminar (${e.code()})."
                    else -> "No s'ha pogut eliminar el llibre."
                }
            }
        )
        _loading.value = false
    }

    open fun obrirLlibre(llibre: Llibre) {
        _llibre.value = llibre
    }
}
