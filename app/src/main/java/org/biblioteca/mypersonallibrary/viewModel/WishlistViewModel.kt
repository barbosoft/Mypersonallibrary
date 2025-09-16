package org.biblioteca.mypersonallibrary.viewModel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.biblioteca.mypersonallibrary.data.Llibre
import org.biblioteca.mypersonallibrary.data.WishlistItem
import org.biblioteca.mypersonallibrary.data.WishlistRepositoryHybrid
import org.biblioteca.mypersonallibrary.data.sync.WishlistSyncWorker
import retrofit2.HttpException
import java.io.IOException
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

    // Estat de busy (per deshabilitar botons, mostrar loader, etc.)
    private val _busy = MutableStateFlow(false)
    val busy: StateFlow<Boolean> = _busy.asStateFlow()

    private val _missatge = MutableStateFlow<String?>(null)
    val missatge: StateFlow<String?> = _missatge

    private val _snackbar = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val snackbar: SharedFlow<String> = _snackbar
    init {
        // Refresc fort a l'arrencada de la pantalla/VM
        viewModelScope.launch { repo.refreshFromServer() }
    }
    fun toast(message: String) = viewModelScope.launch {
        _snackbar.emit(message)
    }

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

    fun syncAra(context: Context) {
        WorkManager.getInstance(context.applicationContext)
            .enqueue(WishlistSyncWorker.oneOff())
    }

/*
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
                preuDesitjat = null,
                createdAt = System.currentTimeMillis()
            )
            repo.addOrUpdate(item)
        } finally {
            _loading.value = false
        }
    }

 */

    // dins de WishlistViewModel
    fun addFromCurrentBook(
        book: Llibre,
        notes: String,
        libraryIsbns: Set<String> = emptySet(),
        onAdded: (Boolean) -> Unit = {}
    ) = viewModelScope.launch {
        // Normalitza ISBN
        val normalized = (book.isbn ?: "").replace("-", "").replace(" ", "").uppercase()
        if (normalized.isBlank()) {
            _snackbar.emit("Cal un ISBN")
            onAdded(false)
            return@launch
        }

        // 1) Ja és a la WISHLIST? -> NO afegim (evitem duplicat)
        val existsInWishlist = items.value.any { it.isbn?.replace("-", "")?.replace(" ", "")?.uppercase() == normalized }
        if (existsInWishlist) {
            _snackbar.emit("Ja és a la llista de desitjos")
            onAdded(false)
            return@launch
        }

        // 2) Construïm el desig i l’afegim encara que ja sigui a la biblioteca
        val now = System.currentTimeMillis()
        val wish = WishlistItem(
            id = null,
            titol = book.titol,
            autor = book.autor,
            isbn = normalized,
            imatgeUrl = book.imatgeUrl,
            notes = notes.takeIf { it.isNotBlank() },
            idioma = book.idioma,
            pagines = book.pagines,
            anyPublicacio = book.anyPublicacio,
            editorial = book.editorial,
            edicio = book.edicio,
            preuDesitjat = null,
            createdAt = now,
            updatedAt = now
        )

        repo.addOrUpdate(wish)

        // 3) Missatge segons si ja era a la biblioteca
        if (normalized in libraryIsbns) {
            _snackbar.emit("Afegit. Ja és a la biblioteca (es veurà el banner).")
        } else {
            _snackbar.emit("Afegit a la llista de desitjos.")
        }

        onAdded(true)
    }


    // helper (mateix que uses al formulari)
    private fun normalizeIsbn(raw: String) =
        raw.replace("-", "").replace(" ", "").uppercase()




    fun delete(item: WishlistItem) = viewModelScope.launch {
        val id = item.id
        if (id == null || id <= 0) {
            _snackbar.emit("Aquest element encara no està sincronitzat.")
            return@launch
        }

        repo.delete(id)
            .onSuccess {
                _snackbar.emit("Eliminat de la wishlist.")
            }
            .onFailure { e ->
                _snackbar.emit("No s'ha pogut eliminar: ${e.message ?: "error"}")
            }
    }

    fun purchase(
        item: WishlistItem,
        onCreate: (Llibre) -> Unit = {}
    ) = viewModelScope.launch {

        // 1) Necessitem un id vàlid del servidor
        val id = item.id ?: 0L
        val r = repo.purchase(id)
        if (id <= 0L) {
            // Encara no sincronitzat: opcionalment prova d’upsert i demana reintentar
            // runCatching { repo.addOrUpdate(item) }
            // TODO: mostra snackbar/toast: "Element pendent de sincronitzar. Torna-ho a provar."
            _snackbar.emit("Aquest element encara no està sincronitzat. Desa’l primer.")
            return@launch
        }
        _busy.emit(true)
        val result = repo.purchase(id)
        _busy.emit(false)

        result
            //repo.purchase(id)
            r.onSuccess { llibre ->
                _snackbar.emit("Afegit a la biblioteca: ${llibre.titol ?: ""}")
                // opcional: força un pull curt
                launch { repo.sync() }          // refresca wishlist
                /*llibre ->
                // Si vols fer alguna acció addicional (o avisar la LlibreViewModel):
                onCreate(llibre)
                _snackbar.emit("Afegit a la biblioteca: ${llibre.titol ?: ""}")
                // No cal cap refresh manual si la llista principal observa Room.

                 */
            }
            .onFailure { e ->
                when (e) {
                    is WishlistRepositoryHybrid.AlreadyInLibraryException -> _snackbar.emit("Ja tens aquest llibre")
                    is IOException -> _snackbar.emit("Sense connexió")
                    is HttpException -> _snackbar.emit("HTTP ${e.code()}")
                    else -> _snackbar.emit("No s'ha pogut completar la compra")
                }
            }
    }

    private fun Throwable.asNiceMessage(): String = when (this) {
        is retrofit2.HttpException -> "HTTP ${code()}"
        is java.net.UnknownHostException -> "Sense connexió"
        else -> (message ?: "error")
    }

}
