package com.example.carga_activa_termo

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

interface ApiService {
    @GET("api/carga-activa.php")
    suspend fun getCargaActiva(): List<Lectura>

    @GET("api/carga-activa-24.php")
    suspend fun getCargaActiva24h(): List<Lectura>
}

object ApiClient {
    private const val BASE_URL = "http://web.ctehabana.une.cu/"

    // true  → Usa datos falsos
    // false → Se conecta al servidor real
    private const val USAR_MOCK = false

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    fun getMockData(): List<Lectura> {
        return listOf(
            Lectura(lec1 = 63.78, lec2 = 83.96, lec3 = null, hora = "08:24 am")
        )
    }

    fun getMockData24h(): List<Lectura> {
        return listOf(
            Lectura(lec1 = 55.81, lec2 = 68.99, lec3 = 05.00, hora = "03:00 pm"),
            Lectura(lec1 = 55.89, lec2 = 69.40, lec3 = 05.00, hora = "04:00 pm"),
            Lectura(lec1 = 57.35, lec2 = 68.35, lec3 = 05.00, hora = "05:00 pm"),
            Lectura(lec1 = 55.66, lec2 = 67.30, lec3 = 05.00, hora = "06:00 pm"),
            Lectura(lec1 = 56.54, lec2 = 68.24, lec3 = 05.00, hora = "07:00 pm"),
            Lectura(lec1 = 57.19, lec2 = 81.86, lec3 = 05.00, hora = "08:00 pm"),
            Lectura(lec1 = 56.57, lec2 = 82.22, lec3 = 05.00, hora = "09:00 pm"),
            Lectura(lec1 = 56.42, lec2 = 81.27, lec3 = 05.00, hora = "10:00 pm"),
            Lectura(lec1 = 56.51, lec2 = 81.73, lec3 = 05.00, hora = "11:00 pm"),
            Lectura(lec1 = 56.98, lec2 = 81.84, lec3 = 05.00, hora = "12:00 am"),
            Lectura(lec1 = 56.62, lec2 = 81.62, lec3 = 05.00, hora = "01:00 am"),
            Lectura(lec1 = 56.92, lec2 = 81.75, lec3 = 05.00, hora = "02:00 am"),
            Lectura(lec1 = 56.77, lec2 = 81.31, lec3 = 05.00, hora = "03:00 am"),
            Lectura(lec1 = 57.23, lec2 = 81.27, lec3 = 05.00, hora = "04:00 am"),
            Lectura(lec1 = 57.01, lec2 = 81.04, lec3 = 05.00, hora = "05:00 am"),
            Lectura(lec1 = 58.31, lec2 = 81.38, lec3 = 05.00, hora = "06:00 am"),
            Lectura(lec1 = 57.32, lec2 = 81.16, lec3 = 24.10, hora = "07:00 am"),
            Lectura(lec1 = 57.35, lec2 = 81.84, lec3 = 24.10, hora = "08:00 am"),
            Lectura(lec1 = 57.82, lec2 = 81.15, lec3 = 24.10, hora = "09:00 am"),
            Lectura(lec1 = 57.74, lec2 = 80.13, lec3 = 24.10, hora = "10:00 am"),
            Lectura(lec1 = 56.59, lec2 = 81.62, lec3 = 24.10, hora = "11:00 am"),
            Lectura(lec1 = null, lec2 = 81.15, lec3 = 24.10, hora = "12:00 pm"),
            Lectura(lec1 = null, lec2 = 75.07, lec3 = 24.10, hora = "01:00 pm"),
            Lectura(lec1 = null, lec2 = 70.08, lec3 = 24.10, hora = "01:44 pm")
        )
    }

    fun isMockMode(): Boolean = USAR_MOCK
}