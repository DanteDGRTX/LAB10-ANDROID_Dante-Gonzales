package com.dantegonzales.lab10android.books

import com.google.gson.annotations.SerializedName

data class BooksSearchResponse(
    val items: List<VolumeReference> = emptyList(),
)

data class VolumeReference(
    val id: String,
)

data class BookVolumeResponse(
    val id: String,
    @SerializedName("volumeInfo") val volumeInfo: BookVolumeInfo?,
)

data class BookVolumeInfo(
    val title: String?,
    val authors: List<String>?,
    @SerializedName("imageLinks") val imageLinks: BookImageLinks?,
)

data class BookImageLinks(
    val thumbnail: String?,
)

data class Book(
    val id: String,
    val title: String,
    val authors: List<String>,
    val thumbnailUrl: String?,
)

data class BookSearchResult(
    val books: List<Book>,
    val source: String,
)

data class OpenLibrarySearchResponse(
    val docs: List<OpenLibraryBook> = emptyList(),
)

data class OpenLibraryBook(
    val title: String?,
    @SerializedName("author_name") val authors: List<String>?,
    @SerializedName("cover_i") val coverId: Int?,
)
