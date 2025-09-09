package org.biblioteca.mypersonallibrary.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.biblioteca.mypersonallibrary.data.Llibre
import org.biblioteca.mypersonallibrary.data.WishlistItem
import org.biblioteca.mypersonallibrary.data.WishlistRepositoryHybrid
import kotlin.comparisons.nullsLast // 👈 per compareBy amb CASE_INSENSITIVE_ORDER

// --- Criteri d’ordre de la wishlist
enum class WishlistOrder { BY_TITLE, BY_AUTHOR, BY_RECENT }

class WishlistViewModel(
    // ✅ ara el repo ve per constructor (veuràs la factory a sota)
    private val repo: WishlistRepositoryHybrid
) : ViewModel() {

    // Flux d’items persistits al repositori
    val items: StateFlow<List<WishlistItem>> =
        repo.observeAll().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // Estat UI
    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query

    private val _order = MutableStateFlow(WishlistOrder.BY_TITLE)
    val order: StateFlow<WishlistOrder> = _order

    // Llista visible = items filtrats + ordenats
    val visibleItems: StateFlow<List<WishlistItem>> =
        combine(items, _query, _order) { list, q, ord ->
            val qn = q.trim().lowercase()
            val filtered = if (qn.isEmpty()) list else list.filter { it.matches(qn) }
            when (ord) {
                WishlistOrder.BY_TITLE  ->
                    filtered.sortedWith(compareBy(nullsLast(String.CASE_INSENSITIVE_ORDER)) { it.titol ?: "" })
                WishlistOrder.BY_AUTHOR ->
                    filtered.sortedWith(compareBy(nullsLast(String.CASE_INSENSITIVE_ORDER)) { it.autor ?: "" })
                WishlistOrder.BY_RECENT ->
                    filtered.sortedByDescending { it.createdAt } // ✅ createdAt és Long
            }
        }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // Helpers de filtre
    private fun WishlistItem.matches(q: String): Boolean {
        fun String?.m() = this?.lowercase()?.contains(q) == true
        return titol.m() || autor.m() || isbn.m() || sinopsis.m() || notes.m()
    }

    fun setQuery(q: String) { _query.value = q }
    fun setOrder(o: WishlistOrder) { _order.value = o }

    /** Afegeix (o actualitza) un element a la wishlist */
    fun add(item: WishlistItem) = viewModelScope.launch {
        _loading.value = true
        try {
            repo.addOrUpdate(item)         // ✅ ara existeix
        } finally {
            _loading.value = false
        }
    }

    /** Elimina per id */
    fun remove(id: Long) = viewModelScope.launch {
        _loading.value = true
        try {
            repo.remove(id)
        } finally {
            _loading.value = false
        }
    }

    /** Afegeix a wishlist a partir d’un llibre actual del formulari */
    fun addFromCurrentBook(book: Llibre, notes: String?) = viewModelScope.launch {
        _loading.value = true
        try {
            val item = WishlistItem(
                id = null,                        // es genera a mapper amb nanoTime()
                titol = book.titol,
                autor = book.autor,
                isbn = book.isbn,
                sinopsis = book.sinopsis,
                notes = notes,
                imatgeUrl = book.imatgeUrl,
                idioma = book.idioma,
                pagines = book.pagines,
                editorial = book.editorial,
                edicio = book.edicio,
                anyPublicacio = book.anyPublicacio,
                //preuDesitjat = null,
                //createdAt = System.currentTimeMillis()
            )
            repo.addOrUpdate(item)
        } finally {
            _loading.value = false
        }
    }
/*
    /** Compra: converteix a Llibre i esborra de la wishlist */
    fun purchase(item: WishlistItem, onCreate: (Llibre) -> Unit) = viewModelScope.launch {
        _loading.value = true
        try {
            val llibre = Llibre(
                id = null,
                titol = item.titol,
                autor = item.autor,
                isbn = item.isbn,
                editorial = item.editorial,
                edicio = item.edicio,
                sinopsis = item.sinopsis,
                pagines = item.pagines,
                imatgeUrl = item.imatgeUrl,
                anyPublicacio = item.anyPublicacio,
                idioma = item.idioma,
                categoria = null,
                ubicacio = null,
                llegit = false,
                comentari = null,
                puntuacio = null
            )
            onCreate(llibre)
            item.id?.let { repo.remove(it) }
        } finally {
            _loading.value = false
        }
    }

 */

    fun purchase(item: WishlistItem, onCreate: (Llibre) -> Unit) = viewModelScope.launch {
        _loading.value = true
        try {
            val llibre = repo.purchase(item.id ?: return@launch)  // 👈 compra remot + neteja local
            onCreate(llibre) // la pantalla ja crida el VM de llibres per desar-lo a Room
        } finally {
            _loading.value = false
        }
    }
}
