package org.biblioteca.mypersonallibrary.data

class WishlistRepository(private val api: BibliotecaApi = RetrofitInstance.api) {

    suspend fun getAll(): List<WishlistItem> = api.getWishlist()
    suspend fun add(item: WishlistItem): WishlistItem = api.addWishlist(item)
    suspend fun delete(id: Long) = api.deleteWishlist(id)
    suspend fun purchase(id: Long): Llibre = api.purchaseWishlist(id)
}
