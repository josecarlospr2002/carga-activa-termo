package com.example.carga_activa_termo

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

interface ApiService {
    @GET("api/carga-activa.php")
    suspend fun getCargaActiva(): List<Lectura>
}

object ApiClient {
    private const val BASE_URL = "http://web.ctehabana.une.cu/"

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}