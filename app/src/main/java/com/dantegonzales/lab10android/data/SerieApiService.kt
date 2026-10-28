package com.dantegonzales.lab10android.data

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

/** HTTP operations exposed by the Django REST Framework series endpoint. */
interface SerieApiService {
    @GET("serie")
    suspend fun getSeries(): Response<List<SerieModel>>

    @GET("serie/{id}")
    suspend fun getSerie(@Path("id") id: Int): Response<SerieModel>

    @POST("serie")
    suspend fun createSerie(@Body serie: SerieModel): Response<SerieModel>

    @PUT("serie/{id}")
    suspend fun updateSerie(
        @Path("id") id: Int,
        @Body serie: SerieModel,
    ): Response<SerieModel>

    @DELETE("serie/{id}")
    suspend fun deleteSerie(@Path("id") id: Int): Response<Unit>
}
