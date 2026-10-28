package com.dantegonzales.lab10android.data

import com.google.gson.annotations.SerializedName

/** Matches the JSON representation returned by the Django REST API. */
data class SerieModel(
    @SerializedName("id") val id: Int? = null,
    @SerializedName("name") val name: String,
    @SerializedName("release_date") val releaseDate: String,
    @SerializedName("rating") val rating: Int,
    @SerializedName("category") val category: String,
)
