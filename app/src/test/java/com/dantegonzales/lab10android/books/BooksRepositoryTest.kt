package com.dantegonzales.lab10android.books

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class BooksRepositoryTest {
    @Test
    fun searchBooksLoadsDetailsAndUsesSecureThumbnailUrl() = runBlocking {
        val fakeService = FakeGoogleBooksService()
        val repository = NetworkBooksRepository(fakeService)

        val books = repository.searchBooks(" android compose ")

        assertEquals("android compose", fakeService.lastQuery)
        assertEquals("Google Books", books.source)
        assertEquals(2, books.books.size)
        assertEquals("Compose Basics", books.books.first().title)
        assertEquals("https://books.example/cover.jpg", books.books.first().thumbnailUrl)
        assertEquals(listOf("vol-1", "vol-2"), fakeService.requestedVolumeIds)
    }

    @Test
    fun fallsBackWhenGoogleBooksReturnsNoResults() = runBlocking {
        val primary = object : BooksRepository {
            override suspend fun searchBooks(query: String) = BookSearchResult(emptyList(), "Google Books")
        }
        val fallbackBook = Book("fallback-1", "Open Book", emptyList(), null)
        val fallback = object : BooksRepository {
            override suspend fun searchBooks(query: String) = BookSearchResult(listOf(fallbackBook), "Open Library")
        }

        val result = BooksRepositoryWithFallback(primary, fallback).searchBooks("android")

        assertEquals("Open Library", result.source)
        assertEquals(fallbackBook, result.books.single())
    }

    private class FakeGoogleBooksService : GoogleBooksService {
        var lastQuery: String? = null
        val requestedVolumeIds = mutableListOf<String>()

        override suspend fun searchVolumes(query: String, maxResults: Int): BooksSearchResponse {
            lastQuery = query
            assertEquals(12, maxResults)
            return BooksSearchResponse(listOf(VolumeReference("vol-1"), VolumeReference("vol-2")))
        }

        override suspend fun getVolume(volumeId: String): BookVolumeResponse {
            requestedVolumeIds += volumeId
            return when (volumeId) {
                "vol-1" -> BookVolumeResponse(
                    id = volumeId,
                    volumeInfo = BookVolumeInfo(
                        title = "Compose Basics",
                        authors = listOf("Android Team"),
                        imageLinks = BookImageLinks("http://books.example/cover.jpg"),
                    ),
                )
                else -> BookVolumeResponse(
                    id = volumeId,
                    volumeInfo = BookVolumeInfo("Kotlin Guide", listOf("A. Developer"), null),
                )
            }
        }
    }
}
