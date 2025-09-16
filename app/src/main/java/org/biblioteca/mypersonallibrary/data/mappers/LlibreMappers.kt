package org.biblioteca.mypersonallibrary.data.mappers

import org.biblioteca.mypersonallibrary.data.Llibre
import org.biblioteca.mypersonallibrary.data.local.LlibreEntity
import org.biblioteca.mypersonallibrary.data.remote.dto.LlibreDto

// -------- Entity ↔ Domain --------
fun LlibreEntity.toDomain(): Llibre = Llibre(
    id = id,
    titol = titol,
    autor = autor,
    isbn = isbn,
    editorial = editorial,
    edicio = edicio,
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
    updatedAt = updatedAt // si el teu domain el té; si no, elimina aquesta línia
)

fun Llibre.toEntity(): LlibreEntity = LlibreEntity(
    id = id ?: 0L,
    titol = titol,
    autor = autor,
    isbn = isbn,
    editorial = editorial,
    edicio = edicio,
    sinopsis = sinopsis,
    pagines = pagines,
    imatgeUrl = imatgeUrl,
    anyPublicacio = anyPublicacio,
    idioma = idioma,
    categoria = categoria,
    ubicacio = ubicacio,
    llegit = llegit ?: false,
    comentari = comentari,
    puntuacio = puntuacio
    // createdAt/updatedAt/pendingSync tenen default a l’Entity
)

// Sobrecàrrega per actualitzar l'ordre “RECENT”
fun Llibre.toEntity(now: Long): LlibreEntity = toEntity().copy(updatedAt = now)


// -------- DTO ↔ Domain --------
fun LlibreDto.toDomain(): Llibre = Llibre(
    id = id,
    titol = titol,
    autor = autor,
    isbn = isbn,
    editorial = editorial,
    edicio = edicio,
    sinopsis = sinopsis,
    pagines = pagines,
    imatgeUrl = imatgeUrl,
    anyPublicacio = anyPublicacio,
    idioma = idioma,
    categoria = categoria,
    ubicacio = ubicacio,
    llegit = llegit,
    comentari = comentari,
    puntuacio = puntuacio
)

fun Llibre.toDto(): LlibreDto = LlibreDto(
    id = id,
    titol = titol,
    autor = autor,
    isbn = isbn,
    editorial = editorial,
    edicio = edicio,
    sinopsis = sinopsis,
    pagines = pagines,
    imatgeUrl = imatgeUrl,
    anyPublicacio = anyPublicacio,
    idioma = idioma,
    categoria = categoria,
    ubicacio = ubicacio,
    llegit = llegit ?: false,
    comentari = comentari,
    puntuacio = puntuacio
)


// -------- DTO → Entity (per al “pull”) --------
fun LlibreDto.toEntity(): LlibreEntity = LlibreEntity(
    id = id ?: 0L,
    titol = titol,
    autor = autor,
    isbn = isbn,
    editorial = editorial,
    edicio = edicio,
    sinopsis = sinopsis,
    pagines = pagines,
    imatgeUrl = imatgeUrl,
    anyPublicacio = anyPublicacio,
    idioma = idioma,
    categoria = categoria,
    ubicacio = ubicacio,
    llegit = llegit,
    comentari = comentari,
    puntuacio = puntuacio
)

fun LlibreDto.toEntity(now: Long): LlibreEntity = toEntity().copy(updatedAt = now)

