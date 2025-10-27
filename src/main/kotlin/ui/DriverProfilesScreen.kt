package ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import db.repository.DriverProfileRepository
import db.repository.CarRepository
import models.DriverProfile
import models.Car
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverProfilesScreen(
    userId: String,
    profiles: List<DriverProfile>,
    activeProfileId: String?,
    onSelectProfile: (String?) -> Unit,
    onProfilesChanged: (List<DriverProfile>) -> Unit,
    onInfoClicked: (DriverProfile) -> Unit
) {
    var localProfiles by remember { mutableStateOf(profiles) }
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var editProfile by remember { mutableStateOf<DriverProfile?>(null) }
    var newName by remember { mutableStateOf("") }
    var newPhotoUrl by remember { mutableStateOf("") }
    var newPhone by remember { mutableStateOf("") }
    var newEmail by remember { mutableStateOf("") }
    val licenseTypes = listOf("A", "A1", "B", "B1", "C", "C1", "D", "D1", "E", "Otro")
    var newLicenseType by remember { mutableStateOf("") }
    var editName by remember { mutableStateOf("") }
    var editPhotoUrl by remember { mutableStateOf("") }
    var editPhone by remember { mutableStateOf("") }
    var editEmail by remember { mutableStateOf("") }
    var editLicenseType by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    var carsForUser by remember { mutableStateOf(listOf<Car>()) }
    var checkedCarIds by remember { mutableStateOf(setOf<String>()) }

    LaunchedEffect(profiles) {
        localProfiles = profiles
    }

    LaunchedEffect(userId, activeProfileId) {
        carsForUser = try {
            withContext(Dispatchers.IO) { CarRepository.getAllCars(userId) }
        } catch (e: Exception) {
            emptyList()
        }
        checkedCarIds = emptySet()
    }

    fun reloadProfiles() {
        scope.launch {
            val updated = try {
                withContext(Dispatchers.IO) { DriverProfileRepository.getProfilesByUser(userId) }
            } catch (e: Exception) {
                emptyList()
            }
            localProfiles = updated
            onProfilesChanged(updated)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Conductores", style = MaterialTheme.typography.headlineMedium)
            Button(
                onClick = {
                    newName = ""
                    newPhotoUrl = ""
                    showAddDialog = true
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Icon(Icons.Filled.Person, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Añadir conductor")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(localProfiles) { profile ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Person, contentDescription = null)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(profile.name, style = MaterialTheme.typography.titleMedium)
                                if (profile.id == activeProfileId) {
                                    Text("Seleccionado", color = MaterialTheme.colorScheme.primary)
                                }
                                if (!profile.photoUrl.isNullOrBlank()) {
                                    Text(profile.photoUrl!!, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                if (!profile.phone.isNullOrBlank()) {
                                    Text("Tel: ${profile.phone}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                if (!profile.email.isNullOrBlank()) {
                                    Text("Email: ${profile.email}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                if (profile.licenseTypes.isNotEmpty()) {
                                    Text("Carnet: ${profile.licenseTypes.joinToString(", ")}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = { onSelectProfile(profile.id) }) { Text("Seleccionar") }
                            OutlinedButton(onClick = { onInfoClicked(profile) }) { Text("Info") }
                            IconButton(onClick = {
                                editProfile = profile
                                editName = profile.name
                                editPhotoUrl = profile.photoUrl ?: ""
                                editPhone = profile.phone ?: ""
                                editEmail = profile.email ?: ""
                                editLicenseType = profile.licenseTypes.joinToString(",")
                                showEditDialog = true
                            }) {
                                Icon(Icons.Filled.Edit, contentDescription = "Editar")
                            }
                            IconButton(onClick = {
                                scope.launch {
                                    try {
                                        withContext(Dispatchers.IO) { DriverProfileRepository.deleteProfile(profile.id) }
                                       
                                        if (activeProfileId == profile.id) onSelectProfile(null)
                                        reloadProfiles()
                                    } catch (_: Exception) { }
                                }
                            }) {
                                Icon(Icons.Filled.Delete, contentDescription = "Eliminar")
                            }
                        }
                    }
                }
            }
        }

        // Active profile vehicle management
        val activeProfile = localProfiles.firstOrNull { it.id == activeProfileId }
        if (activeProfile != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text("Vehículos del conductor seleccionado", style = MaterialTheme.typography.titleLarge)

            val assigned = carsForUser.filter { it.driverProfileId == activeProfile.id }
            val available = carsForUser.filter { it.driverProfileId != activeProfile.id }

            if (assigned.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Asignados", style = MaterialTheme.typography.titleMedium)
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    items(assigned) { car ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.DirectionsCar, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text("${car.brand} ${car.model}", style = MaterialTheme.typography.titleMedium)
                                        Text("${car.year} • ${car.plate}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                                OutlinedButton(onClick = {
                                    scope.launch {
                                        try {
                                            val updated = car.copy(driverProfileId = null)
                                            withContext(Dispatchers.IO) { CarRepository.updateCar(updated) }
                                            carsForUser = withContext(Dispatchers.IO) { CarRepository.getAllCars(userId) }
                                        } catch (_: Exception) { }
                                    }
                                }) { Text("Quitar") }
                            }
                        }
                    }
                }
            }

            // List of available items with multiple selection to assign
            Spacer(modifier = Modifier.height(16.dp))
            Text("Disponibles", style = MaterialTheme.typography.titleMedium)
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                items(available) { car ->
                    val checked = checkedCarIds.contains(car.id)
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(checked = checked, onCheckedChange = { isChecked ->
                                    checkedCarIds = if (isChecked) checkedCarIds + car.id else checkedCarIds - car.id
                                })
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(Icons.Filled.DirectionsCar, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text("${car.brand} ${car.model}", style = MaterialTheme.typography.titleMedium)
                                    Text("${car.year} • ${car.plate}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            OutlinedButton(onClick = {
                                scope.launch {
                                    try {
                                        val updated = car.copy(driverProfileId = activeProfile.id)
                                        withContext(Dispatchers.IO) { CarRepository.updateCar(updated) }
                                        carsForUser = withContext(Dispatchers.IO) { CarRepository.getAllCars(userId) }
                                        checkedCarIds = checkedCarIds - car.id
                                    } catch (_: Exception) { }
                                }
                            }) { Text("Asignar") }
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                Button(
                    onClick = {
                        scope.launch {
                            try {
                                val toAssign = available.filter { checkedCarIds.contains(it.id) }
                                toAssign.forEach { car ->
                                    val updated = car.copy(driverProfileId = activeProfile.id)
                                    withContext(Dispatchers.IO) { CarRepository.updateCar(updated) }
                                }
                                carsForUser = withContext(Dispatchers.IO) { CarRepository.getAllCars(userId) }
                                checkedCarIds = emptySet()
                            } catch (_: Exception) { }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    enabled = checkedCarIds.isNotEmpty()
                ) {
                    Text("Asignar seleccionados")
                }
            }
        }

        // Add driver dialog
        if (showAddDialog) {
            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                confirmButton = {
                    TextButton(onClick = {
                        scope.launch {
                            try {
                                        val newLicenses = newLicenseType.split(",").map { it.trim() }.filter { it.isNotBlank() }
                                        withContext(Dispatchers.IO) { DriverProfileRepository.createProfile(userId, newName.trim(), newPhotoUrl.trim().ifBlank { null }, newPhone.trim().ifBlank { null }, newEmail.trim().ifBlank { null }, newLicenses) }
                                showAddDialog = false
                                reloadProfiles()
                            } catch (_: Exception) { showAddDialog = false }
                        }
                    }) { Text("Guardar") }
                },
                dismissButton = {
                    TextButton(onClick = { showAddDialog = false }) { Text("Cancelar") }
                },
                title = { Text("Nuevo conductor") },
                text = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = newName,
                            onValueChange = { newName = it },
                            label = { Text("Nombre") },
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = newPhotoUrl,
                            onValueChange = { newPhotoUrl = it },
                            label = { Text("Foto URL (opcional)") },
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = newPhone,
                            onValueChange = { newPhone = it },
                            label = { Text("Teléfono (opcional)") },
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = newEmail,
                            onValueChange = { newEmail = it },
                            label = { Text("Email (opcional)") },
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        // License type multi-select
                        var licenseExpanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(expanded = licenseExpanded, onExpandedChange = { licenseExpanded = it }) {
                            OutlinedTextField(
                                value = if (newLicenseType.isBlank()) "Seleccionar tipos..." else newLicenseType,
                                onValueChange = { /* readOnly */ },
                                label = { Text("Tipos de carnet (opcional)") },
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = licenseExpanded) },
                                modifier = Modifier.menuAnchor(),
                                singleLine = true
                            )
                            ExposedDropdownMenu(expanded = licenseExpanded, onDismissRequest = { licenseExpanded = false }) {
                                licenseTypes.forEach { lt ->
                                    val parts = newLicenseType.split(",").map { it.trim() }.filter { it.isNotBlank() }
                                    val selected = parts.contains(lt)
                                    DropdownMenuItem(text = { Row { Checkbox(checked = selected, onCheckedChange = null); Spacer(Modifier.width(8.dp)); Text(lt) } }, onClick = {
                                        val mutable = parts.toMutableList()
                                        if (mutable.contains(lt)) mutable.remove(lt) else mutable.add(lt)
                                        newLicenseType = mutable.joinToString(",")
                                    })
                                }
                            }
                        }
                    }
                }
            )
        }

        // Dialog for editing the driver
        if (showEditDialog && editProfile != null) {
            AlertDialog(
                onDismissRequest = { showEditDialog = false },
                confirmButton = {
                    TextButton(onClick = {
                        val current = editProfile ?: return@TextButton
                        scope.launch {
                            try {
                                val updated = current.copy(
                                    name = editName.trim(),
                                    photoUrl = editPhotoUrl.trim().ifBlank { null },
                                    phone = editPhone.trim().ifBlank { null },
                                    email = editEmail.trim().ifBlank { null },
                                    licenseTypes = editLicenseType.split(",").map { it.trim() }.filter { it.isNotBlank() }
                                )
                                withContext(Dispatchers.IO) { DriverProfileRepository.updateProfile(updated) }
                                showEditDialog = false
                                reloadProfiles()
                            } catch (_: Exception) { showEditDialog = false }
                        }
                    }) { Text("Guardar") }
                },
                dismissButton = {
                    TextButton(onClick = { showEditDialog = false }) { Text("Cancelar") }
                },
                title = { Text("Editar conductor") },
                text = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = editName,
                            onValueChange = { editName = it },
                            label = { Text("Nombre") },
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = editPhotoUrl,
                            onValueChange = { editPhotoUrl = it },
                            label = { Text("Foto URL (opcional)") },
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = editPhone,
                            onValueChange = { editPhone = it },
                            label = { Text("Teléfono (opcional)") },
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = editEmail,
                            onValueChange = { editEmail = it },
                            label = { Text("Email (opcional)") },
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        var eLicenseExpanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(expanded = eLicenseExpanded, onExpandedChange = { eLicenseExpanded = it }) {
                            OutlinedTextField(
                                value = if (editLicenseType.isBlank()) "Seleccionar tipos..." else editLicenseType,
                                onValueChange = { /* readOnly */ },
                                label = { Text("Tipos de carnet (opcional)") },
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = eLicenseExpanded) },
                                modifier = Modifier.menuAnchor(),
                                singleLine = true
                            )
                            ExposedDropdownMenu(expanded = eLicenseExpanded, onDismissRequest = { eLicenseExpanded = false }) {
                                licenseTypes.forEach { lt ->
                                    val parts = editLicenseType.split(",").map { it.trim() }.filter { it.isNotBlank() }
                                    val selected = parts.contains(lt)
                                    DropdownMenuItem(text = { Row { Checkbox(checked = selected, onCheckedChange = null); Spacer(Modifier.width(8.dp)); Text(lt) } }, onClick = {
                                        val mutable = parts.toMutableList()
                                        if (mutable.contains(lt)) mutable.remove(lt) else mutable.add(lt)
                                        editLicenseType = mutable.joinToString(",")
                                    })
                                }
                            }
                        }
                    }
                }
            )
        }
    }
}