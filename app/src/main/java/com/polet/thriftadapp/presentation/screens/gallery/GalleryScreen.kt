package com.polet.thriftadapp.presentation.screens.gallery

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.polet.thriftadapp.data.local.entities.TicketEntity
import com.polet.thriftadapp.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalleryScreen(
    state: GalleryState,
    onSearchChange: (String) -> Unit,
    onSelectTicket: (TicketEntity?) -> Unit,
    onRequestDelete: (TicketEntity) -> Unit,      // abre diálogo con 2 opciones
    onDeletePhotoOnly: (TicketEntity) -> Unit,    // elimina solo la foto (desde visor)
    onDeleteCompleteGasto: (TicketEntity) -> Unit,
    onDismissDeleteDialog: () -> Unit,
    onBack: () -> Unit
) {
    BackHandler(enabled = state.selectedTicket != null) {
        onSelectTicket(null)
    }

    // --- DIÁLOGO DE 2 OPCIONES ---
    state.deleteDialogTicket?.let { ticket ->
        AlertDialog(
            onDismissRequest = onDismissDeleteDialog,
            title = { Text("¿Qué deseas eliminar?", fontWeight = FontWeight.Bold) },
            text  = { Text("\"${ticket.concept}\" — $${ticket.amount} MXN") },
            confirmButton = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { onDeletePhotoOnly(ticket) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF57F17))
                    ) {
                        Text("Eliminar solo la foto", color = Color.White)
                    }
                    Button(
                        onClick = { onDeleteCompleteGasto(ticket) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                    ) {
                        Text("Eliminar gasto completo", color = Color.White)
                    }
                    TextButton(
                        onClick = onDismissDeleteDialog,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Cancelar", color = Color.Gray)
                    }
                }
            },
            dismissButton = {}
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Galería de tickets", fontSize = 20.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás", tint = PurplePrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = LightBG)
            )
        },
        containerColor = LightBG
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp)
            ) {
                // Buscador
                OutlinedTextField(
                    value = state.searchText,
                    onValueChange = onSearchChange,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
                    placeholder = { Text("Buscar por concepto...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    shape = RoundedCornerShape(15.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PurplePrimary)
                )

                if (state.tickets.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No hay tickets guardados", color = Color.Gray)
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(state.tickets) { ticket ->
                            TicketCard(
                                ticket = ticket,
                                onClick = { onSelectTicket(ticket) }
                            )
                        }
                    }
                }
            }

            // Visor Full Screen
            state.selectedTicket?.let { ticket ->
                FullImageVisor(
                    ticket              = ticket,
                    onClose             = { onSelectTicket(null) },
                    onDeletePhotoOnly   = { onDeletePhotoOnly(it); onSelectTicket(null) },
                    onDeleteCompleteGasto = { onRequestDelete(it) }
                )
            }
        }
    }
}

@Composable
fun FullImageVisor(
    ticket: TicketEntity,
    onClose: () -> Unit,
    onDeletePhotoOnly: (TicketEntity) -> Unit,      // elimina la foto, el gasto permanece
    onDeleteCompleteGasto: (TicketEntity) -> Unit   // abre diálogo para eliminar el gasto
) {
    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
            AsyncImage(
                model = ticket.imagePath,
                contentDescription = null,
                modifier = Modifier.fillMaxSize().align(Alignment.Center),
                contentScale = ContentScale.Fit
            )

            // Barra superior: Atrás | Eliminar gasto completo
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 40.dp, start = 20.dp, end = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onClose,
                    modifier = Modifier.background(Color.Black.copy(0.4f), CircleShape)
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Cerrar", tint = Color.White)
                }
                IconButton(
                    onClick = { onDeleteCompleteGasto(ticket) },
                    modifier = Modifier.background(Color.Red.copy(0.2f), CircleShape)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar gasto", tint = Color.Red)
                }
            }

            // Barra inferior: info + botón eliminar SOLO la foto
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .background(Color.Black.copy(0.6f))
                    .padding(20.dp)
            ) {
                Text(ticket.concept, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text("$${ticket.amount} MXN", color = Color.White.copy(0.8f), fontSize = 16.sp)
                Text(ticket.date, color = Color.Gray, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(12.dp))
                // Botón explícito para eliminar solo la foto
                OutlinedButton(
                    onClick = { onDeletePhotoOnly(ticket) },
                    modifier = Modifier.fillMaxWidth(),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF57F17)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        tint = Color(0xFFF57F17),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Eliminar solo la foto", color = Color(0xFFF57F17), fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
fun TicketCard(ticket: TicketEntity, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().aspectRatio(0.8f).clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column {
            AsyncImage(
                model = ticket.imagePath,
                contentDescription = ticket.concept,
                modifier = Modifier.fillMaxWidth().weight(1f).clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp)),
                contentScale = ContentScale.Crop
            )
            Column(modifier = Modifier.padding(10.dp)) {
                Text(ticket.concept, fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1)
                Text("$${ticket.amount} MXN", color = PurplePrimary, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                Text(ticket.date, fontSize = 10.sp, color = Color.Gray)
            }
        }
    }
}