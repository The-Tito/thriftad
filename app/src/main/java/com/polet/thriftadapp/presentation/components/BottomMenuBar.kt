package com.polet.thriftadapp.presentation.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

// 1. Simplificamos la ruta de Inicio a solo "home"
sealed class BottomItem(val route: String, val icon: ImageVector, val label: String) {
    object Inicio : BottomItem("home", Icons.Default.Home, "Inicio") // <-- CAMBIO AQUÍ
    object Anadir : BottomItem("add", Icons.Default.Add, "Añadir")
    object Lugares : BottomItem("map", Icons.Default.LocationOn, "Lugares")
    object Perfil : BottomItem("profile", Icons.Default.Person, "Perfil")
}

@Composable
fun BottomMenuBar(currentRoute: String?, onItemClick: (String) -> Unit) {
    NavigationBar(
        containerColor = Color.White
    ) {
        val items = listOf(
            BottomItem.Inicio,
            BottomItem.Anadir,
            BottomItem.Lugares,
            BottomItem.Perfil
        )

        items.forEach { item ->
            // 2. Mejoramos la lógica de selección
            // Si la ruta actual contiene "home", el icono de Inicio se iluminará
            val isSelected = when {
                item.route == "home" -> currentRoute?.startsWith("home") == true
                else -> currentRoute == item.route
            }

            NavigationBarItem(
                selected = isSelected,
                onClick = { onItemClick(item.route) },
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color(0xFF6A1B9A),
                    indicatorColor = Color(0xFFF3E5F5)
                )
            )
        }
    }
}