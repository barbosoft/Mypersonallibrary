package org.biblioteca.mypersonallibrary.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "wishlist")
data class WishlistEntity(
    @PrimaryKey(autoGenerate = true)
    //val id: Long = 0L,
    val id: Long? = null,
    val titol: String? = null,
    val autor: String? = null,
    val isbn: String? = null,
    val sinopsis: String? = null,
    val notes: String? = null,
    val imatgeUrl: String? = null,
    val idioma: String? = null,
    val pagines: Int? = null,
    val editorial: String? = null,
    val edicio: String? = null,
    val anyPublicacio: String? = null,
    val preuDesitjat: Double? = null,
    val createdAt: Long = System.currentTimeMillis(),

    // 🔽 Camps locals per a la sync
    val updatedAt: Long = System.currentTimeMillis(),
    val pendingSync: Boolean = false,
    val deleted: Boolean = false
)
