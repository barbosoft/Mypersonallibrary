package org.biblioteca.mypersonallibrary.data

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.biblioteca.mypersonallibrary.data.RetrofitInstance.api
import org.biblioteca.mypersonallibrary.data.local.WishlistDao
import org.biblioteca.mypersonallibrary.data.local.WishlistEntity
import org.biblioteca.mypersonallibrary.data.mappers.WishlistMappers.toDomain
import org.biblioteca.mypersonallibrary.data.mappers.WishlistMappers.toDto
import org.biblioteca.mypersonallibrary.data.mappers.WishlistMappers.toDtoForPost
import org.biblioteca.mypersonallibrary.data.mappers.WishlistMappers.toEntity
import org.biblioteca.mypersonallibrary.data.remote.WishlistApi
import org.biblioteca.mypersonallibrary.data.sync.SyncPrefs

/**
 * Repositori híbrid:
 *  - Guarda sempre a Room (offline-first)
 *  - Exposa un Flow dels items (observeAll)
 *  - Marca pendents de sync quan fem add/update/remove
 *  - La funció sync() empeny els pendents i després estira del servidor
 */
class WishlistRepositoryHybrid(
    private val dao: WishlistDao,
    private val api: WishlistApi,
    private val prefs: SyncPrefs? = null
) {
    /*
    /** Flux amb tots els elements, mapejats a domini. */
    fun observeAll(): Flow<List<WishlistItem>> =
        dao.observeAll().map { list: List<WishlistEntity> ->
            list.map { it.toDomain() }
        }

 */
    fun observeAll(): Flow<List<WishlistItem>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    /** Afegeix o actualitza un item a Room i el marca com pendent de sync. */
    suspend fun addOrUpdate(item: WishlistItem) = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val pending = item.toEntity(now).copy(
            updatedAt = now,
            pendingSync = true,
            deleted = false
        )
        // 2) Desa a Room i obté l’ID real (si era 0L)
        val localId = dao.upsert(pending)               // <- ha de retornar Long
        val effectiveLocal = pending.copy(id = if (pending.id == 0L) localId else pending.id)

        try {
            // 3) Pujar al backend (no forcis id=0; si no n’hi ha, envia null)
            val posted =
                api.upsert(effectiveLocal.toDtoForPost(now))  // <- API retorna el DTO guardat

            // 4) Reemplaça localment pel que torna el servidor i marca sincronitzat
            val synced = posted.toEntity(now).copy(pendingSync = false, deleted = false)

            // Pot canviar l’ID => fem replace segur
            dao.deleteById(effectiveLocal.id)
            dao.upsert(synced)

        } catch (t: Throwable) {
            // Xarxa KO: deixem pendingSync=true perquè el Worker ho pugi després
            android.util.Log.w("WISHLIST", "No s'ha pogut pujar al servidor; quedarà pendent", t)
        }
    }




        /*
        dao.upsert(entity)

        try {
            //val posted = api.upsert(entity.toDtoForPost(now)) // <- resposta del servidor
            // Reemplaça el registre local amb el que torna el backend i marca com sincronitzat
            //dao.replaceAll(listOf(posted.toEntity().copy(pendingSync = false, deleted = false)))


            api.upsert(item.copy(id = entity.id.takeIf { it != 0L } ?: item.id).toDto(now))
           // dao.markSynced(listOfNotNull(entity.id.takeIf { it != 0L }))
            dao.markSynced(listOf(entity.id))


        } catch (t: Throwable) {
            Log.w("WISHLIST", "No s'ha pogut pujar al servidor; quedarà pendent", t)
            // pendingSync es manté a true per al Worker

         */
        //}


        /*
        runCatching {
            api.upsert(entity.toDtoForPost(now))
            dao.markSynced(listOfNotNull(entity.id.takeIf { it != 0L }))
        }

         */
    //}

    /** Soft-delete + marca com pendent de sync. */
    suspend fun remove(id: Long) = withContext(Dispatchers.IO) {
        dao.markDeleted(id, updatedAt = System.currentTimeMillis())
        //dao.markDeleted(id, now)

        try {
            api.delete(id)
            // ara podem fer delete real localment
            dao.deleteById(id)
        } catch (t: Throwable) {
            Log.w("WISHLIST", "Delete remot fallit; es netejarà al proper sync", t)
            // Es manté com deleted + pendingSync=true
        }



    }

    suspend fun purchase(id: Long): org.biblioteca.mypersonallibrary.data.Llibre =
        withContext(Dispatchers.IO) {
            // marca soft-delete local (per si cau la xarxa després)
            dao.markDeleted(id, updatedAt = System.currentTimeMillis())


            try {
                val llibreDto = api.purchase(id)
                // neteja local definitiva
                dao.deleteById(id)
                // retorna el llibre creat al servidor (el VM el desarà a Room llibres)
                    //llibreDtoToDomain(llibreDto)
                return@withContext llibreDto.toDomain()
            } catch (t: Throwable) {
                Log.w("WISHLIST", "Purchase remot fallit; ho farà el Worker quan hi hagi xarxa", t)
                // Fallback OFFLINE: convertim el que tinguem local a Llibre (mínim ISBN/Títol)
                val local = dao.getByIdNow(id) // crea aquest DAO si no el tens: SELECT * FROM wishlist WHERE id=:id
                // (mínim) crea un Llibre de fallback
                org.biblioteca.mypersonallibrary.data.Llibre(
                    id = null,
                    titol = local?.titol,
                    autor = local?.autor,
                    isbn = local?.isbn,
                    editorial = local?.editorial,
                    edicio = local?.edicio,
                    sinopsis = local?.sinopsis,
                    pagines = local?.pagines,
                    imatgeUrl = local?.imatgeUrl,
                    anyPublicacio = local?.anyPublicacio,
                    idioma = local?.idioma,
                    categoria = null,
                    ubicacio = null,
                    llegit = false,
                    comentari = null,
                    puntuacio = null
                )
                return@withContext Llibre()
            }
        }




    /**
     * Sincronitza:
     * 1) PUSH: envia al backend els canvis pendents (upserts i deletes)
     * 2) PULL: descarrega l’estat del servidor i fa replace local
     */
    suspend fun sync() = withContext(Dispatchers.IO) {
        // --- PUSH ---
        val pending = dao.getPendingSync()
        if (pending.isNotEmpty()) {
            val toDeleteIds = pending.filter { it.deleted }.mapNotNull { it.id }
            val toUpsert   = pending.filter { !it.deleted }

            if (toUpsert.isNotEmpty()) {
                api.upsertAll(toUpsert.map { it.toDtoForPost() })
            }
            if (toDeleteIds.isNotEmpty()) {
                api.deleteMany(toDeleteIds)
            }

            dao.markSynced(pending.mapNotNull { it.id })
        }

        // --- PULL ---
        val remote   = api.getAll()
        val newLocal = remote.map { it.toEntity().copy(pendingSync = false, deleted = false) }
        dao.replaceAll(newLocal)

        prefs?.setLastSync(System.currentTimeMillis())
    }
}
