package com.polet.thriftadapp.presentation.screens.account

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.polet.thriftadapp.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountScreen(
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configuración", fontSize = 20.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(LightBG)
                .padding(20.dp)
        ) {
            Text("Información Personal", fontWeight = FontWeight.Bold, color = PurplePrimary)
            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = "Polet Itandegui Jimenez Diaz",
                onValueChange = {},
                label = { Text("Nombre Completo") },
                modifier = Modifier.fillMaxWidth(),
                enabled = false // Será editable cuando hagamos el ViewModel
            )

            Spacer(modifier = Modifier.height(15.dp))

            OutlinedTextField(
                value = "Software Engineering Student",
                onValueChange = {},
                label = { Text("Cargo / Rol") },
                modifier = Modifier.fillMaxWidth(),
                enabled = false
            )
        }
    }
}