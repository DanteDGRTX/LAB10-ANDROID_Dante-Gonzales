package com.dantegonzales.lab10android.books

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import kotlinx.coroutines.launch

private val bookshelfBackground = Color(0xFFF5F3EE)
private val bookshelfInk = Color(0xFF1D2735)
private val bookshelfBlue = Color(0xFF315C8C)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookshelfScreen(
    repository: BooksRepository = AppContainer.booksRepository,
    onBack: () -> Unit,
) {
    var query by remember { mutableStateOf("android") }
    var books by remember { mutableStateOf<List<Book>>(emptyList()) }
    var source by remember { mutableStateOf("Google Books") }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    suspend fun searchBooks() {
        val term = query.trim()
        if (term.isEmpty()) {
            error = "Escribe un tema o título para buscar."
            books = emptyList()
            return
        }
        loading = true
        error = null
        try {
            val result = repository.searchBooks(term)
            books = result.books
            source = result.source
            if (books.isEmpty()) error = "No encontramos resultados. Prueba otra búsqueda."
        } catch (exception: Exception) {
            error = "No se pudo conectar con Google Books. Revisa tu conexión e inténtalo otra vez."
        } finally {
            loading = false
        }
    }

    LaunchedEffect(Unit) { searchBooks() }

    Scaffold(
        containerColor = bookshelfBackground,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = bookshelfBackground),
                title = {
                    Column {
                        Text("BOOKSHELF", color = bookshelfInk, fontWeight = FontWeight.Black, letterSpacing = 1.6.sp)
                        Text("DESCUBRE TU PRÓXIMA LECTURA", color = bookshelfBlue, fontSize = 9.sp, letterSpacing = 1.3.sp)
                    }
                },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("‹ CINE", color = bookshelfInk, fontWeight = FontWeight.Bold) }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Busca entre millones de libros", color = bookshelfInk, style = MaterialTheme.typography.titleMedium)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    modifier = Modifier.weight(1f),
                    value = query,
                    onValueChange = { query = it },
                    label = { Text("Título, autor o tema") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { scope.launch { searchBooks() } }),
                )
                Button(onClick = { scope.launch { searchBooks() } }) { Text("Buscar") }
            }
            if (loading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        CircularProgressIndicator(color = bookshelfBlue)
                        Text("Buscando libros…", color = bookshelfInk)
                    }
                }
            } else if (error != null) {
                Text(error.orEmpty(), color = if (books.isEmpty()) bookshelfInk else bookshelfBlue)
                if (books.isEmpty()) {
                    Text("Servicio: Google Books · Retrofit + Gson", color = bookshelfBlue, fontSize = 12.sp)
                }
            } else {
                Text(
                    text = if (source == "Open Library") {
                        "${books.size} RESULTADOS · OPEN LIBRARY (RESPALDO)"
                    } else {
                        "${books.size} RESULTADOS · GOOGLE BOOKS"
                    },
                    color = bookshelfBlue,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.7.sp,
                )
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    items(books, key = { it.id }) { book ->
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            if (book.thumbnailUrl != null) {
                                AsyncImage(
                                    model = book.thumbnailUrl,
                                    contentDescription = "Portada de ${book.title}",
                                    modifier = Modifier.fillMaxWidth().aspectRatio(0.68f).clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop,
                                )
                            } else {
                                Box(
                                    modifier = Modifier.fillMaxWidth().aspectRatio(0.68f)
                                        .clip(RoundedCornerShape(8.dp)).background(bookshelfBlue),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(book.title.take(1).uppercase(), color = Color.White, fontSize = 44.sp, fontWeight = FontWeight.Black)
                                }
                            }
                            Text(book.title, color = bookshelfInk, fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                            Text(book.authors.joinToString().ifBlank { "Autor desconocido" }, color = bookshelfBlue, fontSize = 11.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            }
        }
    }
}
