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

    // true  → Usa datos falsos
    // false → Se conecta al servidor real
    private const val USAR_MOCK = true

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    fun getMockData(): List<Lectura> {
        return listOf(
            Lectura(lec1 = 32.98, lec2 = 21.82, lec3 = 23.0, hora = "05:30 am"),
            Lectura(lec1 = 35.70, lec2 = 24.15, lec3 = 25.3, hora = "06:00 am"),
            Lectura(lec1 = 38.20, lec2 = 26.43, lec3 = 27.8, hora = "06:30 am"),
            Lectura(lec1 = 42.10, lec2 = 28.97, lec3 = 30.2, hora = "07:00 am"),
            Lectura(lec1 = 45.60, lec2 = 31.24, lec3 = 32.5, hora = "07:30 am"),
            Lectura(lec1 = 59.78, lec2 = 27.96, lec3 = null, hora = "07:29 am")
        )
    }

    fun isMockMode(): Boolean = USAR_MOCK
}