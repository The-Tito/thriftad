package com.polet.thriftadapp.presentation.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.polet.thriftadapp.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    userName: String = "Usuario",
    onLogout: () -> Unit,
    onNavigateToAccount: () -> Unit,
    onNavigateToGallery: () -> Unit,
    onNavigateToHelp: () -> Unit,
    onNavigateToPrivacy: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LightBG)
            .padding(25.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(50.dp))

        // --- FOTO DE PERFIL (Círculo con inicial) ---
        Box(
            modifier = Modifier
                .size(110.dp)
                .clip(CircleShape)
                .background(PurplePrimary),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = userName.take(1).uppercase(),
                color = Color.White,
                fontSize = 45.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // --- INFORMACIÓN DEL USUARIO --- [cite: 5, 7]
        Text(
            text = userName,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(40.dp))

        // --- MENÚ DE 5 BOTONES (4 Opciones + 1 Logout) ---

        // 1. Configuración de Cuenta
        OpcionPerfil(
            texto = "Configuración de Cuenta",
            icono = Icons.Default.Settings,
            colorIcono = Color(0xFF4FC3F7),
            onClick = onNavigateToAccount
        )

        // 2. Galería de Tickets (Respaldo visual Etapa 2) [cite: 26, 32]
        OpcionPerfil(
            texto = "Galería de Tickets",
            icono = Icons.Default.PhotoLibrary,
            colorIcono = Color(0xFF81C784),
            onClick = onNavigateToGallery
        )

        // 3. Centro de Ayuda
        OpcionPerfil(
            texto = "Centro de Ayuda",
            icono = Icons.Default.HelpOutline,
            colorIcono = Color(0xFFF06292),
            onClick = onNavigateToHelp
        )

        // 4. Privacidad y Seguridad (Requisito Play Store)
        OpcionPerfil(
            texto = "Privacidad y Seguridad",
            icono = Icons.Default.LockPerson,
            colorIcono = Color(0xFFFFB74D),
            onClick = onNavigateToPrivacy
        )

        Spacer(modifier = Modifier.weight(1f))

        // 5. BOTÓN CERRAR SESIÓN
        Button(
            onClick = onLogout,
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF5350)),
            shape = RoundedCornerShape(18.dp),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
        ) {
            Icon(Icons.Default.Logout, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Cerrar Sesión",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OpcionPerfil(
    texto: String,
    icono: ImageVector,
    colorIcono: Color,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick, // Hacemos la Card clickable
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(15.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(18.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icono,
                contentDescription = null,
                tint = colorIcono,
                modifier = Modifier.size(26.dp)
            )
            Spacer(modifier = Modifier.width(18.dp))
            Text(
                text = texto,
                fontSize = 15.sp,
                color = Color.Black,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = TextGray,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}