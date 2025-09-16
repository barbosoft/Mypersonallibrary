package org.biblioteca.mypersonallibrary.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.biblioteca.mypersonallibrary.data.local.LlibreDao
import org.biblioteca.mypersonallibrary.data.local.LlibreEntity
import org.biblioteca.mypersonallibrary.data.remote.dto.LlibreDto
import org.biblioteca.mypersonallibrary.data.mappers.toDomain
import org.biblioteca.mypersonallibrary.data.mappers.toDto
import org.biblioteca.mypersonallibrary.data.mappers.toEntity
//import org.biblioteca.mypersonallibrary.data.remote.dto.toDomain
//import org.biblioteca.mypersonallibrary.data.remote.dto.toDto
import org.biblioteca.mypersonallibrary.data.local.toEntity
//import org.biblioteca.mypersonallibrary.data.local.toDomain
//import org.biblioteca.mypersonallibrary.data.remote.dto.LlibreDto
import org.biblioteca.mypersonallibrary.data.remote.dto.toEntity
import retrofit2.HttpException
import java.io.IOException

class LlibreRepository(
    private val dao: LlibreDao,                          // <-- DAO de Room
    private val api: BibliotecaApi = RetrofitInstance.api,    // <-- Retrofit (ja el tenies)

) {
    /** Llegeix locals per tenir UI immediata (si uses Room a la llista). */
    fun observeAll(): Flow<List<Llibre>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    /** Esborrat optimista: primer Room (instant a la UI), després backend */
    suspend fun delete(id: Long) = withContext(Dispatchers.IO) {
        dao.deleteById(id)                       // <- la UI s’actualitza de seguida
        runCatching { api.deleteLlibre(id) }     // si falla, ja ho corregirà un refresh posterior
    }

    /** Elimina local immediatament (per a UI instantània). */
    suspend fun deleteLocal(id: Long) = withContext(Dispatchers.IO) {
        dao.deleteById(id)
    }

    suspend fun refreshAll() = withContext(Dispatchers.IO) {
        // Si el teu endpoint retorna Llibre (no DTO), adapta el mapping
        val remote: List<LlibreDto> = runCatching { api.getTotsElsLlibres() }
            .getOrElse { emptyList() }

        val now = System.currentTimeMillis()
        val entities = remote.map { it.toEntity(now) }
        dao.replaceAll(entities)
    }

    fun observeLibraryIsbns(): Flow<Set<String>> =
    dao.observeIsbns().map { it.filterNotNull().map { s ->
        s.replace("-", "")
            .replace(" ", "")
            .uppercase()
    }
        .toSet() }

    /** Opcional: escriu local de seguida perquè la UI salti a dalt per RECENT. */
    suspend fun upsertLocal(l: Llibre): Long = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        dao.upsert(l.toEntity(now))
    }

    /** Opcional: marca pendent de sync (si tens el camp). */
    suspend fun markPendingSync(id: Long) {
        // dao.markPendingSync(id, true)   // només si tens la columna
    }

    /** Opcional: puja updatedAt per reordenar per RECENT. */
    suspend fun touchUpdatedAt(id: Long) {
        // dao.touchUpdatedAt(id, System.currentTimeMillis())
    }

    /** Cerca per ISBN (aquí la teva API sí que pot seguir retornant Response). */
    suspend fun fetchLlibreByIsbn(isbn: String): Result<Llibre?> = try {
        val r = api.fetchByIsbn(isbn)
        if (r.isSuccessful) Result.success(r.body()?.toDomain())
        else Result.failure(HttpException(r))
    } catch (e: IOException) {
        Result.failure(e)
    } catch (e: Exception) {
        Result.failure(e)
    }

    /** Crea i torna la versió canònica del servidor. */
    suspend fun createLlibre(llibre: Llibre): Result<Llibre> = try {
        val r = api.createLlibre(llibre.toDto())
        when {
            r.code() == 409 -> Result.failure(IllegalStateException("Ja existeix un llibre amb aquest ISBN"))
            r.isSuccessful   -> Result.success(r.body()!!.toDomain())
            else             -> Result.failure(HttpException(r))
        }
    } catch (e: IOException) {
        Result.failure(e)
    } catch (e: Exception) {
        Result.failure(e)
    }

    /** Carrega tots els llibres; si falla la xarxa, torna locals/buit. */
    suspend fun getTotsElsLlibres(): List<Llibre> = try {
        api.getTotsElsLlibres().map { it.toDomain() }
    } catch (e: Exception) {
        emptyList()
    }

    suspend fun esborrarLlibre(id: Long): Result<Unit> = try {
        val r = api.deleteLlibre(id)
        if (r.isSuccessful) Result.success(Unit) else Result.failure(HttpException(r))
    } catch (e: IOException) {
        Result.failure(e)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun updateLlibre(l: Llibre): Result<Llibre> = runCatching {
        api.actualitzarLlibre(l.id ?: error("id requerit"), l.toDto()).toDomain()
    }
    /*
    suspend fun updateLlibre(l: Llibre): Result<Llibre> = runCatching {
        val id = l.id ?: error("id requerit per actualitzar")
        api.actualitzarLlibre(id, l.sanitizeForApi())
    }

     */

    // (opcional) sanititza URIs locals per no enviar-les al servidor si no cal
    private fun Llibre.sanitizeForApi(): Llibre =
        if (imatgeUrl?.startsWith("content://") == true) copy(imatgeUrl = null) else this
/*
    // ---------------------------
    //         MAPPEJADORS
    // ---------------------------

    private fun LlibreEntity.toDomain(): Llibre = Llibre(
        id = id,
        titol = titol,
        autor = autor,
        isbn = isbn,
        editorial = editorial,
        edicio = edicio,
        sinopsis = sinopsis,
        pagines = pagines,
        imatgeUrl = imatgeUrl,
        anyPublicacio = anyPublicacio,
        idioma = idioma,
        categoria = categoria,
        ubicacio = ubicacio,
        llegit = llegit,
        comentari = comentari,
        puntuacio = puntuacio
    )

    private fun Llibre.toEntity(): LlibreEntity = LlibreEntity(
        id = id ?: 0L,
        titol = titol,
        autor = autor,
        isbn = isbn,
        editorial = editorial,
        edicio = edicio,
        sinopsis = sinopsis,
        pagines = pagines,
        imatgeUrl = imatgeUrl,
        anyPublicacio = anyPublicacio,
        idioma = idioma,
        categoria = categoria,
        ubicacio = ubicacio,
        llegit = llegit ?: false,
        comentari = comentari,
        puntuacio = puntuacio
        // createdAt / updatedAt / pendingSync tenen default a l'Entity
    )

 */


}

