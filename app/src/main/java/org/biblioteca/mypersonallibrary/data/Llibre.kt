package org.biblioteca.mypersonallibrary.data

data class Llibre (
    val id: Long? = null,
    val titol: String? = null,
    val autor: String? = null,
    val editorial: String? = null,
    val edicio: String? = null,
    val isbn: String? = null,
    val sinopsis: String? = null,
    val pagines: Int? = null,
    val imatgeUrl: String? = null,
    val anyPublicacio: Int? = null,
    val idioma: String? = null,
    val categoria: String? = null,
    val ubicacio: String? = null
)