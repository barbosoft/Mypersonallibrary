package org.biblioteca.mypersonallibrary.data

import org.biblioteca.mypersonallibrary.data.remote.dto.LlibreDto
import org.biblioteca.mypersonallibrary.data.remote.dto.WishDto
import retrofit2.Response
import retrofit2.http.*

interface BibliotecaApi {

    // ---------- Llibres ----------
    @GET("llibres")
    suspend fun getTotsElsLlibres(): List<LlibreDto>

    @GET("llibres/fetch/{isbn}")
    suspend fun fetchByIsbn(@Path("isbn") isbn: String): Response<LlibreDto>

    @POST("llibres")
    suspend fun createLlibre(@Body dto: LlibreDto): Response<LlibreDto>

    @PUT("llibres/{id}")
    suspend fun actualitzarLlibre(@Path("id") id: Long, @Body dto: LlibreDto): LlibreDto

    @DELETE("llibres/{id}")
    suspend fun deleteLlibre(@Path("id") id: Long): Response<Unit>

    @GET("llibres/autor/{autor}")
    suspend fun getByAutor(@Path("autor") autor: String): List<LlibreDto>

    // ---------- Wishlist ----------
    @GET("wishlist")
    suspend fun getWishlist(): List<WishDto>

    @POST("wishlist/upsert")
    suspend fun upsertWishlist(@Body dto: WishDto): WishDto

    @POST("wishlist/upsertAll")
    suspend fun upsertWishlistAll(@Body dtos: List<WishDto>): List<WishDto>

    @DELETE("wishlist/{id}")
    suspend fun deleteWishlist(@Path("id") id: Long)

    @POST("wishlist/deleteMany")
    suspend fun deleteWishlistMany(@Body ids: List<Long>)

    @POST("wishlist/purchase/{id}")
    suspend fun purchaseWishlist(@Path("id") id: Long): LlibreDto
}
