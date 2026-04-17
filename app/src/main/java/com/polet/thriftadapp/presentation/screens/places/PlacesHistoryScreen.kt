package com.polet.thriftadapp.presentation.screens.places

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.polet.thriftadapp.presentation.viewmodels.PlacesViewModel
import com.polet.thriftadapp.domain.model.ItemLugar
// Quitamos la data class de aquí porque ya debería estar en un modelo o en el ViewModel
// Pero para que no te de error, la dejamos si el ViewModel la usa igual.

@Composable
fun PlacesHistoryScreen(
    viewModel: PlacesViewModel = hiltViewModel() // <--- CONEXIÓN AL MOTOR
) {
    // Colores de tu marca Polet
    val fondoGris = Color(0xFFF7F8FA)
    val moradoSuave = Color(0xFFE8DEF8)
    val rosaIcono = Color(0xFFD81B60)

    // Escuchamos la lista REAL que viene del GPS y el Formulario
    val listaReal = viewModel.listaLugares

    Scaffold(
        containerColor = fondoGris
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 25.dp)
        ) {
            // CABECERA
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 25.dp)
            ) {
                Text(text = "📍", fontSize = 22.sp)
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Historial de Lugares",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.Black
                )
            }

            // LISTA DINÁMICA
            if (listaReal.isEmpty()) {
                // Si no hay nada, mostramos un mensaje bonito
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Aún no tienes lugares registrados", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(15.dp)
                ) {
                    items(listaReal) { item ->
                        TarjetaLugar(item, moradoSuave, rosaIcono)
                    }
                }
            }
        }
    }
}

@Composable
fun TarjetaLugar(item: ItemLugar, colorFondoIcono: Color, colorPin: Color) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(colorFondoIcono),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "📍", fontSize = 20.sp)
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.nombre,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = Color.Black
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "📍", fontSize = 10.sp, color = colorPin)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = item.ubicacion,
                        fontSize = 12.sp,
                        color = Color.Gray,
                        maxLines = 1 // Para que no se amontone si la dirección es larga
                    )
                }
            }

            Text(
                text = item.monto,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 17.sp,
                color = Color.Black
            )
        }
    }
}