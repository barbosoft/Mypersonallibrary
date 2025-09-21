package org.biblioteca.mypersonallibrary.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface LlibreDao {
    @Query("SELECT * FROM llibre ORDER BY updatedAt DESC")
    fun observeAll(): Flow<List<LlibreEntity>>

    @Query("SELECT isbn FROM llibre WHERE isbn IS NOT NULL")
    fun observeIsbns(): Flow<List<String>>

    @Query("SELECT EXISTS(SELECT 1 FROM llibre WHERE isbn = :isbn)")
    suspend fun existsByIsbn(isbn: String): Boolean

    @Query("DELETE FROM llibre WHERE id = :id")
    suspend fun deleteById(id: Long)

    //  Upsert d’un sol element
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: LlibreEntity): Long

    //  Upsert de llista (per guardar el que ve del servidor)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entities: List<LlibreEntity>): List<Long>

    // Marca com pendent: simplement pugem updatedAt per guanyar el RECENT a UI
    @Query("UPDATE llibre SET updatedAt = :updatedAt WHERE id = :id")
    suspend fun markPendingSync(id: Long, updatedAt: Long)

    @Query("SELECT * FROM llibre ORDER BY updatedAt DESC")
    suspend fun getAllOnce(): List<LlibreEntity>

    @Query("DELETE FROM llibre")
    suspend fun clearAll()

    @Transaction
    suspend fun replaceAll(newOnes: List<LlibreEntity>) {
        clearAll()
        upsert(newOnes)
    }


}
