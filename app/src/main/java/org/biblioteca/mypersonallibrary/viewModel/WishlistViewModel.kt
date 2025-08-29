package org.biblioteca.mypersonallibrary.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.biblioteca.mypersonallibrary.data.Llibre

data class WishlistItem(
    val id: Long? = null,
    val titol: String? = null,
    val autor: String? = null,
    val isbn: String? = null,
    val sinopsis: String? = null,
    val notes: String? = null
)

class WishlistViewModel : ViewModel() {

    private val _items = MutableStateFlow<List<WishlistItem>>(emptyList())
    val items: StateFlow<List<WishlistItem>> = _items

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    /** ---- HELPERS ---- */

    /** Genera un ID positiu per a nous elements en memòria. */
    private fun newId(): Long = System.nanoTime() and Long.MAX_VALUE

    /** Neteja cadenes buides/espais a null. */
    private fun String?.clean(): String? =
        this?.trim()?.takeIf { it.isNotEmpty() }

    /** ---- ACCIONS PRINCIPALS ---- */

    /**
     * Afegeix (o actualitza) un element a la wishlist.
     * Si `item.id` és null se li assigna un id nou.
     */
    fun add(item: WishlistItem) = viewModelScope.launch {
        val id = item.id ?: newId()
        _items.value = _items.value
            .filterNot { it.id == id }
            .plus(item.copy(id = id))
    }

    /** Elimina per id. */
    fun remove(id: Long) = viewModelScope.launch {
        _items.value = _items.value.filterNot { it.id == id }
    }

    /**
     * 👉 Des del formulari de “Nou llibre”: construeix un WishlistItem a partir del [Llibre]
     * (i opcionalment [notes]) i l’afegeix a la llista. Si ja n’hi ha un amb el mateix ISBN,
     * es reemplaça (merge senzill).
     */
    fun addFromCurrentBook(book: Llibre, notes: String? = null, onAdded: (() -> Unit)? = null) =
        viewModelScope.launch {
            val item = WishlistItem(
                id = newId(),
                titol = book.titol.clean(),
                autor = book.autor.clean(),
                isbn = book.isbn.clean(),
                sinopsis = book.sinopsis.clean(),
                notes = notes.clean()
            )

            val isbn = item.isbn?.lowercase()
            _items.value = if (isbn != null) {
                // Evita duplicats per ISBN: reemplaça l’existent
                _items.value.filterNot { it.isbn?.lowercase() == isbn } + item
            } else {
                _items.value + item
            }
            onAdded?.invoke()
        }

    /**
     * Marca com “comprat”: converteix WishlistItem -> Llibre i lliura'l al callback `onCreate`,
     * i elimina l’element de la llista.
     */
    fun purchase(item: WishlistItem, onCreate: (Llibre) -> Unit) = viewModelScope.launch {
        _loading.value = true
        try {
            val llibre = Llibre(
                id = null,
                titol = item.titol,
                autor = item.autor,
                isbn = item.isbn,
                editorial = null,
                edicio = null,
                sinopsis = null,
                pagines = null,
                imatgeUrl = null,
                anyPublicacio = null,
                idioma = null,
                categoria = null,
                ubicacio = null,
                llegit = false,
                comentari = null,
                puntuacio = null
            )
            onCreate(llibre)
            item.id?.let { remove(it) }
        } finally {
            _loading.value = false
        }
    }
}
