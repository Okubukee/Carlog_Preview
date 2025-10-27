package ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import models.Workshop

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkshopsScreen(workshops: List<Workshop>) {
    var viewMode by remember { mutableStateOf(ViewMode.LIST) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Talleres") },
                actions = {
                    IconToggleButton(checked = viewMode == ViewMode.MAP, onCheckedChange = {
                        viewMode = if (it) ViewMode.MAP else ViewMode.LIST
                    }) {
                        Icon(if (viewMode == ViewMode.MAP) Icons.Default.List else Icons.Default.Map, contentDescription = "Cambiar vista")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            when (viewMode) {
                ViewMode.LIST -> WorkshopList(workshops)
                ViewMode.MAP -> MapViewPlaceholder()
            }
        }
    }
}

@Composable
private fun WorkshopList(workshops: List<Workshop>) {
    if (workshops.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No hay talleres disponibles")
        }
    } else {
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            items(workshops) { workshop ->
                WorkshopCard(workshop = workshop)
            }
        }
    }
}

@Composable
private fun MapViewPlaceholder() {
    Box(modifier = Modifier.fillMaxSize().background(Color.LightGray), contentAlignment = Alignment.Center) {
        Text("Vista de mapa interactivo (placeholder)", color = Color.Black)
    }
}

@Composable
fun WorkshopCard(workshop: Workshop) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(workshop.name, style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(4.dp))
            Text(workshop.location, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text(workshop.phone, style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Tarifa: ${workshop.hourlyRate} €/h", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
        }
    }
}

enum class ViewMode {
    LIST, MAP
}