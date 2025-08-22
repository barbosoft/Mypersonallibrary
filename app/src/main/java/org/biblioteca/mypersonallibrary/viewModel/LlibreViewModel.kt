package org.biblioteca.mypersonallibrary.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.biblioteca.mypersonallibrary.data.Llibre
import org.biblioteca.mypersonallibrary.data.LlibreRepository
import retrofit2.HttpException
import java.io.IOException

open class LlibreViewModel : ViewModel() {

    private val repository = LlibreRepository()

    // Estat del formulari (llibre actual o en creació)
    private val _llibre = MutableStateFlow<Llibre?>(null)
    val llibre: StateFlow<Llibre?> get() = _llibre

    // Missatges i càrrega
    private val _missatge = MutableStateFlow<String?>(null)
    val missatge: StateFlow<String?> = _missatge

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    // Llista de llibres
    private val _totsElsLlibres = MutableStateFlow<List<Llibre>>(emptyList())
    open val totsElsLlibres: StateFlow<List<Llibre>> get() = _totsElsLlibres

    // ---------- Busy overlay (xarxa o navegació) ----------
    private val _navegant = MutableStateFlow(false)
    val navegant: StateFlow<Boolean> = _navegant
    fun startNav() = run { _navegant.value = true }
    fun endNav()   = run { _navegant.value = false }

    val busy: StateFlow<Boolean> =
        combine(loading, navegant) { l, n -> l || n }
            .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    fun netejarMissatge() { _missatge.value = null }

    fun netejarForm() {
        _llibre.value = Llibre()              // deixa el formulari en blanc
        _missatge.value = "Formulari netejat"
    }

    init {
        carregaTots()
    }

    open fun carregaTots() {
        viewModelScope.launch {
            // Assumim que el repositori té getTotsElsLlibres()
            _totsElsLlibres.value = repository.getTotsElsLlibres()
        }
    }

    fun buscarPerIsbn(isbn: String) {
        viewModelScope.launch {
            _loading.value = true
            repository.fetchLlibreByIsbn(isbn).fold(
                onSuccess = { res ->
                    if (res == null) _missatge.value = "No s'ha trobat cap llibre amb aquest ISBN"
                    else _llibre.value = res
                },
                onFailure = { e ->
                    _missatge.value = when (e) {
                        is IOException -> "Sense connexió. Revisa la xarxa."
                        is HttpException -> "Error del servidor (${e.code()})."
                        else -> "Error desconegut en cercar per ISBN."
                    }
                }
            )
            _loading.value = false
        }
    }

    fun guardarLlibre(llibre: Llibre, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _loading.value = true
            repository.createLlibre(llibre).fold(
                onSuccess = {
                    _missatge.value = "Llibre desat correctament"
                    carregaTots()          // refresca la llista
                    onSuccess()
                },
                onFailure = { e ->
                    _missatge.value = when (e) {
                        is IllegalStateException -> e.message               // p.ex. 409 ISBN duplicat
                        is IOException -> "Sense connexió. No s'ha pogut desar."
                        is HttpException -> "Error del servidor en desar (${e.code()})."
                        else -> "No s'ha pogut desar el llibre."
                    }
                }
            )
            _loading.value = false
        }
    }

    fun actualitzarCamp(modificar: (Llibre?) -> Llibre) {
        _llibre.value = modificar(_llibre.value)
    }

    // Retorna Job perquè pugui ser sobreescrit als tests
    open fun eliminarLlibre(l: Llibre) = viewModelScope.launch {
        val id = l.id ?: run {
            _missatge.value = "Aquest llibre no té ID (no es pot eliminar)"
            return@launch
        }
        _loading.value = true
        repository.esborrarLlibre(id).fold(
            onSuccess = {
                _missatge.value = "Llibre eliminat"
                carregaTots()
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

    // ---------- Estat per al flux d’escàner/edició ----------
    private val _llibreEnEdicio = MutableStateFlow<Llibre?>(null)
    val llibreEnEdicio: StateFlow<Llibre?> = _llibreEnEdicio

    /** Prepara un nou llibre a partir d’un ISBN escanejat. */
    fun prepararNouLlibreAmbIsbn(isbn: String) {
        _llibreEnEdicio.value = Llibre(
            id = null, titol = null, autor = null, isbn = isbn, imatgeUrl = null
        )
    }

    /** (Opcional) Enriquir camps titol/autor/imatge usant la crida existent del repositori. */
    fun enriquirLlibrePerIsbn() {
        val actual = _llibreEnEdicio.value ?: return
        val isbn = actual.isbn ?: return
        viewModelScope.launch {
            repository.fetchLlibreByIsbn(isbn).fold(
                onSuccess = { enrich ->
                    if (enrich != null) {
                        _llibreEnEdicio.value = actual.copy(
                            titol = enrich.titol ?: actual.titol,
                            autor = enrich.autor ?: actual.autor,
                            imatgeUrl = enrich.imatgeUrl ?: actual.imatgeUrl
                        )
                    }
                },
                onFailure = {
                    // Si falla, simplement no canviem res; opcionalment pots fer:
                    // _missatge.value = "No s'ha pogut enriquir dades per ISBN"
                }
            )
        }
    }

    fun netejarEdicio() { _llibreEnEdicio.value = null }

    fun desarEdicio(onSuccess: () -> Unit) {
        val actual = llibre.value ?: run {
            _missatge.value = "Cap llibre per desar"
            return
        }
        val id = actual.id ?: run {
            _missatge.value = "Aquest llibre no té ID (no es pot actualitzar)"
            return
        }

        viewModelScope.launch {
            _loading.value = true
            repository.updateLlibre(actual).fold(
                onSuccess = {
                    _missatge.value = "Canvis desats"
                    carregaTots()
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

}
