import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import db.DatabaseManager
import db.repository.User
import ui.AuthScreen
import ui.CarMaintenanceApp
import ui.CarMaintenanceTheme

fun main() = application {
    var databaseInitializedSuccessfully = false

    try {
        DatabaseManager.init()
        databaseInitializedSuccessfully = true
        println("DatabaseManager.init() llamado y completado exitosamente desde main().")
    } catch (e: Exception) {
        System.err.println("FALLO CRÍTICO AL INICIALIZAR LA BASE DE DATOS desde main(): ${e.message}")
        e.printStackTrace()
    }

    if (databaseInitializedSuccessfully) {
        var isDarkMode by remember { mutableStateOf(true) }
        var currentUser by remember { mutableStateOf<User?>(null) }

        val windowState = rememberWindowState(width = 1450.dp, height = 950.dp)
        Window(
            onCloseRequest = ::exitApplication,
            title = if (currentUser == null) "CarLog - Login" else "CarLog",
            state = windowState
        ) {
            CarMaintenanceTheme(isDarkMode = isDarkMode) {
                if (currentUser == null) {
                    AuthScreen(onLoginSuccess = { user ->
                        currentUser = user
                    })
                } else {
                    CarMaintenanceApp(
                        currentUser = currentUser!!,
                        onLogout = { currentUser = null },
                        isDarkMode = isDarkMode,
                        onToggleTheme = { isDarkMode = !isDarkMode }
                    )
                }
            }
        }
    } else {
        Window(
            onCloseRequest = ::exitApplication,
            title = "Error de Aplicación - CarLog",
            state = rememberWindowState(width = 600.dp, height = 300.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Error crítico: No se pudo inicializar la base de datos.\n" +
                            "La aplicación no puede continuar.\n" +
                            "Por favor, revisa la consola para más detalles del error.",
                    textAlign = TextAlign.Center,
                    fontSize = 16.sp,
                    color = Color.Red
                )
            }
        }
    }
}