package org.biblioteca.mypersonallibrary.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.biblioteca.mypersonallibrary.data.Llibre

@Entity(tableName = "llibre")
data class LlibreEntity(
    @PrimaryKey(autoGenerate = true) val id: Long? = null,
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
    val llegit: Boolean? = null,
    val comentari: String? = null,
    val puntuacio: Int? = null,
    /** → clau per ordenar per RECENT */
    val updatedAt: Long = System.currentTimeMillis()
)

// Mappers
fun LlibreEntity.toDomain(): Llibre = Llibre(
    id = id,
    titol = titol,
    autor = autor,
    editorial = editorial,
    edicio = edicio,
    isbn = isbn,
    sinopsis = sinopsis,
    pagines = pagines,
    imatgeUrl = imatgeUrl,
    anyPublicacio = anyPublicacio,
    idioma = idioma,
    categoria = categoria,
    ubicacio = ubicacio,
    llegit = llegit,
    comentari = comentari,
    puntuacio = puntuacio,
    updatedAt = updatedAt
)

fun Llibre.toEntity(): LlibreEntity = LlibreEntity(
    id = id,
    titol = titol,
    autor = autor,
    editorial = editorial,
    edicio = edicio,
    isbn = isbn,
    sinopsis = sinopsis,
    pagines = pagines,
    imatgeUrl = imatgeUrl,
    anyPublicacio = anyPublicacio,
    idioma = idioma,
    categoria = categoria,
    ubicacio = ubicacio,
    llegit = llegit ?: false,
    comentari = comentari,
    puntuacio = puntuacio,
    updatedAt = updatedAt ?: System.currentTimeMillis()
)
