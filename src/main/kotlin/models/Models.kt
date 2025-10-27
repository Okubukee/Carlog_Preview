package models

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

data class Car(
    val id: String,
    val brand: String,
    val model: String,
    val year: Int,
    val plate: String,
    val km: Int,
    val imageUrl: String?,
    val nextServiceDate: String?,
    val color: String,
    val transmission: String,
    val fuelType: String,
    val purchaseDate: String?,
    val driverProfileId: String?,
    val secondaryDriverIds: List<String> = emptyList(),
    val lastTiresCheckDate: String? = null,
    val lastOilCheckDate: String? = null,
    val lastBrakesCheckDate: String? = null,
    val lastLightsCheckDate: String? = null,
    val lastWipersCheckDate: String? = null
)

data class Workshop(
    val id: String,
    val name: String,
    val specialty: String,
    val phone: String,
    val location: String,
    val hourlyRate: Double
)

data class Maintenance(
    val id: String,
    val carId: String,
    val workshopId: String?,
    val date: String,
    val description: String,
    val cost: Double,
    val type: String,
    val km: Int
)

data class Invoice(
    val id: String,
    val maintenanceId: String,
    val date: String,
    val total: Double,
    val status: String
)

data class ExpenseItem(
    val id: String,
    val carId: String,
    val description: String,
    val date: String,
    val amount: Double,
    val icon: ImageVector
)

data class Reminder(
    val id: String,
    val carId: String,
    val title: String,
    val subtitle: String
)

data class DriverProfile(
    val id: String,
    val userId: String,
    val name: String,
    val photoUrl: String?,
    val phone: String? = null,
    val email: String? = null,
    val licenseTypes: List<String> = emptyList()
)

data class IconOption(val name: String, val icon: ImageVector)

val expenseIconOptions = listOf(
    IconOption("Combustible", Icons.Filled.LocalGasStation),
    IconOption("Seguro", Icons.Filled.Shield),
    IconOption("Lavado", Icons.Filled.Wash),
    IconOption("Mantenimiento/Reparación", Icons.Filled.Build),
    IconOption("Peaje/Parking", Icons.Filled.CreditCard),
    IconOption("Impuestos/Otros", Icons.Filled.Receipt),
    IconOption("Compra Accesorio", Icons.Filled.ShoppingCart)
)