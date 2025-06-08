import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Основная тема (для главного экрана погоды)
private val WeatherDarkColorScheme = darkColorScheme(
    primary = Color(0xFF78909C), //Было Color.White
    secondary = Color(0xFFB0BEC5),
    tertiary = Color(0xFF78909C),
    surface = Color(0xFF073042),
    onSurfaceVariant = Color(0xFF85A0AC),
    onSurface = Color.White,
    onPrimary = Color.White,
    background = Color(0xFF073042)
)

// Вторая тема (для экранов настроек и городов)
private val WeatherLightColorScheme = lightColorScheme(
    primary = Color(0xFF2196F3),
    onPrimary = Color.White,
    background = Color(0xFFeaeaea),
    onBackground = Color.Black,
    surface = Color.White,
    onSurface = Color.Black
)

@Composable
fun WeatherAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    isMainScreen: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (isMainScreen) WeatherDarkColorScheme else WeatherLightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}