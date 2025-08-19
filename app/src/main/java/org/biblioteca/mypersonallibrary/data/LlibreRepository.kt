package org.biblioteca.mypersonallibrary.data

import retrofit2.HttpException
import java.io.IOException

class LlibreRepository {

    private val api = RetrofitInstance.api

    suspend fun fetchLlibreByIsbn(isbn: String): Result<Llibre?> = try {
        val r = api.fetchByIsbn(isbn)
        if (r.isSuccessful) Result.success(r.body())
        else Result.failure(HttpException(r))
    } catch (e: IOException) { // sense connexió / timeout
        Result.failure(e)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun createLlibre(llibre: Llibre): Result<Unit> = try {
        val r = api.createLlibre(llibre)
        when {
            r.code() == 409 -> Result.failure(IllegalStateException("Ja existeix un llibre amb aquest ISBN"))
            r.isSuccessful    -> Result.success(Unit)
            else              -> Result.failure(HttpException(r))
        }
    } catch (e: IOException) {
        Result.failure(e)
    } catch (e: Exception) {
        Result.failure(e)
    }


    suspend fun getTotsElsLlibres(): List<Llibre> {
        return try {
            api.getTotsElsLlibres()
        } catch (e: Exception) {
            emptyList()
        }
        /*
        val response = api.getAll()
        return response.body() ?: emptyList()

         */
    }

    suspend fun esborrarLlibre(id: Long): Result<Unit> = try {
        val r = api.deleteLlibre(id)
        if (r.isSuccessful) Result.success(Unit)
        else Result.failure(HttpException(r))
    } catch (e: IOException) {
        Result.failure(e)             // sense xarxa / timeout
    } catch (e: Exception) {
        Result.failure(e)
    }

}