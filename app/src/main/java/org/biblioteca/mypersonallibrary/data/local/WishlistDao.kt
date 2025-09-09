package org.biblioteca.mypersonallibrary.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface WishlistDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: WishlistEntity): Long   // 👈 retorna ID

    @Query("UPDATE wishlist SET deleted = 1, pendingSync = 1, updatedAt = :updatedAt WHERE id = :id")
    suspend fun markDeleted(id: Long, updatedAt: Long)

    @Query("UPDATE wishlist SET pendingSync = 0 WHERE id IN (:ids)")
    suspend fun markSynced(ids: List<Long>)

    @Query("SELECT * FROM wishlist WHERE pendingSync = 1")
    suspend fun getPendingSync(): List<WishlistEntity>

    @Query("SELECT * FROM wishlist WHERE id = :id LIMIT 1")
    suspend fun getByIdNow(id: Long): WishlistEntity?

    @Query("DELETE FROM wishlist WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM wishlist")
    suspend fun clearAll()

    @Transaction
    suspend fun replaceAll(newOnes: List<WishlistEntity>) {
        clearAll()
        newOnes.forEach { upsert(it) }
    }

    @Query("SELECT * FROM wishlist ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<WishlistEntity>>
}



/*
    @Query("SELECT * FROM wishlist WHERE deleted = 0 ORDER BY updatedAt DESC")
    fun observeAll(): Flow<List<WishlistEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: WishlistEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(entities: List<WishlistEntity>)

    // Soft-delete: marquem borrat i pendent de sync
    @Query("UPDATE wishlist SET deleted = 1, pendingSync = 1, updatedAt = :updatedAt WHERE id = :id")
    suspend fun markDeleted(id: Long, updatedAt: Long)

    // Elements locals pendents d’enviar al servidor
    @Query("SELECT * FROM wishlist WHERE pendingSync = 1")
    suspend fun getPendingSync(): List<WishlistEntity>

    // Després d’un PUSH correcte
    @Query("UPDATE wishlist SET pendingSync = 0 WHERE id IN (:ids)")
    suspend fun markSynced(ids: List<Long>)

    @Query("DELETE FROM wishlist")
    suspend fun clearAll()

    // Convenience per substituir l’estat local amb el remot
    @Transaction
    suspend fun replaceAll(entities: List<WishlistEntity>) {
        clearAll()
        upsertAll(entities)
    }

 */


