package ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import db.repository.*
import models.*
import ui.WorkshopsScreen


data class CheckItem(val name: String, val done: Boolean)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    userId: String,
    refreshTrigger: Int,
    onVehicleSelected: (Car) -> Unit,
    onDriverSelected: (DriverProfile) -> Unit,
    onLogout: () -> Unit,
    onToggleTheme: () -> Unit,
    isDarkTheme: Boolean
) {
    var selectedTabInMain by remember { mutableStateOf(0) }
    var showDropdownMenu by remember { mutableStateOf(false) }
    var cars by remember { mutableStateOf(listOf<Car>()) }
    var workshops by remember { mutableStateOf(listOf<Workshop>()) }
    var driverProfiles by remember { mutableStateOf(listOf<DriverProfile>()) }
    var activeProfileId by remember { mutableStateOf<String?>(null) }
    var selectedCarInDashboard by remember { mutableStateOf<Car?>(null) }

    LaunchedEffect(Unit) { 
        try {
            workshops = WorkshopRepository.getAllWorkshops()
        } catch (e: Exception) {
            workshops = emptyList()
        }
        try {
            driverProfiles = DriverProfileRepository.getProfilesByUser(userId)
        } catch (e: Exception) {
            driverProfiles = emptyList()
        }
    }

    LaunchedEffect(activeProfileId, selectedTabInMain, refreshTrigger) {
        try {
            val loadedCars = CarRepository.getAllCars(userId)
            cars = loadedCars
            if (selectedCarInDashboard == null || !loadedCars.contains(selectedCarInDashboard)) {
                selectedCarInDashboard = loadedCars.firstOrNull()
            }
        } catch (_: Exception) { 
            cars = emptyList()
            selectedCarInDashboard = null
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("CarLog") },
                actions = {
                    IconButton(onClick = { showDropdownMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Más opciones")
                    }
                    DropdownMenu(
                        expanded = showDropdownMenu,
                        onDismissRequest = { showDropdownMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Cambiar tema") },
                            leadingIcon = { Icon(if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode, "Cambiar tema") },
                            onClick = {
                                onToggleTheme()
                                showDropdownMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Soporte") },
                            leadingIcon = { Icon(Icons.Default.Help, "Soporte") },
                            onClick = { showDropdownMenu = false }
                        )
                        DropdownMenuItem(
                            text = { Text("Cerrar sesión") },
                            leadingIcon = { Icon(Icons.AutoMirrored.Filled.Logout, "Cerrar sesión") },
                            onClick = {
                                onLogout()
                                showDropdownMenu = false
                            }
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            if (selectedTabInMain == 0) {
                ExpandableFAB()
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            TabRow(selectedTabIndex = selectedTabInMain) {
                Tab(selected = selectedTabInMain == 0, onClick = { selectedTabInMain = 0 }, text = { Text("Panel") }, icon = { Icon(Icons.Default.Dashboard, null) })
                Tab(selected = selectedTabInMain == 1, onClick = { selectedTabInMain = 1 }, text = { Text("Vehículos") }, icon = { Icon(Icons.Default.DirectionsCar, null) })
                Tab(selected = selectedTabInMain == 2, onClick = { selectedTabInMain = 2 }, text = { Text("Talleres") }, icon = { Icon(Icons.Default.Build, null) })
                Tab(selected = selectedTabInMain == 3, onClick = { selectedTabInMain = 3 }, text = { Text("Conductores") }, icon = { Icon(Icons.Default.Person, null) })
            }

            when (selectedTabInMain) {
                0 -> {
                    val carsForDisplay = if (activeProfileId != null) cars.filter { it.driverProfileId == activeProfileId } else cars
                    DashboardPanel(
                        cars = carsForDisplay,
                        selectedCar = selectedCarInDashboard,
                        onCarSelected = { selectedCarInDashboard = it },
                        onVehicleSelected = onVehicleSelected
                    )
                }
                1 -> VehiclesCrudScreen(userId, cars, driverProfiles, activeProfileId, { updated -> cars = updated }, onVehicleSelected)
                2 -> WorkshopsScreen(workshops)
                3 -> DriverProfilesScreen(userId, driverProfiles, activeProfileId, { id -> activeProfileId = id }, { updated -> driverProfiles = updated }, { dp -> onDriverSelected(dp) })
            }
        }
    }
}

@Composable
fun DashboardPanel(
    cars: List<Car>,
    selectedCar: Car?,
    onCarSelected: (Car) -> Unit,
    onVehicleSelected: (Car) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            VehicleSelectorAndWallet(
                cars = cars,
                selectedCar = selectedCar,
                onCarSelected = onCarSelected,
                onWalletClicked = { 
                    selectedCar?.let { onVehicleSelected(it) } 
                }
            )
        }

        item {
            AlertsCard(
                alerts = listOf( 
                    "Vencimiento ITV en 25 días",
                    "Seguro vence el 15/11/2025",
                    "Mantenimiento B pendiente"
                )
            )
        }

        item {
            MileageCard(km = selectedCar?.km ?: 0, onUpdateMileage = { /* TODO */ })
        }

        item {
            val checks = listOf(
                CheckItem("Nivel de aceite", true),
                CheckItem("Presión de neumáticos", true),
                CheckItem("Nivel de refrigerante", true),
                CheckItem("Luces", false),
                CheckItem("Líquido de frenos", false)
            )
            ChecksAndExpensesSection(monthlyExpense = 350.50f, yearlyExpense = 2800.75f, checks = checks)
        }

        item {
            RecentActivityFeed(
                activities = listOf( 
                    "Último repostaje: 45L - 65.20€",
                    "Último servicio: Cambio de aceite y filtros"
                )
            )
        }
        
        item { Spacer(modifier = Modifier.height(64.dp)) } 
    }
}

@Composable
fun MileageCard(km: Int, onUpdateMileage: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("Kilometraje Actual", style = MaterialTheme.typography.titleMedium)
                Text("$km km", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            }
            Button(onClick = onUpdateMileage) {
                Icon(Icons.Default.Edit, null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Actualizar")
            }
        }
    }
}

@Composable
fun ChecksAndExpensesSection(monthlyExpense: Float, yearlyExpense: Float, checks: List<CheckItem>) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Box(Modifier.weight(1f)) {
            QuickChecksCard(checks = checks)
        }
        Box(Modifier.weight(1f)) {
            ExpenseDonutChart(monthlyExpense = monthlyExpense, yearlyExpense = yearlyExpense)
        }
    }
}

