package org.biblioteca.mypersonallibrary.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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

    fun netejarMissatge() { _missatge.value = null }

    fun netejarForm() {
        _llibre.value = Llibre()        // Així deixem el formulari en blanc
        _missatge.value = "Formulari netejat"
    }

    init {
        carregaTots()
    }

    open fun carregaTots() {
        viewModelScope.launch {
            _totsElsLlibres.value = repository.getTotsElsLlibres()
        }
    }

    fun buscarPerIsbn(isbn: String) {
        viewModelScope.launch {
            _loading.value = true
            repository.fetchLlibreByIsbn(isbn).fold(
                onSuccess = { res ->
                    if (res == null) {
                        _missatge.value = "No s'ha trobat cap llibre amb aquest ISBN"
                    } else {
                        _llibre.value = res
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
            _loading.value = false
        }
    }

    fun guardarLlibre(llibre: Llibre, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _loading.value = true
            repository.createLlibre(llibre).fold(
                onSuccess = {
                    _missatge.value = "Llibre desat correctament"
                    carregaTots() // Refrescar la llista
                    onSuccess()
                },
                onFailure = { e ->
                    _missatge.value = when (e) {
                        is IllegalStateException -> e.message // 409 ISBN duplicat
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

    open fun eliminarLlibre(l: Llibre) = viewModelScope.launch {
        val id = l.id ?: run {
            _missatge.value = "Aquest llibre no té ID (no es pot eliminar)"
            return@launch
        }
        _loading.value = true
        repository.esborrarLlibre(id).fold(
            onSuccess = {
                _missatge.value = "Llibre eliminat"
                carregaTots()                      // 👈 refresca la llista
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