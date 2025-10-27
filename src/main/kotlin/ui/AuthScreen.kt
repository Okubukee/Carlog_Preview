package ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import db.repository.User

@Composable
fun AuthScreen(onLoginSuccess: (User) -> Unit) {
    var showLogin by remember { mutableStateOf(true) }

    Surface(modifier = Modifier.fillMaxSize()) {
        Box(contentAlignment = Alignment.Center) {
            if (showLogin) {
                LoginScreen(
                    onLoginSuccess = onLoginSuccess,
                    onRegisterClick = { showLogin = false }
                )
            } else {
                RegisterScreen(
                    onRegisterSuccess = { onLoginSuccess(it) }, 
                    onBackToLogin = { showLogin = true }
                )
            }
        }
    }
}