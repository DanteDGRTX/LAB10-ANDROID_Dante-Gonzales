package com.dantegonzales.lab10android.ui

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.dantegonzales.lab10android.data.ApiClient
import com.dantegonzales.lab10android.data.SerieModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

private val validCategories = listOf("horror", "comedy", "action", "drama")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeriesScreen() {
    val series = remember { mutableStateListOf<SerieModel>() }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var loading by remember { mutableStateOf(false) }
    var saving by remember { mutableStateOf(false) }
    var formVisible by remember { mutableStateOf(false) }
    var formSerie by remember { mutableStateOf<SerieModel?>(null) }
    var deleteSerie by remember { mutableStateOf<SerieModel?>(null) }

    suspend fun refreshSeries() {
        loading = true
        try {
            Log.d("Lab10API", "GET /serie: iniciando solicitud")
            val response = withTimeout(15_000) { ApiClient.series.getSeries() }
            Log.d(
                "Lab10API",
                "GET /serie -> ${response.code()}, count=${response.body()?.size}, " +
                    "contentType=${response.headers()["Content-Type"]}",
            )
            if (response.isSuccessful) {
                val receivedSeries = response.body()
                if (receivedSeries == null) {
                    snackbarHostState.showSnackbar("Django devolvió una respuesta vacía.")
                    return
                }
                series.clear()
                series.addAll(receivedSeries)
            } else {
                snackbarHostState.showSnackbar("No se pudo cargar la lista (${response.code()}).")
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

    LaunchedEffect(Unit) { refreshSeries() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Series")
                        Text(
                            "Django REST · 192.168.1.6:8000",
                            style = MaterialTheme.typography.labelSmall,
                        )
                    }
                },
                actions = {
                    TextButton(onClick = { scope.launch { refreshSeries() } }) {
                        Text("Actualizar")
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    formSerie = null
                    formVisible = true
                },
            ) { Text("+ Nueva") }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
        ) {
            Text(
                "Los cambios se guardan en el servicio web.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(vertical = 12.dp),
            )
            if (loading && series.isEmpty()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else if (series.isEmpty()) {
                Text("No hay series para mostrar. Compruebe que Django esté ejecutándose.")
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(series, key = { it.id ?: it.name }) { serie ->
                        SerieCard(
                            serie = serie,
                            onEdit = {
                                val id = serie.id ?: return@SerieCard
                                scope.launch {
                                    saving = true
                                    try {
                                        val response = ApiClient.series.getSerie(id)
                                        if (response.isSuccessful && response.body() != null) {
                                            formSerie = response.body()
                                            formVisible = true
                                        } else {
                                            snackbarHostState.showSnackbar(
                                                "No se pudo abrir la serie (${response.code()})."
                                            )
                                        }
                                    } catch (error: Exception) {
                                        snackbarHostState.showSnackbar(
                                            error.localizedMessage ?: "Error al consultar la serie."
                                        )
                                    } finally {
                                        saving = false
                                    }
                                }
                            },
                            onDelete = { deleteSerie = serie },
                        )
                    }
                }
            }
            if (loading && series.isNotEmpty()) {
                Text("Actualizando…", modifier = Modifier.padding(vertical = 6.dp))
            }
        }
    }

    if (formVisible) {
        SerieFormDialog(
            initial = formSerie,
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
                            snackbarHostState.showSnackbar(
                                if (changed.id == null) "Serie creada." else "Serie actualizada."
                            )
                            refreshSeries()
                        } else {
                            snackbarHostState.showSnackbar(
                                "Django rechazó los datos (${response.code()}). Revise la fecha y categoría."
                            )
                        }
                    } catch (error: Exception) {
                        snackbarHostState.showSnackbar(
                            "No se pudo guardar: ${error.localizedMessage ?: "sin conexión"}"
                        )
                    } finally {
                        saving = false
                    }
                }
            },
        )
    }

    deleteSerie?.let { target ->
        AlertDialog(
            onDismissRequest = { deleteSerie = null },
            title = { Text("Eliminar serie") },
            text = { Text("¿Desea eliminar ‘${target.name}’ (ID ${target.id}) del servidor?") },
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
                                    deleteSerie = null
                                    snackbarHostState.showSnackbar("Serie eliminada.")
                                    refreshSeries()
                                } else {
                                    snackbarHostState.showSnackbar(
                                        "No se pudo eliminar (${response.code()})."
                                    )
                                }
                            } catch (error: Exception) {
                                snackbarHostState.showSnackbar(
                                    "No se pudo eliminar: ${error.localizedMessage ?: "sin conexión"}"
                                )
                            } finally {
                                saving = false
                            }
                        }
                    },
                ) { Text("Eliminar") }
            },
            dismissButton = {
                TextButton(onClick = { deleteSerie = null }) { Text("Cancelar") }
            },
        )
    }
}

@Composable
private fun SerieCard(
    serie: SerieModel,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "${serie.name}  ·  ID ${serie.id ?: "nuevo"}",
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(Modifier.height(4.dp))
            Text("Estreno: ${serie.releaseDate}")
            Text("Categoría: ${serie.category}  ·  Calificación: ${serie.rating}/10")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                TextButton(onClick = onEdit) { Text("Editar") }
                TextButton(onClick = onDelete) { Text("Eliminar") }
            }
        }
    }
}

@Composable
private fun SerieFormDialog(
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
        title = { Text(if (initial == null) "Nueva serie" else "Editar serie ${initial.id}") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; validationError = null },
                    label = { Text("Nombre") },
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
                    label = { Text("Categoría") },
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
                        name.isBlank() -> "Ingrese el nombre de la serie."
                        !Regex("\\d{4}-\\d{2}-\\d{2}").matches(releaseDate) ->
                            "La fecha debe tener el formato AAAA-MM-DD."
                        score == null || score !in 0..10 -> "La calificación debe estar entre 0 y 10."
                        category !in validCategories -> "Seleccione una categoría válida."
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
            ) { Text(if (saving) "Guardando…" else "Guardar") }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss, enabled = !saving) { Text("Cancelar") }
        },
    )
}
