package ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import models.Car
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import db.repository.ExpenseItemRepository
import db.repository.CarRepository
import db.repository.InvoiceRepository
import db.repository.MaintenanceRepository
import db.repository.ReminderRepository
import models.ExpenseItem
import models.Invoice
import models.Maintenance
import models.Reminder
import models.expenseIconOptions
import java.time.LocalDate
import java.time.format.DateTimeParseException
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehicleDetailScreen(
    car: Car,
    onBackPressed: () -> Unit,
    onCarUpdated: (Car) -> Unit,
    onEditVehicle: (Car) -> Unit,
    onDeleteVehicle: (Car) -> Unit
) {
    // Dialog flags
    var showAddExpenseDialog by remember { mutableStateOf(false) }
    var showAddMaintenanceDialog by remember { mutableStateOf(false) }
    var showAddInvoiceDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalles del vehículo") },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    // Registrar menu
                    var regMenuExpanded by remember { mutableStateOf(false) }
                    Box {
                        IconButton(onClick = { regMenuExpanded = true }) {
                            Icon(Icons.Default.Edit, contentDescription = "Registrar")
                        }
                        DropdownMenu(expanded = regMenuExpanded, onDismissRequest = { regMenuExpanded = false }) {
                            DropdownMenuItem(text = { Text("Mantenimiento") }, onClick = { regMenuExpanded = false; showAddMaintenanceDialog = true })
                            DropdownMenuItem(text = { Text("Factura") }, onClick = { regMenuExpanded = false; showAddInvoiceDialog = true })
                            DropdownMenuItem(text = { Text("Gasto") }, onClick = { regMenuExpanded = false; showAddExpenseDialog = true })
                        }
                    }

                    // Actions menu (edit, delete)
                    var actionsMenuExpanded by remember { mutableStateOf(false) }
                    Box {
                        IconButton(onClick = { actionsMenuExpanded = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Más acciones")
                        }
                        DropdownMenu(expanded = actionsMenuExpanded, onDismissRequest = { actionsMenuExpanded = false }) {
                            DropdownMenuItem(
                                text = { Text("Editar vehículo") },
                                leadingIcon = { Icon(Icons.Default.Edit, null) },
                                onClick = { actionsMenuExpanded = false; onEditVehicle(car) }
                            )
                            DropdownMenuItem(
                                text = { Text("Eliminar vehículo") },
                                leadingIcon = { Icon(Icons.Default.Delete, null) },
                                onClick = { actionsMenuExpanded = false; onDeleteVehicle(car) }
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.DirectionsCar,
                    contentDescription = "Vehículo",
                    modifier = Modifier.size(100.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Vehicle information
            Text(
                text = "${car.brand} ${car.model}",
                style = MaterialTheme.typography.headlineMedium
            )
            
            Text(
                text = "Año: ${car.year}",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = 8.dp)
            )
            
            Text(
                text = "Matrícula: ${car.plate}",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = 4.dp)
            )
            
            Text(
                text = "Kilometraje: ${car.km} km",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = 4.dp)
            )
            
            Spacer(modifier = Modifier.height(24.dp))

            // --- Tabs to organize vehicle details into pages ---
            var selectedTab by remember { mutableStateOf(0) }
            val tabs = listOf("Resumen", "Chequeos", "Gastos", "Recordatorios")
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(selected = selectedTab == index, onClick = { selectedTab = index }, text = { Text(title) })
                }
            }

            // Expenses state (per-car) and global expenses (all cars) used for totals and percentage
            var expenses by remember { mutableStateOf(listOf<ExpenseItem>()) }
            var allExpenses by remember { mutableStateOf(listOf<ExpenseItem>()) }

            // load per-car expenses
            LaunchedEffect(car.id) {
                try {
                    expenses = withContext(Dispatchers.IO) { ExpenseItemRepository.getExpenseItemsByCarId(car.id) }
                } catch (_: Exception) { expenses = emptyList() }
            }

            // load all expenses (for global total / percentage)
            LaunchedEffect(Unit) {
                try {
                    allExpenses = withContext(Dispatchers.IO) { ExpenseItemRepository.getAllExpenseItems() }
                } catch (_: Exception) { allExpenses = emptyList() }
            }
            
            // Show only the selected tab's content
            val scope = rememberCoroutineScope()

            when (selectedTab) {
                0 -> {
                    // Resumen
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp), contentPadding = PaddingValues(vertical = 8.dp)) {
                        
                        item {
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("Detalles Clave", style = MaterialTheme.typography.titleLarge)
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Icon(Icons.Default.ColorLens, contentDescription = "Color", modifier = Modifier.size(28.dp))
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(car.color, style = MaterialTheme.typography.bodyLarge)
                                        }
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Icon(Icons.Default.Settings, contentDescription = "Transmisión", modifier = Modifier.size(28.dp))
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(car.transmission, style = MaterialTheme.typography.bodyLarge)
                                        }
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Icon(Icons.Default.LocalGasStation, contentDescription = "Combustible", modifier = Modifier.size(28.dp))
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(car.fuelType, style = MaterialTheme.typography.bodyLarge)
                                        }
                                    }
                                    car.purchaseDate?.let {
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                                            Icon(Icons.Default.Event, contentDescription = "Fecha de compra")
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("Comprado el: $it", style = MaterialTheme.typography.bodyMedium)
                                        }
                                    }
                                }
                            }
                        }

                        // Card for Upcoming Dates
                        item {
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("Próximas Fechas", style = MaterialTheme.typography.titleLarge)
                                    Spacer(modifier = Modifier.height(16.dp))
                                    // Next Service
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Build, contentDescription = "Próximo servicio", tint = MaterialTheme.colorScheme.primary)
                                        Spacer(modifier = Modifier.width(16.dp))
                                        Column {
                                            Text("Próximo Servicio", style = MaterialTheme.typography.titleMedium)
                                            Text(car.nextServiceDate ?: "No especificado", style = MaterialTheme.typography.bodyLarge)
                                        }
                                    }
                                    // You can add other important dates here, like ITV, insurance, etc.
                                }
                            }
                        }

                        // Card for Costs
                        item {
                            val vehicleTotal = expenses.sumOf { it.amount }
                            val today = LocalDate.now()
                            val vehicleMonthly = expenses.filter {
                                try { LocalDate.parse(it.date).year == today.year && LocalDate.parse(it.date).monthValue == today.monthValue } catch (e: DateTimeParseException) { false }
                            }.sumOf { it.amount }

                            Card(modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("Resumen de Costes", style = MaterialTheme.typography.titleLarge)
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("Este Mes", style = MaterialTheme.typography.titleMedium)
                                            Text(String.format("%.2f €", vehicleMonthly), style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
                                        }
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("Total", style = MaterialTheme.typography.titleMedium)
                                            Text(String.format("%.2f €", vehicleTotal), style = MaterialTheme.typography.headlineSmall)
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(16.dp))
                                    OutlinedButton(
                                        onClick = { selectedTab = 2 }, 
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(Icons.Default.BarChart, contentDescription = "Ver gastos detallados")
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Ver gastos detallados")
                                    }
                                }
                            }
                        }
                    }
                }

                1 -> {
                    // Quick checks
                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                            Text(text = "Chequeos rápidos", style = MaterialTheme.typography.titleLarge)
                            Spacer(modifier = Modifier.height(8.dp))

                            data class QuickCheck(
                                val id: String,
                                val label: String,
                                val intervalLabel: String, 
                                val intervalMonths: Int   
                            )
                            val defaultChecks = listOf(
                                QuickCheck("tires", "Neumáticos", "Mensual", 1),
                                QuickCheck("oil", "Aceite", "Cada 6 meses", 6),
                                QuickCheck("brakes", "Líquido de frenos", "Cada 12 meses", 12),
                                QuickCheck("lights", "Luces", "Mensual", 1),
                                QuickCheck("wipers", "Limpiaparabrisas", "Cada 6 meses", 6)
                            )

                            fun getCarLastCheckDate(checkId: String): String? {
                                return when (checkId) {
                                    "tires" -> car.lastTiresCheckDate
                                    "oil" -> car.lastOilCheckDate
                                    "brakes" -> car.lastBrakesCheckDate
                                    "lights" -> car.lastLightsCheckDate
                                    "wipers" -> car.lastWipersCheckDate
                                    else -> null
                                }
                            }

                            fun nextDateFor(lastDateStr: String?, intervalMonths: Int): String {
                                val lastDate = try {
                                    lastDateStr?.let { LocalDate.parse(it) }
                                } catch (e: DateTimeParseException) {
                                    null
                                }

                                val calculatedNextDate = if (lastDate != null) {
                                    lastDate.plusMonths(intervalMonths.toLong())
                                } else {
                                    LocalDate.now().plusMonths(intervalMonths.toLong())
                                }
                                return calculatedNextDate.toString()
                            }

                            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(defaultChecks) { qc ->
                                    val lastCheckDate = getCarLastCheckDate(qc.id)
                                    val nextText = nextDateFor(lastCheckDate, qc.intervalMonths)

                                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                                        Column {
                                            Text(qc.label, style = MaterialTheme.typography.titleMedium) 
                                            Text(qc.intervalLabel, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) 
                                        }
                                        Column(horizontalAlignment = Alignment.End) {
                                            val checked = lastCheckDate != null
                                            Checkbox(checked = checked, onCheckedChange = { isChecked ->
                                                val newDate = if (isChecked) LocalDate.now().toString() else null
                                                val newCar = when (qc.id) {
                                                    "tires" -> car.copy(lastTiresCheckDate = newDate)
                                                    "oil" -> car.copy(lastOilCheckDate = newDate)
                                                    "brakes" -> car.copy(lastBrakesCheckDate = newDate)
                                                    "lights" -> car.copy(lastLightsCheckDate = newDate)
                                                    "wipers" -> car.copy(lastWipersCheckDate = newDate)
                                                    else -> car
                                                }
                                                onCarUpdated(newCar)
                                            })

                                            val lastText = lastCheckDate ?: "-"
                                            Text("Último: $lastText", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text("Próximo: $nextText", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                2 -> {
                    // Expenses
                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "Gastos", style = MaterialTheme.typography.titleLarge)
                                FilledTonalButton(onClick = { showAddExpenseDialog = true }) {
                                    Icon(Icons.Default.AttachMoney, contentDescription = "Añadir gasto")
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Añadir gasto")
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            if (expenses.isEmpty()) {
                                Text("No hay gastos registrados para este vehículo.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            } else {
                                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(expenses) { ex ->
                                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                val iconVec = ex.icon
                                                Icon(iconVec, contentDescription = null, modifier = Modifier.size(28.dp))
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Column {
                                                    Text(ex.description, style = MaterialTheme.typography.bodyMedium)
                                                    Text(ex.date, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                }
                                            }
                                            Text(String.format("%.2f €", ex.amount), style = MaterialTheme.typography.bodyMedium)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Totals
                                val vehicleTotal = expenses.sumOf { it.amount }
                                val today = LocalDate.now()
                                val vehicleMonthly = expenses.filter {
                                    try { LocalDate.parse(it.date).year == today.year && LocalDate.parse(it.date).monthValue == today.monthValue } catch (e: DateTimeParseException) { false }
                                }.sumOf { it.amount }
                                val vehicleYearly = expenses.filter {
                                    try { LocalDate.parse(it.date).year == today.year } catch (e: DateTimeParseException) { false }
                                }.sumOf { it.amount }

                                val allTotal = allExpenses.sumOf { it.amount }
                                val percentOfAll = if (allTotal > 0.0) (vehicleTotal / allTotal * 100.0) else 0.0

                                Divider(modifier = Modifier.padding(vertical = 8.dp))
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Text("Totales", style = MaterialTheme.typography.titleMedium)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Column {
                                            Text("Total vehículo", style = MaterialTheme.typography.bodyMedium)
                                            Text(String.format("%.2f €", vehicleTotal), style = MaterialTheme.typography.bodyLarge)
                                        }
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text("% del total", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text(String.format("%.1f%%", percentOfAll), style = MaterialTheme.typography.bodyLarge)
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Column {
                                            Text("Mensual (este mes)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text(String.format("%.2f €", vehicleMonthly), style = MaterialTheme.typography.bodyMedium)
                                        }
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text("Anual (este año)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text(String.format("%.2f €", vehicleYearly), style = MaterialTheme.typography.bodyMedium)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Add expense dialog
                    if (showAddExpenseDialog) {
                        var newDesc by remember { mutableStateOf("") }
                        var newDate by remember { mutableStateOf(java.time.LocalDate.now().toString()) }
                        var newAmount by remember { mutableStateOf("") }
                        var selectedIcon by remember { mutableStateOf(expenseIconOptions.firstOrNull()?.icon ?: Icons.Default.Receipt) }

                        AlertDialog(
                            onDismissRequest = { showAddExpenseDialog = false },
                            title = { Text("Nuevo gasto") },
                            text = {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(value = newDesc, onValueChange = { newDesc = it }, label = { Text("Descripción") })
                                    OutlinedTextField(value = newDate, onValueChange = { newDate = it }, label = { Text("Fecha (YYYY-MM-DD)") })
                                    OutlinedTextField(value = newAmount, onValueChange = { newAmount = it }, label = { Text("Importe") })

                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text("Icono:")
                                        expenseIconOptions.forEach { opt ->
                                            IconButton(onClick = { selectedIcon = opt.icon }) {
                                                Icon(opt.icon, contentDescription = opt.name, tint = if (selectedIcon == opt.icon) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
                                            }
                                        }
                                    }
                                }
                            },
                            confirmButton = {
                                TextButton(onClick = {
                                    scope.launch {
                                        try {
                                            val amountVal = newAmount.toDoubleOrNull() ?: 0.0
                                            val ex = ExpenseItem(id = UUID.randomUUID().toString(), carId = car.id, description = newDesc.trim().ifBlank { "Gasto" }, date = newDate, amount = amountVal, icon = selectedIcon)
                                            withContext(Dispatchers.IO) { ExpenseItemRepository.addExpenseItem(ex) }

                                            try { expenses = withContext(Dispatchers.IO) { ExpenseItemRepository.getExpenseItemsByCarId(car.id) } } catch (_: Exception) { expenses = emptyList() }
                                            try { allExpenses = withContext(Dispatchers.IO) { ExpenseItemRepository.getAllExpenseItems() } } catch (_: Exception) { allExpenses = emptyList() }
                                        } catch (e: Exception) { System.err.println("addExpense: ${e.message}") } finally { showAddExpenseDialog = false }
                                    }
                                }) { Text("Guardar") }
                            },
                            dismissButton = { TextButton(onClick = { showAddExpenseDialog = false }) { Text("Cancelar") } }
                        )
                    }
                }

                3 -> {
                    // Reminders
                    var reminders by remember { mutableStateOf(listOf<Reminder>()) }
                    var showAddReminderDialog by remember { mutableStateOf(false) }

                    fun refreshReminders() {
                        scope.launch(Dispatchers.IO) {
                            try {
                                reminders = ReminderRepository.getRemindersByCarId(car.id)
                            } catch (e: Exception) {
                                reminders = emptyList()
                                System.err.println("Failed to load reminders: ${e.message}")
                            }
                        }
                    }

                    LaunchedEffect(car.id) {
                        refreshReminders()
                    }

                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "Recordatorios", style = MaterialTheme.typography.titleLarge)
                                FilledTonalButton(onClick = { showAddReminderDialog = true }) {
                                    Icon(Icons.Default.Add, contentDescription = "Añadir Recordatorio")
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Añadir")
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            if (reminders.isEmpty()) {
                                Text("No hay recordatorios para este vehículo.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            } else {
                                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(reminders) { reminder ->
                                        ListItem(
                                            headlineContent = { Text(reminder.title) },
                                            supportingContent = { reminder.subtitle.takeIf { it.isNotBlank() }?.let { Text(it) } },
                                            trailingContent = {
                                                IconButton(onClick = {
                                                    scope.launch(Dispatchers.IO) {
                                                        try {
                                                            ReminderRepository.deleteReminder(reminder.id)
                                                            refreshReminders()
                                                        } catch (e: Exception) {
                                                            System.err.println("Failed to delete reminder: ${e.message}")
                                                        }
                                                    }
                                                }) {
                                                    Icon(Icons.Default.Delete, contentDescription = "Eliminar recordatorio")
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (showAddReminderDialog) {
                        var newTitle by remember { mutableStateOf("") }
                        var newSubtitle by remember { mutableStateOf("") }

                        AlertDialog(
                            onDismissRequest = { showAddReminderDialog = false },
                            title = { Text("Añadir Recordatorio") },
                            text = {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(value = newTitle, onValueChange = { newTitle = it }, label = { Text("Título") })
                                    OutlinedTextField(value = newSubtitle, onValueChange = { newSubtitle = it }, label = { Text("Subtítulo (opcional)") })
                                }
                            },
                            confirmButton = {
                                TextButton(onClick = {
                                    if (newTitle.isNotBlank()) {
                                        val newReminder = Reminder(
                                            id = UUID.randomUUID().toString(),
                                            carId = car.id,
                                            title = newTitle,
                                            subtitle = newSubtitle
                                        )
                                        scope.launch(Dispatchers.IO) {
                                            try {
                                                ReminderRepository.addReminder(newReminder)
                                                refreshReminders()
                                            } catch (e: Exception) {
                                                System.err.println("Failed to add reminder: ${e.message}")
                                            }
                                        }
                                        showAddReminderDialog = false
                                    }
                                }) { Text("Guardar") }
                            },
                            dismissButton = { TextButton(onClick = { showAddReminderDialog = false }) { Text("Cancelar") } }
                        )
                    }
                }
            }
        }
    }
}