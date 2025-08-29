package org.biblioteca.mypersonallibrary.data

data class WishlistItem(
    val id: Long? = null,
    val titol: String? = null,
    val autor: String? = null,
    val isbn: String? = null,
    val sinopsis: String? = null,
    val imatgeUrl: String? = null,
    val notes: String? = null,
    val preuDesitjat: Double? = null
)
