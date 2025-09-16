package org.biblioteca.mypersonallibrary.data

import retrofit2.Response
import retrofit2.http.*

interface BibliotecaApi {

    @GET("api/llibres/fetch/{isbn}")
    suspend fun fetchByIsbn(@Path("isbn") isbn: String): Response<Llibre>

    @POST("api/llibres")
    suspend fun createLlibre(@Body llibre: Llibre): Response<Llibre>

    @GET("api/llibres/autor/{autor}")
    suspend fun getByAutor(@Path("autor") autor: String): Response<Llibre>

    @GET("api/llibres")
    suspend fun getAll(): Response<List<Llibre>>

    @GET("api/llibres")
    suspend fun getTotsElsLlibres(): List<Llibre>

    @DELETE("api/llibres/{id}")
    suspend fun deleteLlibre(@Path("id") id: Long): Response<Unit>

    @PUT("api/llibres/{id}")
    suspend fun actualitzarLlibre(@Path("id") id: Long, @Body llibre: Llibre): Llibre

    // endpoints Wishlist
    @GET("wishlist")
    suspend fun getWishlist(): List<WishlistItem>

    @GET("wishlist")
    suspend fun getTotsElsLlibresPerComprar(): List<Llibre>

    @POST("wishlist")
    suspend fun addWishlist(@Body item: WishlistItem): WishlistItem

    @DELETE("wishlist/{id}")
    suspend fun deleteWishlist(@Path("id") id: Long)

    @POST("wishlist/{id}/purchase")
    suspend fun purchaseWishlist(@Path("id") id: Long): Llibre


}