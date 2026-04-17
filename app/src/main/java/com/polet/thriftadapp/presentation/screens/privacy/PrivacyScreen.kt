package com.polet.thriftadapp.presentation.screens.privacy

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.polet.thriftadapp.ui.theme.PurplePrimary
import com.polet.thriftadapp.ui.theme.LightBG

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Privacidad y Seguridad", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Regresar", tint = PurplePrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = LightBG)
            )
        },
        containerColor = LightBG
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 25.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            Icon(
                imageVector = Icons.Default.PrivacyTip,
                contentDescription = null,
                modifier = Modifier.size(70.dp).align(Alignment.CenterHorizontally),
                tint = PurplePrimary
            )

            Text(
                text = "Tu seguridad es primero",
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.fillMaxWidth().padding(vertical = 15.dp),
                textAlign = TextAlign.Center
            )

            // --- BLOQUE DE PROTECCIÓN (EL MURO CONTRA GROSERÍAS) ---
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)), // Rojo suave de alerta
                modifier = Modifier.padding(vertical = 10.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Gavel, contentDescription = null, tint = Color(0xFFD32F2F))
                        Spacer(Modifier.width(10.dp))
                        Text(
                            "Términos de Convivencia",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD32F2F),
                            fontSize = 16.sp
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "ThriftAdApp es un espacio seguro. Queda estrictamente prohibido el uso de lenguaje ofensivo o ataques verbales hacia el personal de soporte o administración. " +
                                "\n\nCualquier mensaje que contenga insultos resultará en el bloqueo permanente de la cuenta sin previo aviso.",
                        fontSize = 13.sp,
                        color = Color.Black,
                        lineHeight = 18.sp
                    )
                }
            }

            // --- SECCIÓN: ALMACENAMIENTO ---
            SeccionTextoPrivacidad(
                titulo = "Protección de Datos",
                cuerpo = "Toda tu información financiera se gestiona localmente en tu dispositivo. No subimos tus registros de gastos a servidores externos sin tu autorización."
            )

            // --- SECCIÓN: PERMISOS ---
            SeccionTextoPrivacidad(
                titulo = "Uso de Cámara",
                cuerpo = "La cámara se utiliza exclusivamente para capturar fotos de tus tickets y facilitar el registro de gastos. Las fotos se guardan en tu galería personal."
            )

            Spacer(modifier = Modifier.height(40.dp))

            Text(
                text = "Versión 1.0.0 - ThriftAdApp © 2026",
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun SeccionTextoPrivacidad(titulo: String, cuerpo: String) {
    Column(modifier = Modifier.padding(vertical = 12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Security, contentDescription = null, tint = PurplePrimary, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(text = titulo, fontWeight = FontWeight.Bold, color = PurplePrimary, fontSize = 15.sp)
        }
        Text(
            text = cuerpo,
            fontSize = 14.sp,
            color = Color.DarkGray,
            modifier = Modifier.padding(top = 4.dp),
            textAlign = TextAlign.Justify
        )
    }
}