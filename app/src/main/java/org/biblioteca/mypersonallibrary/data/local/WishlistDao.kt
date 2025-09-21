package org.biblioteca.mypersonallibrary.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface WishlistDao {

    @Query("SELECT * FROM wishlist ORDER BY updatedAt DESC")
    fun observeAll(): Flow<List<WishlistEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: WishlistEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<WishlistEntity>)

    @Query("UPDATE wishlist SET deleted = 1, updatedAt = :updatedAt WHERE id = :id")
    suspend fun markDeleted(id: Long, updatedAt: Long)

    @Query("DELETE FROM wishlist WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM wishlist WHERE pendingSync = 1")
    suspend fun getPendingSync(): List<WishlistEntity>

    @Query("SELECT * FROM wishlist WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): WishlistEntity?

    //@Query("SELECT * FROM wishlist ORDER BY updatedAt DESC")

    @Query("UPDATE wishlist SET pendingSync = 0 WHERE id IN (:ids)")
    suspend fun markSynced(ids: List<Long?>)

    @Query("DELETE FROM wishlist")
    suspend fun clearAll()

    @Transaction
    suspend fun replaceAll(items: List<WishlistEntity>) {
        clearAll()
        upsertAll(items)
    }

    @Query("DELETE FROM wishlist WHERE id = 0")
    suspend fun deleteWithIdZero()
}
