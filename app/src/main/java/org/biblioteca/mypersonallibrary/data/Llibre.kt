package org.biblioteca.mypersonallibrary.data

data class Llibre(
    val id: Long? = null,
    val titol: String? = null,
    val autor: String? = null,
    val editorial: String? = null,
    val edicio: String? = null,
    val isbn: String? = null,
    val sinopsis: String? = null,
    val pagines: Int? = null,
    val imatgeUrl: String? = null,
    val anyPublicacio: String? = null,
    val idioma: String? = null,
    val categoria: String? = null,
    val ubicacio: String? = null,

    // 🔽 Camps nous (opcionales)
    val llegit: Boolean? = null,
    val comentari: String? = null,
    val puntuacio: Int? = null, // 0..5

    val updatedAt: Long = System.currentTimeMillis()
)