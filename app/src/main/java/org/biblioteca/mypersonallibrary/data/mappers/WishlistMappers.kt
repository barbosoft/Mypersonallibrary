package org.biblioteca.mypersonallibrary.data.mappers

import org.biblioteca.mypersonallibrary.data.WishlistItem
import org.biblioteca.mypersonallibrary.data.local.WishlistEntity
import org.biblioteca.mypersonallibrary.data.remote.dto.WishDto

object WishlistMappers {

    // -------- DTO -> ENTITY --------
    fun WishDto.toEntity(now: Long): WishlistEntity =
        WishlistEntity(
            id          = this.id ?: 0L,
            titol       = this.titol,
            autor       = this.autor,
            isbn        = this.isbn ?: "",
            imatgeUrl   = this.imatgeUrl,
            notes       = this.notes,
            updatedAt   = this.updatedAt ?: now,
            pendingSync = false,
            deleted     = false
        )

    // -------- DOMAIN -> ENTITY --------
    fun WishlistItem.toEntity(now: Long): WishlistEntity =
        WishlistEntity(
            id          = this.id ?: 0L,
            titol       = this.titol,
            autor       = this.autor,
            isbn        = this.isbn ?: "",
            imatgeUrl   = this.imatgeUrl,
            notes       = this.notes,
            updatedAt   = this.updatedAt ?: now,
            pendingSync = true,   // per defecte quan ho creem des de UI
            deleted     = false
        )

    // -------- ENTITY -> DTO (POST/PUT) --------
    fun WishlistEntity.toDtoForPost(now: Long): WishDto =
        WishDto(
            id        = null,   // si és 0/NULL, al servidor es crearà
            titol     = this.titol,
            autor     = this.autor,
            isbn      = this.isbn,
            imatgeUrl = this.imatgeUrl,
            notes     = this.notes,
            updatedAt = now
        )

    // (opc.) DOMAIN -> DTO (per si uses el repo simple)
    fun WishlistItem.toDtoForPost(now: Long): WishDto =
        WishDto(
            id        = this.id?.takeIf { it > 0L },
            titol     = this.titol,
            autor     = this.autor,
            isbn      = this.isbn,
            imatgeUrl = this.imatgeUrl,
            notes     = this.notes,
            updatedAt = now
        )

    // -------- ENTITY -> DOMAIN --------
    fun WishlistEntity.toDomain(): WishlistItem =
        WishlistItem(
            id        = this.id,
            titol     = this.titol,
            autor     = this.autor,
            isbn      = this.isbn,
            imatgeUrl = this.imatgeUrl,
            notes     = this.notes,
            updatedAt = this.updatedAt
        )

    // -------- DTO -> DOMAIN --------
    fun WishDto.toDomain(): WishlistItem =
        WishlistItem(
            id        = this.id ?: 0L,
            titol     = this.titol,
            autor     = this.autor,
            isbn      = this.isbn ?: "",
            imatgeUrl = this.imatgeUrl,
            notes     = this.notes,
            updatedAt = this.updatedAt
        )
}
