package org.biblioteca.mypersonallibrary.data

import android.util.Log
import androidx.core.app.NotificationCompat.MessagingStyle.Message
import com.google.gson.GsonBuilder
//import com.google.zxing.client.android.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import org.biblioteca.mypersonallibrary.BuildConfig
import org.json.JSONArray
import org.json.JSONObject

object RetrofitInstance {

    //private const val BASE_URL = "http://10.0.2.2:8080/"  // Emulador Android Studio
    private const val BASE_URL = "http://192.168.1.145:8080/" // Amb mòbil físic

    // Funció per fer Pretty print del JSONç
    private fun prettyJsonIfPossible(message: String): String {
        return try {
            val trimmed = message.trim()
            when {
                trimmed.startsWith("{") -> JSONObject(trimmed).toString(4)
                trimmed.startsWith("{") -> JSONArray(trimmed).toString(4)
                else -> message // Si no és JSON, no ho modifiquem
            }
        } catch (_: Exception) {
            message // Si no és JSON vàlid, deixem el text original
        }
    }

    // Logger d'HTTP (tag ? "HTTP" al Logcat
    private val logging = HttpLoggingInterceptor { msg ->
        Log.d("BIBLIOTECA_HTTP", prettyJsonIfPossible(msg))
    }.apply {
        level = if (BuildConfig.DEBUG)
            HttpLoggingInterceptor.Level.BODY

        else
            HttpLoggingInterceptor.Level.NONE


    }

    private val  client: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(logging)
        .build()

    val api: BibliotecaApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client) //Per filtrar al Logcat
            .addConverterFactory(
                GsonConverterFactory.create(
                    GsonBuilder().setPrettyPrinting().create()
                ))
            .build()
            .create(BibliotecaApi::class.java)
    }

}