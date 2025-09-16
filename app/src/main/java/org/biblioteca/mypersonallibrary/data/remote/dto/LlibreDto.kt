package org.biblioteca.mypersonallibrary.data.remote.dto

data class LlibreDto(
    val id: Long? = null,
    val titol: String? = null,
    val autor: String? = null,
    val isbn: String? = null,
    val editorial: String? = null,
    val edicio: String? = null,
    val sinopsis: String? = null,
    val pagines: Int? = null,
    val imatgeUrl: String? = null,
    val anyPublicacio: String? = null,
    val idioma: String? = null,
    val categoria: String? = null,
    val ubicacio: String? = null,
    val llegit: Boolean? = null,
    val comentari: String? = null,
    val puntuacio: Int? = null
)
