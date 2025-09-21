package org.biblioteca.mypersonallibrary.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.biblioteca.mypersonallibrary.data.local.LlibreDao
import org.biblioteca.mypersonallibrary.data.local.WishlistDao
import org.biblioteca.mypersonallibrary.data.mappers.WishlistMappers.toDomain
import org.biblioteca.mypersonallibrary.data.mappers.WishlistMappers.toDtoForPost
import org.biblioteca.mypersonallibrary.data.mappers.WishlistMappers.toEntity
import org.biblioteca.mypersonallibrary.data.remote.dto.WishDto
import org.biblioteca.mypersonallibrary.data.sync.SyncPrefs
import org.biblioteca.mypersonallibrary.data.remote.dto.toDomain as llibreDtoToDomain

class WishlistRepositoryHybrid(
    private val dao: WishlistDao,
    private val api: BibliotecaApi,
    private val llibreDao: LlibreDao,
    private val prefs: SyncPrefs? = null
) {

    /** Observa la wishlist (Room) mapant a domini. */
    fun observeAll(): Flow<List<WishlistItem>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    /** Pull explícit: baixa del backend i reemplaça Room (neteja residuals). */
    suspend fun refreshFromServer(): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val now = System.currentTimeMillis()
            val remote = api.getWishlist()                 // GET /api/wishlist
            val entities = remote.map { it.toEntity(now) }
            dao.replaceAll(entities)
        }
    }

    /** Crea/actualitza un element. Fa upsert local i prova d’enviar al backend. */
    suspend fun addOrUpdate(item: WishlistItem): Result<WishlistItem> = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()

        // 1) Escriu a Room amb pendent de sincronitzar
        val pending = item.toEntity(now).copy(
            updatedAt = now,
            pendingSync = true,
            deleted = false
        )

        // Guarda (si item.id==null/0, Room crearà un id local)
        val localId = dao.upsert(pending)
        val effectiveLocal = pending.copy(id = pending.id ?: localId)

        // 2) Envia al backend. Si falla, quedarà pendent.
        runCatching {
            val posted: WishDto = api.upsertWishlist(effectiveLocal.toDtoForPost(now))

            // 3) Substitueix el registre local temporal pel que ve del servidor
            val synced = posted.toEntity(now).copy(pendingSync = false, deleted = false)
            dao.deleteById(effectiveLocal.id!!)   // treu l’entrada temporal
            dao.upsert(synced)                    // escriu l’entrada definitiva

            synced.toDomain()
        }
    }

    /** Elimina un element. Esborra localment si el backend respon; si no, marca per a sync. */
    suspend fun delete(id: Long): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            api.deleteWishlist(id)    // DELETE /api/wishlist/{id}
            dao.deleteById(id)        // treu l’element de Room immediatament
        }.onFailure {
            // Offline o error: marca com a eliminat per sincronitzar més tard
            val now = System.currentTimeMillis()
            dao.markDeleted(id, updatedAt = now)
        }
    }

    /** Compat per si tens crides antigues amb 'remove'. */
    @Deprecated("Fes servir delete(id)")
    suspend fun remove(id: Long): Result<Unit> = delete(id)

    /**
     * Compra: crea el llibre al backend, l’afegeix a la taula de llibres (Room)
     * i elimina la wishlist local.
     */
    /*
    suspend fun purchase(id: Long): Result<Llibre> = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()

        runCatching {
            val llibreDto = api.purchaseWishlist(id)   // POST /api/wishlist/purchase/{id}
            // Neteja de wishlist només si la compra ha anat bé
            dao.deleteById(id)
            // Converteix a domini (alias per evitar col·lisions amb WishlistMappers.toDomain)
            llibreDto.llibreDtoToDomain()
        }.onFailure {
            // Si falla, deixa l'item tal qual (opcional: marcar intent fallit, etc.)
            // També podries revertir un markDeleted previ si l'havies fet.
        }
    }

     */
    class AlreadyInLibraryException(val isbn: String)
        : IllegalStateException("Ja és a la biblioteca")

    suspend fun purchase(id: Long): Result<Llibre> = withContext(Dispatchers.IO) {
        val wish = dao.getById(id) ?: return@withContext Result.failure(
            IllegalStateException("Wishlist no trobada")
        )
        val isbn = wish.isbn
        if (!isbn.isNullOrBlank() && llibreDao.existsByIsbn(isbn)) {
            return@withContext Result.failure(AlreadyInLibraryException(isbn))
        }

        runCatching {
            val dto = api.purchaseWishlist(id)
            dao.deleteById(id)          // només si ha anat bé
            dto.llibreDtoToDomain()
        }
    }



    /**
     * Sincronització: puja pendents (upsert/delete) i després fa un pull complet.
     * Es segura d’executar-la sempre que vulguis (ex. arrencada app, tornada d’offline…).
     */
    suspend fun sync() = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val pending = dao.getPendingSync()

        // ---- PUSH ----
        val toUpsert    = pending.filter { !it.deleted }
        val toDeleteIds = pending.filter { it.deleted }.mapNotNull { it.id }

        val syncedIds = mutableListOf<Long>()

        // UPsert en batch: si falla NO toquem flags locals
        if (toUpsert.isNotEmpty()) {
            runCatching {
                val posted = api.upsertWishlistAll(toUpsert.map { it.toDtoForPost(now) })
                val synced = posted.map { it.toEntity(now).copy(pendingSync = false, deleted = false) }
                if (synced.isNotEmpty()) {
                    dao.upsertAll(synced)
                    // Marquem com sincronitzats únicament els que hem intentat pujar
                    syncedIds += toUpsert.mapNotNull { it.id }
                }
            }.onFailure {
                // Offline / error: mantenim pendingSync=true perquè el Worker ho reintenti
            }
        }

        // DELETE en batch: només marquem si la crida té èxit
        if (toDeleteIds.isNotEmpty()) {
            runCatching {
                api.deleteWishlistMany(toDeleteIds)
                syncedIds += toDeleteIds
            }.onFailure {
                // Res: es mantindran com pending i es reintentarà
            }
        }

        if (syncedIds.isNotEmpty()) {
            dao.markSynced(syncedIds)
        }

        // ---- PULL ----
        // Només fem replaceAll si el GET té èxit. Si falla, NO toquem Room.
        try {
            val remote = api.getWishlist()
            val fresh = remote.map { it.toEntity(now).copy(pendingSync = false) }
            dao.replaceAll(fresh)       // assegura consistència amb el servidor
            dao.deleteWithIdZero()      // higiene per si mai quedés algun id=0 residual
        } catch (_: Throwable) {
            // Offline/500: manté la còpia local perquè la UI segueixi mostrant dades
        }
    }

}
