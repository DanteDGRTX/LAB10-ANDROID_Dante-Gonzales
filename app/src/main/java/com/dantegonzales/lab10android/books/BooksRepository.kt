package com.dantegonzales.lab10android.books

interface BooksRepository {
    suspend fun searchBooks(query: String): BookSearchResult
}

class NetworkBooksRepository(
    private val service: GoogleBooksService,
) : BooksRepository {
    override suspend fun searchBooks(query: String): BookSearchResult {
        val references = service.searchVolumes(query.trim(), maxResults = 12).items
        val books = references.mapNotNull { reference ->
            runCatching { service.getVolume(reference.id) }.getOrNull()?.let { detail ->
                val info = detail.volumeInfo ?: return@let null
                val title = info.title?.takeIf(String::isNotBlank) ?: return@let null
                Book(
                    id = detail.id,
                    title = title,
                    authors = info.authors.orEmpty(),
                    thumbnailUrl = info.imageLinks?.thumbnail
                        ?.replace("http://", "https://"),
                )
            }
        }
        return BookSearchResult(books, "Google Books")
    }
}

class OpenLibraryBooksRepository(
    private val service: OpenLibraryService,
) : BooksRepository {
    override suspend fun searchBooks(query: String): BookSearchResult {
        val response = service.searchBooks(query.trim(), limit = 12)
        val books = response.docs.mapIndexedNotNull { index, item ->
            val title = item.title?.takeIf(String::isNotBlank) ?: return@mapIndexedNotNull null
            Book(
                id = "openlibrary-$index-${title.hashCode()}",
                title = title,
                authors = item.authors.orEmpty(),
                thumbnailUrl = item.coverId?.let { "https://covers.openlibrary.org/b/id/$it-M.jpg?default=false" },
            )
        }
        return BookSearchResult(books, "Open Library")
    }
}

class BooksRepositoryWithFallback(
    private val primary: BooksRepository,
    private val fallback: BooksRepository,
) : BooksRepository {
    override suspend fun searchBooks(query: String): BookSearchResult {
        val primaryResult = runCatching { primary.searchBooks(query) }.getOrNull()
        if (!primaryResult?.books.isNullOrEmpty()) return primaryResult!!
        return fallback.searchBooks(query)
    }
}

object AppContainer {
    val booksRepository: BooksRepository by lazy {
        BooksRepositoryWithFallback(
            primary = NetworkBooksRepository(GoogleBooksClient.service),
            fallback = OpenLibraryBooksRepository(OpenLibraryClient.service),
        )
    }
}
