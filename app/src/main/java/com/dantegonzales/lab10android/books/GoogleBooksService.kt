package com.dantegonzales.lab10android.books

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface GoogleBooksService {
    @GET("volumes")
    suspend fun searchVolumes(
        @Query("q") query: String,
        @Query("maxResults") maxResults: Int = 12,
    ): BooksSearchResponse

    @GET("volumes/{volumeId}")
    suspend fun getVolume(@Path("volumeId") volumeId: String): BookVolumeResponse
}

object GoogleBooksClient {
    private const val BASE_URL = "https://www.googleapis.com/books/v1/"

    val service: GoogleBooksService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GoogleBooksService::class.java)
    }
}

interface OpenLibraryService {
    @GET("search.json")
    suspend fun searchBooks(
        @Query("q") query: String,
        @Query("limit") limit: Int = 12,
        @Query("fields") fields: String = "title,author_name,cover_i",
    ): OpenLibrarySearchResponse
}

object OpenLibraryClient {
    private const val BASE_URL = "https://openlibrary.org/"

    val service: OpenLibraryService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OpenLibraryService::class.java)
    }
}
