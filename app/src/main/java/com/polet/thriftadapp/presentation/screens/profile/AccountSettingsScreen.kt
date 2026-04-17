package com.polet.thriftadapp.presentation.screens.profile

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.polet.thriftadapp.ui.theme.*
import androidx.compose.material3.OutlinedTextFieldDefaults

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountSettingsScreen(
    state: AccountState,
    onIdentifierChange: (String) -> Unit,
    onRoleChange: (String) -> Unit,
    onVibrationToggle: (Boolean) -> Unit,
    onSoundToggle: (Boolean) -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configuración", fontWeight = FontWeight.ExtraBold) },
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
                .padding(horizontal = 25.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // --- SECCIÓN 1: IDENTIFICACIÓN HÍBRIDA ---
            Text("IDENTIFICACIÓN", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = PurplePrimary)

            val isEmail = state.identifier.contains("@")

            OutlinedTextField(
                value = state.identifier,
                onValueChange = onIdentifierChange,
                label = { Text(if (isEmail) "Correo Electrónico" else "Nombre Completo") },
                leadingIcon = {
                    Icon(
                        imageVector = if (isEmail) Icons.Default.Email else Icons.Default.Person,
                        contentDescription = null,
                        tint = PurplePrimary
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                // Usamos el bloque de colores más compatible
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PurplePrimary,
                    unfocusedBorderColor = Color.LightGray,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    cursorColor = PurplePrimary,
                    focusedLabelColor = PurplePrimary
                ),
                singleLine = true
            )

            // --- SECCIÓN 2: ROL ---
            Text("MI ROL ACTUAL", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = PurplePrimary)
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    val roles = listOf("Estudiante", "Negocio", "Hogar")
                    roles.forEach { role ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            RadioButton(
                                selected = (role == state.role),
                                onClick = { onRoleChange(role) },
                                colors = RadioButtonDefaults.colors(selectedColor = PurplePrimary)
                            )
                            Text(text = role, fontSize = 15.sp)
                        }
                    }
                }
            }

            // --- SECCIÓN 3: PREFERENCIAS (Aquí controlas la campanita) ---
            Text("SISTEMA", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = PurplePrimary)
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Switch de Vibración
                    PreferenceRow(
                        title = "Vibración háptica",
                        checked = state.isVibrationEnabled,
                        onToggle = onVibrationToggle
                    )

                    Divider(modifier = Modifier.padding(vertical = 12.dp), thickness = 0.5.dp, color = LightBG)

                    // Switch de Sonido (Campanita)
                    PreferenceRow(
                        title = "Sonido de notificación",
                        checked = state.isSoundEnabled,
                        onToggle = onSoundToggle
                    )
                }
            }

            // Botón para simular guardado y regresar
            Button(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth().height(55.dp),
                shape = RoundedCornerShape(15.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)
            ) {
                Text("Guardar y Salir", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
fun PreferenceRow(title: String, checked: Boolean, onToggle: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, fontWeight = FontWeight.Medium, fontSize = 15.sp)
        Switch(
            checked = checked,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(checkedTrackColor = PurplePrimary)
        )
    }
}