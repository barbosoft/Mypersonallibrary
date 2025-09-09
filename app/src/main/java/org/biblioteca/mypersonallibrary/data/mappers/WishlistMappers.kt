package org.biblioteca.mypersonallibrary.data.mappers

import org.biblioteca.mypersonallibrary.data.local.WishlistEntity
import org.biblioteca.mypersonallibrary.data.remote.dto.WishDto
import org.biblioteca.mypersonallibrary.data.WishlistItem
import org.biblioteca.mypersonallibrary.data.remote.dto.LlibreDto

/**
 * Extensions de mapeig entre les 3 capes:
 *  - Entity (Room)  <->  DTO (API)
 *  - Entity (Room)  ->   Domini (WishlistItem)
 *  - Domini (WishlistItem) -> Entity (Room)
 */
object WishlistMappers {

    /* ---------- Entity -> Domini ---------- */
    fun WishlistEntity.toDomain(): WishlistItem =
        WishlistItem(
            id            = if (id == 0L) null else id,
            titol         = titol,
            autor         = autor,
            isbn          = isbn,
            sinopsis      = sinopsis,
            notes         = notes,
            imatgeUrl     = imatgeUrl,
            idioma        = idioma,
            pagines       = pagines,
            editorial     = editorial,
            edicio        = edicio,
            anyPublicacio = anyPublicacio,
            preuDesitjat  = preuDesitjat,
            createdAt     = createdAt
        )

    /* ---------- Domini -> Entity ---------- */
    fun WishlistItem.toEntity(now: Long = System.currentTimeMillis()): WishlistEntity =
        WishlistEntity(
            id            = id ?: 0L,
            titol         = titol,
            autor         = autor,
            isbn          = isbn,
            sinopsis      = sinopsis,
            notes         = notes,
            imatgeUrl     = imatgeUrl,
            idioma        = idioma,
            pagines       = pagines,
            editorial     = editorial,
            edicio        = edicio,
            anyPublicacio = anyPublicacio,
            preuDesitjat  = preuDesitjat,
            createdAt     = if (createdAt > 0) createdAt else now,
            updatedAt     = now,
            pendingSync   = true,   // per defecte: pendent fins que sync() marqui OK
            deleted       = false
        )

    // Domini WishlistItem -> DTO
    fun WishlistItem.toDto(updatedAt: Long = System.currentTimeMillis()): WishDto =
        WishDto(
            id = id,
            titol = titol,
            autor = autor,
            isbn = isbn,
            sinopsis = sinopsis,
            notes = notes,
            imatgeUrl = imatgeUrl,
            idioma = idioma,
            pagines = pagines,
            editorial = editorial,
            edicio = edicio,
            anyPublicacio = anyPublicacio,
            preuDesitjat = preuDesitjat,
            updatedAt = updatedAt,
            isDeleted = false
        )

    /* ---------- Entity -> DTO ---------- */
    fun WishlistEntity.toDto(): WishDto =
        WishDto(
            id            = id,
            titol         = titol,
            autor         = autor,
            isbn          = isbn,
            sinopsis      = sinopsis,
            notes         = notes,
            imatgeUrl     = imatgeUrl,
            idioma        = idioma,
            pagines       = pagines,
            editorial     = editorial,
            edicio        = edicio,
            anyPublicacio = anyPublicacio,
            preuDesitjat  = preuDesitjat,

            // si el teu DTO porta created/updatedAt els afegeixes aquí
            updatedAt = updatedAt,
            isDeleted = deleted
        )

    /* ---------- DTO -> Entity ---------- */
    fun WishDto.toEntity(now: Long = System.currentTimeMillis()): WishlistEntity =
        WishlistEntity(
            id            = id ?: 0L,
            titol         = titol,
            autor         = autor,
            isbn          = isbn,
            sinopsis      = sinopsis,
            notes         = notes,
            imatgeUrl     = imatgeUrl,
            idioma        = idioma,
            pagines       = pagines,
            editorial     = editorial,
            edicio        = edicio,
            anyPublicacio = anyPublicacio,
            preuDesitjat  = preuDesitjat,
            createdAt     = createdAt ?: now, /* si el DTO no en porta, usa now */
            updatedAt     = updatedAt ?: now,
            pendingSync   = false,
            deleted       = isDeleted ?: false
        )



    fun LlibreDto.toDomain(): org.biblioteca.mypersonallibrary.data.Llibre =
        org.biblioteca.mypersonallibrary.data.Llibre(
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

    // Entity -> DTO pensat per POST/PUT (incloent camps de control si els vols enviar)
    fun WishlistEntity.toDtoForPost(now: Long = System.currentTimeMillis()): WishDto =
        WishDto(
            id            = id.takeIf { it != 0L },
            titol         = titol,
            autor         = autor,
            isbn          = isbn,
            sinopsis      = sinopsis,
            notes         = notes,
            imatgeUrl     = imatgeUrl,
            idioma        = idioma,
            pagines       = pagines,
            editorial     = editorial,
            edicio        = edicio,
            anyPublicacio = anyPublicacio,
            preuDesitjat  = preuDesitjat,
            createdAt     = createdAt,
            updatedAt     = updatedAt,
            isDeleted     = deleted
        )

}
