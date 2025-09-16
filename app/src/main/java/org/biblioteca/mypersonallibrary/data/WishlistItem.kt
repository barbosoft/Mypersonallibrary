package org.biblioteca.mypersonallibrary.data

data class WishlistItem(
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
    val createdAt: Long = System.currentTimeMillis(), // 👈 necessari per BY_RECENT
    val updatedAt: Long? = null
)