@Composable
fun ExpenseDonutChart(monthlyExpense: Float, yearlyExpense: Float) {
    Card(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Resumen de Gastos", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(16.dp))
            DonutChart(listOf(monthlyExpense, yearlyExpense - monthlyExpense), listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary))
            Spacer(modifier = Modifier.height(16.dp))
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(10.dp).background(MaterialTheme.colorScheme.primary, CircleShape))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Este Mes: %.2f €".format(monthlyExpense), fontSize = 14.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(10.dp).background(MaterialTheme.colorScheme.secondary, CircleShape))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Resto del Año: %.2f €".format(yearlyExpense - monthlyExpense), fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
fun DonutChart(values: List<Float>, colors: List<Color>, strokeWidth: Float = 20f) {
    val total = values.sum()
    var startAngle = -90f

    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(120.dp)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            values.forEachIndexed { index, value ->
                val sweepAngle = (value / total) * 360f
                drawArc(
                    color = colors[index],
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    style = Stroke(width = strokeWidth)
                )
                startAngle += sweepAngle
            }
        }
        Text("%.2f €".format(total), fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }
}

@Composable
fun QuickChecksCard(checks: List<CheckItem>) {
    var expanded by remember { mutableStateOf(false) }
    var showOnlyPending by remember { mutableStateOf(true) }
    val completed = checks.count { it.done }
    val total = checks.size

    Card(modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded }) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Chequeos Rápidos", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { completed.toFloat() / total.toFloat() },
                        modifier = Modifier.fillMaxWidth(0.9f)
                    )
                }
                Text("$completed/$total", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }

            AnimatedVisibility(expanded) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { showOnlyPending = !showOnlyPending }) {
                        Checkbox(checked = showOnlyPending, onCheckedChange = { showOnlyPending = it })
                        Text("Mostrar solo pendientes")
                    }
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    val itemsToShow = if (showOnlyPending) checks.filter { !it.done } else checks
                    if (itemsToShow.isEmpty()) {
                        Text("¡Todo al día!", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    } else {
                        itemsToShow.forEach { check ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(if (check.done) Icons.Default.CheckBox else Icons.Default.CheckBoxOutlineBlank, contentDescription = null, tint = if (check.done) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
                                Spacer(Modifier.width(8.dp))
                                Text(check.name)
                            }
                        }
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehicleSelectorAndWallet(
    cars: List<Car>,
    selectedCar: Car?,
    onCarSelected: (Car) -> Unit,
    onWalletClicked: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier.weight(1f)
        ) {
            TextField(
                value = selectedCar?.let { "${it.brand} ${it.model} (${it.plate})" } ?: "Ningún vehículo seleccionado",
                onValueChange = {},
                readOnly = true,
                label = { Text("Vehículo") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                colors = ExposedDropdownMenuDefaults.textFieldColors(),
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                cars.forEach { car ->
                    DropdownMenuItem(
                        text = { Text("${car.brand} ${car.model} (${car.plate})") },
                        onClick = {
                            onCarSelected(car)
                            expanded = false
                        }
                    )
                }
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        IconButton(onClick = onWalletClicked) {
            Icon(Icons.AutoMirrored.Filled.Article, contentDescription = "Cartera de Documentos", modifier = Modifier.size(32.dp))
        }
    }
}

@Composable
fun AlertsCard(alerts: List<String>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Warning, contentDescription = "Alertas", tint = MaterialTheme.colorScheme.onErrorContainer)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Alertas Críticas", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onErrorContainer)
            }
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                items(alerts) { alert ->
                    Text(alert, color = MaterialTheme.colorScheme.onErrorContainer, modifier = Modifier.padding(vertical = 4.dp))
                }
            }
        }
    }
}

