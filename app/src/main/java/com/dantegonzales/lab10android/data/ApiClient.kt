package com.dantegonzales.lab10android.data

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/** The Android emulator reaches the host computer through 10.0.2.2. */
object ApiClient {
    private const val BASE_URL = "http://10.0.2.2:8000/"

    val series: SerieApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SerieApiService::class.java)
    }
}
