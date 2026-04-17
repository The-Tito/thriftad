package com.polet.thriftadapp.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Definimos un esquema único basado en tu maquetado claro
private val ThriftAdColorScheme = lightColorScheme(
    primary = PurplePrimary,    // Tu morado (0xFF6A1B9A)
    background = LightBG,       // Tu gris clarito (0xFFF5F6F9)
    surface = Color.White,      // Blanco para las tarjetas (Cards)
    onPrimary = Color.White,
    onBackground = Color.Black,
    onSurface = Color.Black
)

@Composable
fun ThriftadTheme(
    // Forzamos a que siempre use el esquema claro para que no cambie los colores
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = ThriftAdColorScheme, // Usamos solo tu esquema de maquetado
        typography = Typography,
        content = content
    )
}