package ui

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import db.repository.CarRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import models.Car
import models.DriverProfile
import db.repository.User

@Composable
fun CarMaintenanceApp(
    currentUser: User,
    onLogout: () -> Unit,
    isDarkMode: Boolean,
    onToggleTheme: () -> Unit
) {
    var selectedCar by remember { mutableStateOf<Car?>(null) }
    var showDetailsPage by remember { mutableStateOf(false) }
    var selectedDriver by remember { mutableStateOf<DriverProfile?>(null) }
    var showDriverDetails by remember { mutableStateOf(false) }
    var refreshTrigger by remember { mutableStateOf(0) }
    val scope = rememberCoroutineScope()

    Surface {
        if (showDetailsPage && selectedCar != null) {
            VehicleDetailScreen(
                car = selectedCar!!,
                onBackPressed = {
                    showDetailsPage = false
                    selectedCar = null
                    refreshTrigger++
                },
                onCarUpdated = { updatedCar ->
                    selectedCar = updatedCar
                    scope.launch(Dispatchers.IO) {
                        try {
                            CarRepository.updateCar(updatedCar)
                        } catch (e: Exception) {
                            System.err.println("Failed to update car from App: ${e.message}")
                        }
                    }
                },
                onEditVehicle = { /* TODO: Implement edit */ },
                onDeleteVehicle = { /* TODO: Implement delete */ }
            )
        } else if (showDriverDetails && selectedDriver != null) {
            DriverDetailScreen(profile = selectedDriver!!, onBackPressed = { showDriverDetails = false; selectedDriver = null })
        } else {
            MainScreen(
                userId = currentUser.id,
                refreshTrigger = refreshTrigger,
                onVehicleSelected = { car ->
                    selectedCar = car
                    showDetailsPage = true
                },
                onDriverSelected = { dp -> selectedDriver = dp; showDriverDetails = true },
                onLogout = onLogout,
                onToggleTheme = onToggleTheme,
                isDarkTheme = isDarkMode
            )
        }
    }
}