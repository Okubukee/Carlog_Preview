package ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.unit.dp
import db.repository.CarRepository
import models.Car
import models.DriverProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehiclesCrudScreen(
    userId: String,
    cars: List<Car>,
    driverProfiles: List<DriverProfile>,
    activeProfileId: String?,
    onCarsChanged: (List<Car>) -> Unit,
    onVehicleSelected: (Car) -> Unit
) {
    var localCars by remember { mutableStateOf(cars) }
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var editCar by remember { mutableStateOf<Car?>(null) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var carToDelete by remember { mutableStateOf<Car?>(null) }
    var showMaxSecondariesWarning by remember { mutableStateOf(false) }

    // Fields to add
    var brand by remember { mutableStateOf("") }
    var model by remember { mutableStateOf("") }
    var yearText by remember { mutableStateOf("") }
    var plate by remember { mutableStateOf("") }
    var kmText by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf<String?>(null) }
    var nextServiceDate by remember { mutableStateOf<String?>(null) }
    var color by remember { mutableStateOf("") }
    // Dropdown options
    val transmissionTypes = listOf("Manual", "Automática", "CVT", "Secuencial", "Otro")
    val fuelTypes = listOf("Gasolina", "Diesel", "Híbrido", "Eléctrico", "GLP", "GNC", "Otro")
    var transmission by remember { mutableStateOf("") }
    var fuelType by remember { mutableStateOf("") }
    var purchaseDate by remember { mutableStateOf<String?>(null) }
    var selectedDriverIdForNew by remember { mutableStateOf<String?>(null) }

    // Fields to edit (populated when the dialog box opens)
    var eBrand by remember { mutableStateOf("") }
    var eModel by remember { mutableStateOf("") }
    var eYearText by remember { mutableStateOf("") }
    var ePlate by remember { mutableStateOf("") }
    var eKmText by remember { mutableStateOf("") }
    var eImageUrl by remember { mutableStateOf<String?>(null) }
    var eNextServiceDate by remember { mutableStateOf<String?>(null) }
    var eColor by remember { mutableStateOf("") }
    var eTransmission by remember { mutableStateOf("") }
    var eFuelType by remember { mutableStateOf("") }
    var ePurchaseDate by remember { mutableStateOf<String?>(null) }
    var eSelectedDriverId by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()

    fun createCarAndRefresh() {
        scope.launch {
            try {
                val year = yearText.toIntOrNull() ?: 0
                val km = kmText.toIntOrNull() ?: 0
                val newCar = Car(
                    id = UUID.randomUUID().toString(),
                    brand = brand,
                    model = model,
                    year = year,
                    plate = plate,
                    km = km,
                    imageUrl = imageUrl,
                    nextServiceDate = nextServiceDate,
                    color = color,
                    transmission = transmission,
                    fuelType = fuelType,
                    purchaseDate = purchaseDate,
                    driverProfileId = selectedDriverIdForNew,
                    secondaryDriverIds = emptyList()
                )
                withContext(Dispatchers.IO) { CarRepository.addCar(newCar, userId) }
                val refreshed = withContext(Dispatchers.IO) { CarRepository.getAllCars(userId) }
                localCars = refreshed
                onCarsChanged(refreshed)
                
                brand = ""; model = ""; yearText = ""; plate = ""; kmText = ""; imageUrl = null; nextServiceDate = null; color = ""; transmission = ""; fuelType = ""; purchaseDate = null; selectedDriverIdForNew = null
               
                showAddDialog = false
            } catch (e: Exception) {
                System.err.println("createCarAndRefresh: ERROR: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    LaunchedEffect(cars) { localCars = cars }

    val carsForDisplay = remember(localCars, activeProfileId) {
        if (activeProfileId != null) localCars.filter { it.driverProfileId == activeProfileId } else localCars
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
            Text("Gestión de vehículos", style = MaterialTheme.typography.headlineMedium)
            FilledTonalButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Filled.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Añadir vehículo")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Text("Conductores del usuario", style = MaterialTheme.typography.titleMedium)
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            if (driverProfiles.isEmpty()) {
                item { Text("No hay conductores creados", color = MaterialTheme.colorScheme.onSurfaceVariant) }
            } else {
                items(driverProfiles) { p ->
                    AssistChip(
                        onClick = {},
                        label = { Text(p.name) },
                        leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(carsForDisplay) { car ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.DirectionsCar, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text("${car.brand} ${car.model}", style = MaterialTheme.typography.titleMedium)
                                    Text("${car.year} • ${car.plate} • ${car.km} km", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(onClick = { onVehicleSelected(car) }) { Text("Ver detalles") }
                                OutlinedButton(onClick = {
                                    editCar = car
                                    eBrand = car.brand
                                    eModel = car.model
                                    eYearText = car.year.toString()
                                    ePlate = car.plate
                                    eKmText = car.km.toString()
                                    eImageUrl = car.imageUrl
                                    eNextServiceDate = car.nextServiceDate
                                    eColor = car.color
                                    eTransmission = car.transmission
                                    eFuelType = car.fuelType
                                    ePurchaseDate = car.purchaseDate
                                    eSelectedDriverId = car.driverProfileId
                                    showEditDialog = true
                                }) {
                                    Icon(Icons.Filled.Edit, contentDescription = null)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Editar")
                                }
                                // Delete button opens confirmation dialog
                                IconButton(onClick = {
                                    carToDelete = car
                                    showDeleteConfirm = true
                                }) { Icon(Icons.Filled.Delete, contentDescription = "Eliminar") }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        // Show primary driver + up to 3 secondary drivers
                        val primaryDriver = car.driverProfileId?.let { id -> driverProfiles.firstOrNull { it.id == id } }
                        val secondaryDrivers = car.secondaryDriverIds.mapNotNull { id -> driverProfiles.firstOrNull { it.id == id } }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (primaryDriver != null) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Filled.Person,
                                            contentDescription = null,
                                            modifier = Modifier
                                                .size(48.dp)
                                                .clip(MaterialTheme.shapes.small),
                                            tint = MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(primaryDriver.name, style = MaterialTheme.typography.bodyMedium)
                                            Text("Conductor principal", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                } else {
                                    Text("Sin conductor principal", style = MaterialTheme.typography.bodyMedium)
                                }

                                // Secondaries inline, smaller and spaced to the right of primary
                                if (secondaryDrivers.isNotEmpty()) {
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        secondaryDrivers.take(3).forEach { sd ->
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Filled.Person,
                                                    contentDescription = null,
                                                    modifier = Modifier
                                                        .size(36.dp)
                                                        .clip(MaterialTheme.shapes.small),
                                                    tint = MaterialTheme.colorScheme.onSurface
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(sd.name, style = MaterialTheme.typography.bodySmall, maxLines = 1)
                                            }
                                        }
                                    }
                                }
                            }

                            // Control to change main and secondary conductors
                            Row {
                                var openMenu by remember(car.id) { mutableStateOf(false) }
                                Box {
                                    OutlinedButton(onClick = { openMenu = true }) {
                                        Icon(Icons.Filled.Person, contentDescription = null)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Cambiar conductor")
                                    }
                                    DropdownMenu(expanded = openMenu, onDismissRequest = { openMenu = false }) {
                                        DropdownMenuItem(text = { Text("Sin conductor") }, onClick = {
                                            openMenu = false
                                            scope.launch {
                                                try {
                                                    val updatedCar = car.copy(driverProfileId = null)
                                                    withContext(Dispatchers.IO) { CarRepository.updateCar(updatedCar) }
                                                    val refreshed = withContext(Dispatchers.IO) { CarRepository.getAllCars(userId) }
                                                    localCars = refreshed
                                                    onCarsChanged(refreshed)
                                                } catch (_: Exception) { }
                                            }
                                        })
                                        driverProfiles.forEach { profile ->
                                            DropdownMenuItem(text = { Text(profile.name) }, onClick = {
                                                openMenu = false
                                                scope.launch {
                                                    try {
                                                        val updatedCar = car.copy(driverProfileId = profile.id)
                                                        withContext(Dispatchers.IO) { CarRepository.updateCar(updatedCar) }
                                                        val refreshed = withContext(Dispatchers.IO) { CarRepository.getAllCars(userId) }
                                                        localCars = refreshed
                                                        onCarsChanged(refreshed)
                                                    } catch (_: Exception) { }
                                                }
                                            })
                                        }
                                    }
                                }

                                var openSecMenu by remember(car.id) { mutableStateOf(false) }
                                Spacer(modifier = Modifier.width(8.dp))
                                OutlinedButton(onClick = { openSecMenu = true }) { Text("Secundarios") }
                                DropdownMenu(expanded = openSecMenu, onDismissRequest = { openSecMenu = false }) {
                                    driverProfiles.forEach { profile ->
                                        val assigned = car.secondaryDriverIds.contains(profile.id)
                                        DropdownMenuItem(text = { Text((if (assigned) "Eliminar " else "Añadir ") + profile.name) }, onClick = {
                                            openSecMenu = false
                                            if (!assigned && car.secondaryDriverIds.size >= 3) {
                                                showMaxSecondariesWarning = true
                                            } else {
                                                scope.launch {
                                                    try {
                                                        val newSecondaries = if (assigned) car.secondaryDriverIds - profile.id else (car.secondaryDriverIds + profile.id).distinct()
                                                        val updatedCar = car.copy(secondaryDriverIds = newSecondaries)
                                                        withContext(Dispatchers.IO) { CarRepository.updateCar(updatedCar) }
                                                        val refreshed = withContext(Dispatchers.IO) { CarRepository.getAllCars(userId) }
                                                        localCars = refreshed
                                                        onCarsChanged(refreshed)
                                                    } catch (_: Exception) { }
                                                }
                                            }
                                        })
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Vehicle Dialog
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            confirmButton = {
                TextButton(onClick = { createCarAndRefresh() }) { Text("Guardar") }
            },
            dismissButton = { TextButton(onClick = { showAddDialog = false }) { Text("Cancelar") } },
            title = { Text("Añadir vehículo") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = brand, onValueChange = { brand = it }, label = { Text("Marca") })
                    OutlinedTextField(value = model, onValueChange = { model = it }, label = { Text("Modelo") })
                    OutlinedTextField(value = yearText, onValueChange = { yearText = it }, label = { Text("Año") })
                    OutlinedTextField(value = plate, onValueChange = { plate = it }, label = { Text("Matrícula") })
                    OutlinedTextField(value = kmText, onValueChange = { kmText = it }, label = { Text("Kilómetros") })
                    OutlinedTextField(value = color, onValueChange = { color = it }, label = { Text("Color") })

                    // Transmission dropdown
                    var transmissionExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(expanded = transmissionExpanded, onExpandedChange = { transmissionExpanded = it }) {
                        OutlinedTextField(
                            value = transmission,
                            onValueChange = { transmission = it },
                            label = { Text("Transmisión") },
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = transmissionExpanded) },
                            modifier = Modifier.menuAnchor()
                        )
                        ExposedDropdownMenu(expanded = transmissionExpanded, onDismissRequest = { transmissionExpanded = false }) {
                            transmissionTypes.forEach { option ->
                                DropdownMenuItem(text = { Text(option) }, onClick = { transmission = option; transmissionExpanded = false })
                            }
                        }
                    }

                    // Fuel type dropdown
                    var fuelExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(expanded = fuelExpanded, onExpandedChange = { fuelExpanded = it }) {
                        OutlinedTextField(
                            value = fuelType,
                            onValueChange = { fuelType = it },
                            label = { Text("Combustible") },
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = fuelExpanded) },
                            modifier = Modifier.menuAnchor()
                        )
                        ExposedDropdownMenu(expanded = fuelExpanded, onDismissRequest = { fuelExpanded = false }) {
                            fuelTypes.forEach { option ->
                                DropdownMenuItem(text = { Text(option) }, onClick = { fuelType = option; fuelExpanded = false })
                            }
                        }
                    }

                    OutlinedTextField(value = imageUrl ?: "", onValueChange = { imageUrl = it }, label = { Text("Imagen (URL)") })
                    OutlinedTextField(value = nextServiceDate ?: "", onValueChange = { nextServiceDate = it }, label = { Text("Próximo servicio (YYYY-MM-DD)") })
                    OutlinedTextField(value = purchaseDate ?: "", onValueChange = { purchaseDate = it }, label = { Text("Fecha de compra (YYYY-MM-DD)") })

                    // Selector de conductor
                    var openMenu by remember { mutableStateOf(false) }
                    Box {
                        OutlinedButton(onClick = { openMenu = true }) {
                            Icon(Icons.Filled.Person, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            val selectedName = selectedDriverIdForNew?.let { id -> driverProfiles.firstOrNull { it.id == id }?.name } ?: "Sin conductor"
                            Text(selectedName)
                        }
                        DropdownMenu(expanded = openMenu, onDismissRequest = { openMenu = false }) {
                            DropdownMenuItem(text = { Text("Sin conductor") }, onClick = { selectedDriverIdForNew = null; openMenu = false })
                            driverProfiles.forEach { profile ->
                                DropdownMenuItem(text = { Text(profile.name) }, onClick = { selectedDriverIdForNew = profile.id; openMenu = false })
                            }
                        }
                    }
                }
            }
        )
    }

    // Edit vehicle dialog
    if (showEditDialog && editCar != null) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        try {
                            val year = eYearText.toIntOrNull() ?: editCar!!.year
                            val km = eKmText.toIntOrNull() ?: editCar!!.km
                            val updated = editCar!!.copy(
                                brand = eBrand,
                                model = eModel,
                                year = year,
                                plate = ePlate,
                                km = km,
                                imageUrl = eImageUrl,
                                nextServiceDate = eNextServiceDate,
                                color = eColor,
                                transmission = eTransmission,
                                fuelType = eFuelType,
                                purchaseDate = ePurchaseDate,
                                driverProfileId = eSelectedDriverId
                            )
                            withContext(Dispatchers.IO) { CarRepository.updateCar(updated) }
                            val refreshed = withContext(Dispatchers.IO) { CarRepository.getAllCars(userId) }
                            localCars = refreshed
                            onCarsChanged(refreshed)
                            showEditDialog = false
                        } catch (_: Exception) { }
                    }
                }) { Text("Guardar cambios") }
            },
            dismissButton = { TextButton(onClick = { showEditDialog = false }) { Text("Cancelar") } },
            title = { Text("Editar vehículo") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = eBrand, onValueChange = { eBrand = it }, label = { Text("Marca") })
                    OutlinedTextField(value = eModel, onValueChange = { eModel = it }, label = { Text("Modelo") })
                    OutlinedTextField(value = eYearText, onValueChange = { eYearText = it }, label = { Text("Año") })
                    OutlinedTextField(value = ePlate, onValueChange = { ePlate = it }, label = { Text("Matrícula") })
                    OutlinedTextField(value = eKmText, onValueChange = { eKmText = it }, label = { Text("Kilómetros") })
                    OutlinedTextField(value = eColor, onValueChange = { eColor = it }, label = { Text("Color") })
                    OutlinedTextField(value = eTransmission, onValueChange = { eTransmission = it }, label = { Text("Transmisión") })
                    OutlinedTextField(value = eFuelType, onValueChange = { eFuelType = it }, label = { Text("Combustible") })
                    OutlinedTextField(value = eImageUrl ?: "", onValueChange = { eImageUrl = it }, label = { Text("Imagen (URL)") })
                    OutlinedTextField(value = eNextServiceDate ?: "", onValueChange = { eNextServiceDate = it }, label = { Text("Próximo servicio (YYYY-MM-DD)") })
                    OutlinedTextField(value = ePurchaseDate ?: "", onValueChange = { ePurchaseDate = it }, label = { Text("Fecha de compra (YYYY-MM-DD)") })
                    // Driver selector
                    var openMenu by remember { mutableStateOf(false) }
                    Box {
                        OutlinedButton(onClick = { openMenu = true }) {
                            Icon(Icons.Filled.Person, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            val selectedName = eSelectedDriverId?.let { id -> driverProfiles.firstOrNull { it.id == id }?.name } ?: "Sin conductor"
                            Text(selectedName)
                        }
                        DropdownMenu(expanded = openMenu, onDismissRequest = { openMenu = false }) {
                            DropdownMenuItem(text = { Text("Sin conductor") }, onClick = { eSelectedDriverId = null; openMenu = false })
                            driverProfiles.forEach { profile ->
                                DropdownMenuItem(text = { Text(profile.name) }, onClick = { eSelectedDriverId = profile.id; openMenu = false })
                            }
                        }
                    }
                }
            }
        )
    }

    // Confirm delete dialog
    if (showDeleteConfirm && carToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false; carToDelete = null },
            title = { Text("Eliminar vehículo") },
            text = { Text("¿Estás seguro que deseas eliminar el vehículo ${carToDelete!!.brand} ${carToDelete!!.model}? Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        try {
                            withContext(Dispatchers.IO) { CarRepository.deleteCar(carToDelete!!.id) }
                            val updated = withContext(Dispatchers.IO) { CarRepository.getAllCars(userId) }
                            localCars = updated
                            onCarsChanged(updated)
                        } catch (e: Exception) {
                            System.err.println("deleteCar: ERROR: ${e.message}")
                            e.printStackTrace()
                        } finally {
                            showDeleteConfirm = false
                            carToDelete = null
                        }
                    }
                }) { Text("Eliminar") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false; carToDelete = null }) { Text("Cancelar") }
            }
        )
    }

    // Warning: max 3 secondaries
    if (showMaxSecondariesWarning) {
        AlertDialog(
            onDismissRequest = { showMaxSecondariesWarning = false },
            title = { Text("Límite alcanzado") },
            text = { Text("No puedes asignar más de 3 conductores secundarios a un vehículo.") },
            confirmButton = { TextButton(onClick = { showMaxSecondariesWarning = false }) { Text("OK") } }
        )
    }

}