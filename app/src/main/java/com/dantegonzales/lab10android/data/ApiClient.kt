package com.dantegonzales.lab10android.data

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/** Use the PC's reachable LAN address for this emulator and physical devices. */
object ApiClient {
    private const val BASE_URL = "http://192.168.1.6:8000/"

    val series: SerieApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SerieApiService::class.java)
    }
}
