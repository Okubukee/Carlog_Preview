package ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import db.repository.CarRepository
import models.Car
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import models.DriverProfile

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverDetailScreen(
    profile: DriverProfile,
    onBackPressed: () -> Unit
) {
    var assignedCars by remember { mutableStateOf(listOf<Car>()) }

    LaunchedEffect(profile.id) {
        assignedCars = try {
            withContext(Dispatchers.IO) { CarRepository.getAllCarsByProfile(profile.id) }
        } catch (_: Exception) { emptyList() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalles del conductor") },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Profile Header Card
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Photo placeholder
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.secondaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Person, contentDescription = "Foto de perfil", modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onSecondaryContainer)
                        }
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text(profile.name, style = MaterialTheme.typography.headlineMedium)
                        }
                    }
                }
            }

            // Contact Info Card
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Contacto", style = MaterialTheme.typography.titleLarge)
                        profile.phone?.let {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Phone, null, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(16.dp))
                                Text(it)
                            }
                        }
                        profile.email?.let {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Email, null, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(16.dp))
                                Text(it)
                            }
                        }
                    }
                }
            }

            // Licenses Card
            if (profile.licenseTypes.isNotEmpty()) {
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CardMembership, null, modifier = Modifier.size(24.dp))
                                Spacer(Modifier.width(16.dp))
                                Text("Licencias de conducir", style = MaterialTheme.typography.titleLarge)
                            }
                            Text(
                                profile.licenseTypes.joinToString(", "),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }

            // Statistics Card
            item {
                val totalKm = assignedCars.sumOf { it.km }
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Estadísticas", style = MaterialTheme.typography.titleLarge)
                        Spacer(Modifier.height(16.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Vehículos Asignados", style = MaterialTheme.typography.titleMedium)
                                Text(assignedCars.size.toString(), style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Kilómetros Totales", style = MaterialTheme.typography.titleMedium)
                                Text("$totalKm km", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }

            // Assigned Vehicles List
            item {
                Text("Vehículos asignados", style = MaterialTheme.typography.titleLarge)
            }
            if (assignedCars.isEmpty()) {
                item {
                    Text("No hay vehículos asignados", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(start = 16.dp))
                }
            } else {
                items(assignedCars) { car ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        ListItem(
                            headlineContent = { Text("${car.brand} ${car.model}") },
                            supportingContent = { Text("${car.year} • ${car.plate}") },
                            leadingContent = { Icon(Icons.Default.DirectionsCar, null) }
                        )
                    }
                }
            }
        }
    }
}