@Composable
fun RecentActivityFeed(activities: List<String>) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Actividad Reciente", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 8.dp))
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                activities.forEach { activity ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.History, null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(activity, style = MaterialTheme.typography.bodyMedium)
                    }
                    if (activity != activities.last()) {
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun ExpandableFAB() {
    var isExpanded by remember { mutableStateOf(false) }
    val rotation by animateFloatAsState(if (isExpanded) 45f else 0f)

    Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        AnimatedVisibility(visible = isExpanded) {
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SmallFloatingActionButton(
                    onClick = { /* TODO: Add Expense */ },
                    containerColor = MaterialTheme.colorScheme.surface,
                ) { Icon(Icons.Default.Add, "Añadir Gasto") }

                SmallFloatingActionButton(
                    onClick = { /* TODO: Register Maintenance */ },
                    containerColor = MaterialTheme.colorScheme.surface,
                ) { Icon(Icons.Default.Build, "Registrar Mantenimiento") }

                SmallFloatingActionButton(
                    onClick = { /* TODO: Search Workshop */ },
                    containerColor = MaterialTheme.colorScheme.surface,
                ) { Icon(Icons.Default.Search, "Buscar Taller") }
            }
        }

        FloatingActionButton(
            onClick = { isExpanded = !isExpanded },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = "Acciones",
                modifier = Modifier.rotate(rotation)
            )
        }
    }
}