package com.polet.thriftadapp.presentation.screens.help

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.polet.thriftadapp.ui.theme.PurplePrimary
import com.polet.thriftadapp.ui.theme.LightBG

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var userMessage by remember { mutableStateOf("") }

    // Lista de palabras que bloquean el envío para proteger a Polet
    val prohibitedWords = listOf("pendejo", "estúpido", "basura", "puto", "idiota")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Centro de Ayuda", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Regresar", tint = PurplePrimary)
                    }
                }
            )
        },
        containerColor = LightBG
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(25.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.SupportAgent,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = PurplePrimary
            )

            Text(
                text = "Reportar Fallo o Sugerencia",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 10.dp)
            )

            // CAMPO DE TEXTO PARA EL FEEDBACK
            OutlinedTextField(
                value = userMessage,
                onValueChange = { userMessage = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                placeholder = { Text("Describe el error o la mejora que tienes en mente...") },
                shape = RoundedCornerShape(15.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = PurplePrimary
                )
            )

            // TARJETA DE ADVERTENCIA (Protección de integridad)
            Card(
                modifier = Modifier.padding(vertical = 20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.GppBad, contentDescription = null, tint = Color.Red)
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = "Aviso: El uso de lenguaje ofensivo hacia el soporte técnico resultará en la suspensión inmediata de la cuenta.",
                        fontSize = 11.sp,
                        color = Color.Black,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Button(
                onClick = {
                    val messageLower = userMessage.lowercase()
                    val hasBadWords = prohibitedWords.any { messageLower.contains(it) }

                    if (hasBadWords) {
                        Toast.makeText(context, "🚫 Mensaje bloqueado por lenguaje ofensivo.", Toast.LENGTH_LONG).show()
                    } else if (userMessage.isBlank()) {
                        Toast.makeText(context, "El mensaje no puede estar vacío.", Toast.LENGTH_SHORT).show()
                    } else {
                        enviarCorreoSoporte(context, userMessage)
                        // 2. LIMPIAMOS EL CAMPO (Para que cuando regrese esté vacío)
                        userMessage = ""

                        // 3. OPCIONAL: Mostrar un aviso de que se envió
                        Toast.makeText(context, "Abriendo correo...", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(55.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)
            ) {
                Icon(Icons.Default.Send, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Enviar Reporte", fontWeight = FontWeight.Bold)
            }
        }
    }
}

// Función para disparar el Intent de correo
fun enviarCorreoSoporte(context: Context, mensaje: String) {
    val intent = Intent(Intent.ACTION_SENDTO).apply {
        data = Uri.parse("mailto:")
        putExtra(Intent.EXTRA_EMAIL, arrayOf("itandiaz678@gmail.com"))
        putExtra(Intent.EXTRA_SUBJECT, "Reporte ThriftAdApp - Soporte")
        putExtra(Intent.EXTRA_TEXT, "Detalles del reporte:\n\n$mensaje")
    }

    try {
        context.startActivity(Intent.createChooser(intent, "Selecciona tu app de correo"))
    } catch (e: Exception) {
        Toast.makeText(context, "No tienes una app de correo instalada.", Toast.LENGTH_SHORT).show()
    }
}