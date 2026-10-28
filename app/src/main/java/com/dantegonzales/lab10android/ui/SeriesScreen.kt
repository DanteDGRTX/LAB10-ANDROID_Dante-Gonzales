package com.dantegonzales.lab10android.ui

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dantegonzales.lab10android.data.ApiClient
import com.dantegonzales.lab10android.data.SerieModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

private val validCategories = listOf("horror", "comedy", "action", "drama")
private val cinemaBlack = Color(0xFF111111)
private val cinemaSurface = Color(0xFF222222)
private val cinemaRed = Color(0xFFC8102E)
private val mutedText = Color(0xFFB7B7B7)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CarteleraScreen(onOpenBookshelf: () -> Unit) {
    val catalog = remember { mutableStateListOf<SerieModel>() }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var loading by remember { mutableStateOf(false) }
    var saving by remember { mutableStateOf(false) }
    var formVisible by remember { mutableStateOf(false) }
    var selectedItem by remember { mutableStateOf<SerieModel?>(null) }
    var deleteItem by remember { mutableStateOf<SerieModel?>(null) }

    suspend fun refreshCatalog() {
        loading = true
        try {
            Log.d("Lab10API", "GET /serie: cargando cartelera")
            val response = withTimeout(15_000) { ApiClient.series.getSeries() }
            if (response.isSuccessful) {
                catalog.clear()
                catalog.addAll(response.body().orEmpty())
            } else {
                snackbarHostState.showSnackbar("No se pudo cargar la cartelera (${response.code()}).")
            }
        } catch (error: Exception) {
            Log.e("Lab10API", "GET /serie: error de conexión", error)
            snackbarHostState.showSnackbar(
                "No se pudo conectar con Django: ${error.localizedMessage ?: "revise el servidor"}"
            )
        } finally {
            loading = false
        }
    }

    LaunchedEffect(Unit) { refreshCatalog() }

    Scaffold(
        containerColor = cinemaBlack,
        topBar = {
            Column {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = cinemaBlack,
                        titleContentColor = Color.White,
                    ),
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("CINE", fontWeight = FontWeight.Black, letterSpacing = 1.2.sp)
                            Text("LISTA", color = cinemaRed, fontWeight = FontWeight.Black, letterSpacing = 1.2.sp)
                        }
                    },
                    actions = {
                        TextButton(onClick = onOpenBookshelf) {
                            Text("BOOKSHELF", color = Color.White, fontSize = 10.sp)
                        }
                        TextButton(onClick = { scope.launch { refreshCatalog() } }) {
                            Text("↻", color = Color.White, fontSize = 18.sp)
                        }
                    },
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(cinemaBlack)
                        .padding(start = 18.dp, end = 18.dp, bottom = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column {
                        Text("CARTELERA", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("PELÍCULAS Y SERIES", color = mutedText, fontSize = 10.sp, letterSpacing = 1.4.sp)
                    }
                    Surface(color = Color(0xFF183C2C), shape = RoundedCornerShape(20.dp)) {
                        Text(
                            "● API CONECTADA",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            color = Color(0xFF81D6A2),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    selectedItem = null
                    formVisible = true
                },
                containerColor = cinemaRed,
                contentColor = Color.White,
                shape = RoundedCornerShape(18.dp),
            ) { Text("+", fontSize = 27.sp, fontWeight = FontWeight.Light) }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 14.dp),
        ) {
            if (loading && catalog.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = cinemaRed)
                }
            } else if (catalog.isEmpty()) {
                Column(Modifier.padding(top = 32.dp)) {
                    Text("La cartelera está vacía", color = Color.White, style = MaterialTheme.typography.titleMedium)
                    Text("Agrega un registro con + o verifica que Django esté activo.", color = mutedText)
                }
            } else {
                Text(
                    "${catalog.size} TÍTULOS DISPONIBLES",
                    modifier = Modifier.padding(top = 4.dp, bottom = 12.dp),
                    color = mutedText,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.1.sp,
                )
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(catalog, key = { it.id ?: it.name }) { item ->
                        CatalogCard(
                            item = item,
                            onEdit = {
                                val id = item.id ?: return@CatalogCard
                                scope.launch {
                                    saving = true
                                    try {
                                        val response = ApiClient.series.getSerie(id)
                                        if (response.isSuccessful && response.body() != null) {
                                            selectedItem = response.body()
                                            formVisible = true
                                        } else {
                                            snackbarHostState.showSnackbar("No se pudo consultar el título (${response.code()}).")
                                        }
                                    } catch (error: Exception) {
                                        snackbarHostState.showSnackbar(error.localizedMessage ?: "Error al consultar el título.")
                                    } finally {
                                        saving = false
                                    }
                                }
                            },
                            onDelete = { deleteItem = item },
                        )
                    }
                }
            }
        }
    }

    if (formVisible) {
        CatalogFormDialog(
            initial = selectedItem,
            saving = saving,
            onDismiss = { formVisible = false },
            onSave = { changed ->
                scope.launch {
                    saving = true
                    try {
                        val response = if (changed.id == null) {
                            ApiClient.series.createSerie(changed)
                        } else {
                            ApiClient.series.updateSerie(changed.id, changed)
                        }
                        if (response.isSuccessful) {
                            formVisible = false
                            snackbarHostState.showSnackbar(if (changed.id == null) "Título agregado a la cartelera." else "Título actualizado.")
                            refreshCatalog()
                        } else {
                            snackbarHostState.showSnackbar("Django rechazó los datos (${response.code()}). Revisa fecha y género.")
                        }
                    } catch (error: Exception) {
                        snackbarHostState.showSnackbar("No se pudo guardar: ${error.localizedMessage ?: "sin conexión"}")
                    } finally {
                        saving = false
                    }
                }
            },
        )
    }

    deleteItem?.let { target ->
        AlertDialog(
            onDismissRequest = { deleteItem = null },
            title = { Text("Retirar de cartelera") },
            text = { Text("¿Deseas eliminar ‘${target.name}’ (ID ${target.id}) del servicio web?") },
            confirmButton = {
                Button(
                    enabled = !saving,
                    onClick = {
                        val id = target.id ?: return@Button
                        scope.launch {
                            saving = true
                            try {
                                val response = ApiClient.series.deleteSerie(id)
                                if (response.isSuccessful) {
                                    deleteItem = null
                                    snackbarHostState.showSnackbar("Título eliminado de la cartelera.")
                                    refreshCatalog()
                                } else {
                                    snackbarHostState.showSnackbar("No se pudo eliminar (${response.code()}).")
                                }
                            } catch (error: Exception) {
                                snackbarHostState.showSnackbar("Error de conexión: ${error.localizedMessage ?: "sin conexión"}")
                            } finally {
                                saving = false
                            }
                        }
                    },
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = cinemaRed),
                ) { Text("Eliminar") }
            },
            dismissButton = {
                TextButton(onClick = { deleteItem = null }) { Text("Cancelar") }
            },
        )
    }
}

