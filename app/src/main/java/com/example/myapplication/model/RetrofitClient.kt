package com.example.myapplication.model

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private var retrofit: Retrofit? = null
    private var lastToken: String? = null
    private var lastBaseUrl: String? = null

    fun getClient(baseUrl: String, apiToken: String? = null): Retrofit {
        val httpUrl = baseUrl.toHttpUrlOrNull()
            ?: throw IllegalArgumentException("Ungültige Base-URL: $baseUrl")

        val tokenForCheck = apiToken?.trim()?.ifBlank { null }
        val tokenChanged = tokenForCheck != lastToken
        val urlChanged = baseUrl != lastBaseUrl

        if (retrofit == null || urlChanged || tokenChanged) {
            Log.d("RetrofitClient", "Erstelle neuen Retrofit-Client")
            
            val clientBuilder = OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(120, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)

            if (tokenForCheck != null) {
                clientBuilder.addInterceptor { chain ->
                    val request = chain.request().newBuilder()
                        .header("Authorization", "Bearer $tokenForCheck")
                        .build()
                    chain.proceed(request)
                }
            }

            retrofit = Retrofit.Builder()
                .baseUrl(httpUrl)
                .client(clientBuilder.build())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            
            lastToken = tokenForCheck
            lastBaseUrl = baseUrl
        }
        return retrofit!!
    }
}
