package com.polet.thriftadapp.presentation.screens.login

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LockReset
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
fun ForgotPasswordScreen(onBack: () -> Unit) {
    var input by remember { mutableStateOf("") }
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Recuperar Cuenta", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null, tint = PurplePrimary)
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
                .padding(25.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Default.LockReset,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = PurplePrimary
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                "¿Olvidaste tu contraseña?",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Text(
                "Ingresa tu correo o nombre de usuario y te ayudaremos a recuperar el acceso.",
                textAlign = TextAlign.Center,
                color = Color.Gray,
                modifier = Modifier.padding(vertical = 15.dp)
            )

            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                label = { Text("Usuario o Email") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(15.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PurplePrimary)
            )

            Spacer(modifier = Modifier.height(30.dp))

            Button(
                onClick = {
                    if (input.isBlank()) {
                        Toast.makeText(context, "Por favor escribe algo", Toast.LENGTH_SHORT).show()
                    } else if (input.contains("@")) {
                        // FLUJO A: Tiene correo (Simulado por ahora)
                        Toast.makeText(context, "Instrucciones enviadas a: $input", Toast.LENGTH_LONG).show()
                        onBack()
                    } else {
                        // FLUJO B: No tiene correo (Abrimos Gmail automáticamente)
                        val intent = android.content.Intent(android.content.Intent.ACTION_SENDTO).apply {
                            data = android.net.Uri.parse("mailto:itandiaz678@gmail.com")
                            putExtra(android.content.Intent.EXTRA_SUBJECT, "Recuperación de Cuenta: $input")
                            putExtra(android.content.Intent.EXTRA_TEXT, "Hola Polet, olvidé mi contraseña. Mi usuario en ThriftAd es: $input")
                        }
                        try {
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            // Por si el emulador no tiene app de correo
                            Toast.makeText(context, "Contacta a: itandiaz678@gmail.com", Toast.LENGTH_LONG).show()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(55.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)
            ) {
                Text("Verificar y Continuar", fontWeight = FontWeight.Bold)
            }
        }
    }
}