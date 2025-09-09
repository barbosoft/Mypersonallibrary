package org.biblioteca.mypersonallibrary.data.remote

import org.biblioteca.mypersonallibrary.data.remote.dto.LlibreDto
import org.biblioteca.mypersonallibrary.data.remote.dto.WishDto
import retrofit2.http.*

interface WishlistApi {

    @GET("api/wishlist") suspend fun getAll(): List<WishDto>
    @POST("api/wishlist/upsertAll") suspend fun upsertAll(@Body items: List<WishDto>): List<WishDto>
    @HTTP(method = "DELETE", path = "api/wishlist/{id}", hasBody = false) suspend fun delete(@Path("id") id: Long)
    @POST("api/wishlist/deleteMany") suspend fun deleteMany(@Body ids: List<Long>)
    @POST("api/wishlist/purchase/{id}") suspend fun purchase(@Path("id") id: Long): LlibreDto  // el teu DTO de llibre
    @POST("api/wishlist/upsert") suspend fun upsert(@Body dto: WishDto): WishDto // si l’estàs usant


}