@Composable
private fun CatalogCard(item: SerieModel, onEdit: () -> Unit, onDelete: () -> Unit) {
    val palettes = listOf(
        listOf(Color(0xFF401B2A), Color(0xFFB12338)),
        listOf(Color(0xFF18314A), Color(0xFF487A9C)),
        listOf(Color(0xFF3B2A17), Color(0xFFB47A31)),
        listOf(Color(0xFF22213E), Color(0xFF6B5D9B)),
    )
    val palette = palettes[(item.name.hashCode().ushr(1) % palettes.size)]
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = cinemaSurface),
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.72f)
                    .background(Brush.verticalGradient(palette))
                    .padding(12.dp),
            ) {
                Column(Modifier.align(Alignment.TopStart)) {
                    Text("CINELISTA", color = Color.White.copy(alpha = 0.75f), fontSize = 9.sp, letterSpacing = 2.sp)
                    Spacer(Modifier.height(4.dp))
                    FilmPerforations()
                }
                Text(
                    text = item.name.uppercase(),
                    modifier = Modifier.align(Alignment.Center),
                    color = Color.White,
                    fontSize = 21.sp,
                    lineHeight = 24.sp,
                    fontWeight = FontWeight.Black,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis,
                )
                Column(Modifier.align(Alignment.BottomStart)) {
                    FilmPerforations()
                    Spacer(Modifier.height(6.dp))
                    Text(item.category.uppercase(), color = Color.White, fontSize = 9.sp, letterSpacing = 1.5.sp)
                }
                Surface(
                    modifier = Modifier.align(Alignment.TopEnd),
                    shape = RoundedCornerShape(6.dp),
                    color = cinemaRed,
                ) {
                    Text("${item.rating}/10", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 7.dp, vertical = 5.dp))
                }
            }
            Column(Modifier.padding(horizontal = 10.dp, vertical = 9.dp)) {
                Text(item.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(3.dp))
                Text("Estreno · ${item.releaseDate}", color = mutedText, fontSize = 10.sp, maxLines = 1)
                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    TextButton(onClick = onEdit) { Text("Editar", color = Color.White, fontSize = 11.sp) }
                    TextButton(onClick = onDelete) { Text("Eliminar", color = Color(0xFFFF8D9C), fontSize = 11.sp) }
                }
            }
        }
    }
}

