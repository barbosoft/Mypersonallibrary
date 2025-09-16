package org.biblioteca.mypersonallibrary.data


import org.biblioteca.mypersonallibrary.data.mappers.WishlistMappers.toDomain
import org.biblioteca.mypersonallibrary.data.remote.dto.LlibreDto

class WishlistRepository(private val api: BibliotecaApi = RetrofitInstance.api) {

    suspend fun getAll(): List<WishlistItem> = api.getWishlist()
    suspend fun add(item: WishlistItem): WishlistItem = api.addWishlist(item)
    suspend fun delete(id: Long) = api.deleteWishlist(id)
    suspend fun purchase(id: Long): Llibre = api.purchaseWishlist(id)

    fun llibreDtoToDomain(dto: LlibreDto) = dto.toDomain()
    suspend fun getTotsElsLlibresPerComprar(): List<Llibre> {
        return try {
            api.getTotsElsLlibresPerComprar()
        } catch (e: Exception) {
            emptyList()
        }

    }

}
