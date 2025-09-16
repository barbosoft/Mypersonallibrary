package org.biblioteca.mypersonallibrary.data

import android.util.Log
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.biblioteca.mypersonallibrary.BuildConfig
import org.biblioteca.mypersonallibrary.data.remote.WishlistApi
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitInstance {

    // Emulador: "http://10.0.2.2:8080/"
    private const val BASE_URL = "http://192.168.1.145:8080/api/"

    // Pretty print si el missatge és JSON
    private fun prettyJsonIfPossible(message: String): String = try {
        val t = message.trim()
        when {
            t.startsWith("{") -> JSONObject(t).toString(4)
            t.startsWith("[") -> JSONArray(t).toString(4)   // <-- arreglat (abans hi havia "{")
            else -> message
        }
    } catch (_: Exception) { message }

    // Logger HTTP
    private val logging = HttpLoggingInterceptor { msg ->
        Log.d("BIBLIOTECA_HTTP", prettyJsonIfPossible(msg))
    }.apply {
        level = if (BuildConfig.DEBUG)
            HttpLoggingInterceptor.Level.BODY
        else
            HttpLoggingInterceptor.Level.NONE
    }

    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

   /* // Gson configurat
    private val gson by lazy {
        GsonBuilder()
            .serializeNulls()   // conserva nulls si cal
            .setLenient()       // permissiu amb JSON irregular
            .create()
    }

    */
   // --------- Gson compartit amb l’adapter de Long/ISO ----------
   private val gson: Gson by lazy {
       val longAdapter = LongFromStringOrIsoInstantAdapter()

       GsonBuilder()
           .serializeNulls()
           .setLenient()
           // IMPORTANT: registrar per a Long (boxed) i per al primitive long
           .registerTypeAdapter(java.lang.Long::class.java, longAdapter)
           .registerTypeAdapter(java.lang.Long.TYPE, longAdapter)
           // (opcional) pretty printing als logs
           .setPrettyPrinting()
           .create()
   }


    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    val api: BibliotecaApi by lazy { retrofit.create(BibliotecaApi::class.java) }
}