@Composable
private fun FilmPerforations() {
    Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
        repeat(7) {
            Box(Modifier.size(5.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.55f)))
        }
    }
}

@Composable
private fun CatalogFormDialog(
    initial: SerieModel?,
    saving: Boolean,
    onDismiss: () -> Unit,
    onSave: (SerieModel) -> Unit,
) {
    var name by remember(initial) { mutableStateOf(initial?.name.orEmpty()) }
    var releaseDate by remember(initial) { mutableStateOf(initial?.releaseDate.orEmpty()) }
    var rating by remember(initial) { mutableStateOf(initial?.rating?.toString().orEmpty()) }
    var category by remember(initial) { mutableStateOf(initial?.category.orEmpty()) }
    var validationError by remember(initial) { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "Agregar título" else "Editar título · ID ${initial.id}") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; validationError = null },
                    label = { Text("Nombre del título") },
                    singleLine = true,
                )
                OutlinedTextField(
                    value = releaseDate,
                    onValueChange = { releaseDate = it; validationError = null },
                    label = { Text("Fecha de estreno") },
                    supportingText = { Text("Formato: AAAA-MM-DD") },
                    singleLine = true,
                )
                OutlinedTextField(
                    value = rating,
                    onValueChange = { rating = it.filter(Char::isDigit); validationError = null },
                    label = { Text("Calificación (0–10)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                )
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it.lowercase().trim(); validationError = null },
                    label = { Text("Género") },
                    supportingText = { Text(validCategories.joinToString(" · ")) },
                    singleLine = true,
                )
                validationError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
        },
        confirmButton = {
            Button(
                enabled = !saving,
                onClick = {
                    val score = rating.toIntOrNull()
                    validationError = when {
                        name.isBlank() -> "Ingresa el nombre del título."
                        !Regex("\\d{4}-\\d{2}-\\d{2}").matches(releaseDate) -> "Usa el formato AAAA-MM-DD."
                        score == null || score !in 0..10 -> "La calificación debe estar entre 0 y 10."
                        category !in validCategories -> "El género debe ser: ${validCategories.joinToString()}."
                        else -> null
                    }
                    if (validationError == null && score != null) {
                        onSave(
                            SerieModel(
                                id = initial?.id,
                                name = name.trim(),
                                releaseDate = releaseDate,
                                rating = score,
                                category = category,
                            )
                        )
                    }
                },
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = cinemaRed),
            ) { Text(if (saving) "Guardando…" else "Guardar") }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss, enabled = !saving) { Text("Cancelar") }
        },
    )
}
