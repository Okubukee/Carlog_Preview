package ui

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val AppLightColorScheme = lightColorScheme(
    primary = Color(0xFF1976D2),
    onPrimary = Color.White,
    secondary = Color(0xFF495057),
    onSecondary = Color.White,
    background = Color(0xFFF8F9FA),
    surface = Color.White,
    onBackground = Color(0xFF212529),
    onSurface = Color(0xFF212529),
    error = Color(0xFFDC3545),
    onError = Color.White,
    surfaceVariant = Color(0xFFE9ECEF),
    outline = Color(0xFFDEE2E6),
    primaryContainer = Color(0xFFBBDEFB),
    onPrimaryContainer = Color(0xFF0D47A1),
    secondaryContainer = Color(0xFFD0D0D0),
    onSecondaryContainer = Color.Black,
    tertiary = Color(0xFF00695C),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFB2DFDB),
    onTertiaryContainer = Color(0xFF004D40),
    errorContainer = Color(0xFFFDECEA),
    onErrorContainer = Color(0xFFB71C1C)
)

private val AppDarkColorScheme = darkColorScheme(
    primary = Color(0xFF90CAF9),
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF004C99),
    onPrimaryContainer = Color(0xFFD6E4FF),
    secondary = Color(0xFF80CBC4),
    onSecondary = Color(0xFF003737),
    secondaryContainer = Color(0xFF004F50),
    onSecondaryContainer = Color(0xFFB2DFDB),
    tertiary = Color(0xFFFFB74D),
    onTertiary = Color(0xFF4E3500),
    tertiaryContainer = Color(0xFF755000),
    onTertiaryContainer = Color(0xFFFFDDB8),
    background = Color(0xFF121212),
    onBackground = Color(0xFFE0E0E0),
    surface = Color(0xFF1E1E1E),
    onSurface = Color(0xFFE0E0E0),
    error = Color(0xFFCF6679),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    surfaceVariant = Color(0xFF2C2C2C),
    onSurfaceVariant = Color(0xFFC4C7C5),
    outline = Color(0xFF8E908E)
)

@Composable
fun CarMaintenanceTheme(isDarkMode: Boolean = false, content: @Composable () -> Unit) {
    val colorScheme = if (isDarkMode) AppDarkColorScheme else AppLightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(
            titleLarge = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold, fontSize = 20.sp, color = colorScheme.onSurface),
            titleMedium = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = colorScheme.onSurface),
            bodyLarge = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp, color = colorScheme.onSurface),
            bodyMedium = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp, color = colorScheme.onSurface),
            bodySmall = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, color = colorScheme.onSurface.copy(alpha = 0.7f)),
            labelSmall = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, color = colorScheme.onSurface.copy(alpha = 0.6f)),
            headlineSmall = MaterialTheme.typography.headlineSmall.copy(fontSize=24.sp, fontWeight = FontWeight.Bold, color = colorScheme.primary)
        ),
        shapes = Shapes(
            small = RoundedCornerShape(4.dp),
            medium = RoundedCornerShape(8.dp),
            large = RoundedCornerShape(12.dp)
        ),
        content = content
    )
}