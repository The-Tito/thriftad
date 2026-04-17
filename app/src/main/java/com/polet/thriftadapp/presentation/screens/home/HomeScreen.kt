package com.polet.thriftadapp.presentation.screens.home

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.polet.thriftadapp.ui.theme.*
import com.polet.thriftadapp.presentation.viewmodels.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    userId: Int,
    userName: String,
    nombreCompleto: String = "",
    viewModel: HomeViewModel,
    onToggleNotifications: () -> Unit,
    onShowModal: (Boolean, Boolean, Boolean) -> Unit,
    onAmountChange: (String) -> Unit,
    onAdjustBalance: () -> Unit,
    onDeleteTransaction: (String) -> Unit
) {
    // Conexión al estado del ViewModel (carga iniciada desde NavGraph)
    val state by viewModel.state.collectAsState()

    val sheetState = rememberModalBottomSheetState()
    var showBottomSheet by remember { mutableStateOf(false) }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    // Prioridad: nombre editado en Settings (state) > route arg > userName
                val nombreMostrar = state.nombreCompleto.ifBlank { nombreCompleto.ifBlank { userName } }
                    Column {
                        Text("Hola,", fontSize = 12.sp, color = TextGray)
                        Text(nombreMostrar, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                },
                actions = {
                    IconButton(onClick = { showBottomSheet = true }) {
                        Icon(Icons.Default.Notifications, contentDescription = null, tint = PurplePrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = LightBG)
            )
        },
        containerColor = LightBG
    ) { padding ->

        // --- 1. MODAL DE NOTIFICACIONES ---
        if (showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = { showBottomSheet = false },
                sheetState = sheetState,
                containerColor = Color.White
            ) {
                Column(modifier = Modifier.padding(25.dp).fillMaxWidth()) {
                    Text("Avisos Importantes", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = PurplePrimary)
                    Spacer(modifier = Modifier.height(15.dp))
                    NotificationItem("¡Presupuesto bajo!", "Te queda menos del 15%.", Icons.Default.Warning, Color(0xFFFFA000))
                    NotificationItem("Ticket pendiente", "Recuerda subir tu último gasto.", Icons.Default.ReceiptLong, PurplePrimary)
                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }

        // --- 2. DIÁLOGO DE AJUSTE (AlertDialog) ---
        // --- 2. DIÁLOGO DE AJUSTE (AlertDialog) ACTUALIZADO ---
        if (state.isModalVisible) {
            AlertDialog(
                // Cambiamos esto a vacío para que NO se cierre al picar fuera
                onDismissRequest = { },
                confirmButton = {
                    Button(
                        onClick = { viewModel.onConfirmAdjustment() },
                        // Ajustamos a fillMaxWidth(0.45f) para que los dos botones se vean parejos
                        modifier = Modifier.fillMaxWidth(0.45f).height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (state.isEditingMeta) SecondaryPurple
                            else if (state.isAdding) Color(0xFF66BB6A)
                            else Color(0xFFEF5350)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) { Text("Confirmar") }
                },
                // --- BOTÓN CANCELAR (Tu salida de emergencia) ---
                dismissButton = {
                    TextButton(
                        onClick = { viewModel.onShowModal(false, false, true) },
                        modifier = Modifier.height(50.dp)
                    ) { Text("Cancelar", color = Color.Gray) }
                },
                title = {
                    Text(
                        text = if (state.isEditingMeta) "Meta de Ahorro"
                        else if (state.isAdding) "Sumar al Saldo"
                        else "Restar al Saldo",
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    // Metemos todo en una Column para que quepa el mensaje de error
                    Column {
                        OutlinedTextField(
                            value = state.quickAmount,
                            onValueChange = onAmountChange,
                            placeholder = { Text("0.00") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        // MENSAJE DE SALDO INSUFICIENTE (Solo aparece si el VM detecta que no hay lana)
                        if (state.error != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = state.error!!,
                                color = Color(0xFFEF5350), // Rojo elegante
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )
                        }
                    }
                },
                shape = RoundedCornerShape(25.dp)
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp)
        ) {
            // --- CARD DE PRESUPUESTO ---
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier.fillMaxWidth().height(180.dp)
                        .background(Brush.linearGradient(listOf(PurplePrimary, SecondaryPurple)), RoundedCornerShape(25.dp))
                        .padding(20.dp)
                ) {
                    Column {
                        Text("Presupuesto disponible", color = Color.White.copy(0.8f), fontSize = 12.sp)
                        Text("$${"%.2f".format(state.balance)} MXN", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Black)

                        Surface(
                            color = Color.White.copy(0.15f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .padding(top = 12.dp)
                                .clickable {
                                    viewModel.onShowModal(true,true,true) }
                        ) {
                            Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Star, null, tint = Color.White, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = "Meta de ahorro: $${"%.2f".format(state.savingMeta)}", // <--- ESTA ES LA CORRECCIÓN
                                    color = Color.White,
                                    fontSize = 12.sp)
                            }
                        }
                    }

                    Column(modifier = Modifier.align(Alignment.CenterEnd), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                        IconButton(
                            onClick = {viewModel.onShowModal(true,false,true)}, modifier = Modifier.size(38.dp).background(Color.White.copy(0.2f), CircleShape)) {
                            Icon(Icons.Default.Add, null, tint = Color.White)
                        }
                        IconButton(
                            onClick = {
                                viewModel.onShowModal(true,false,false)
                                      },
                            modifier = Modifier.size(38.dp).background(Color.White.copy(0.2f), CircleShape)
                        ) { Icon(Icons.Default.Remove, null, tint = Color.White) }
                    }
                }
            }

            // --- SECCIONES DIFERENCIADAS POR ROL ---
            item {
                Spacer(modifier = Modifier.height(20.dp))
                val rolNorm = state.role.lowercase().trim()
                when {
                    rolNorm.contains("admin") ||
                    rolNorm.contains("negocio") ||
                    rolNorm.contains("business") ||
                    rolNorm.contains("empresa") -> BusinessPieChart(state.chartData)
                    rolNorm.contains("hogar") ||
                    rolNorm.contains("home")   -> HogarSection(state)
                    else                       -> EstudianteSection(state)
                }
            }

            item {
                Spacer(modifier = Modifier.height(25.dp))
                Text("Últimos Movimientos", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Spacer(modifier = Modifier.height(10.dp))
            }

            // --- LISTA CON SWIPE (CORREGIDA) ---
            items(state.transactions, key = { it.id }) { tx ->
                // VITAL: Usamos remember para que el estado no se resetee mientras deslizas
                val dismissState = rememberSwipeToDismissBoxState(
                    confirmValueChange = { value ->
                        if (value == SwipeToDismissBoxValue.EndToStart) {
                            onDeleteTransaction(tx.id)
                            true
                        } else {
                            false
                        }
                    },
                    positionalThreshold = { distance -> distance * 0.25f }
                )

                // USAMOS UN ANIMATED VISIBILITY PARA QUE NO HAYA SALTOS BRUSCOS
                AnimatedVisibility(
                    visible = state.transactions.any { it.id == tx.id },
                    exit = shrinkVertically() + fadeOut()
                ) {
                    SwipeToDismissBox(
                        state = dismissState,
                        enableDismissFromStartToEnd = false,
                        backgroundContent = {
                            val color by animateColorAsState(
                                when (dismissState.targetValue) {
                                    SwipeToDismissBoxValue.EndToStart -> Color.Red.copy(0.2f)
                                    else -> Color.Transparent
                                }, label = ""
                            )

                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(vertical = 6.dp)
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(color),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Eliminar",
                                    tint = Color.Red,
                                    modifier = Modifier.padding(end = 20.dp)
                                )
                            }
                        }
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            colors = CardDefaults.cardColors(containerColor = White),
                            shape = RoundedCornerShape(18.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(LightBG, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (tx.isInc) Icons.Default.TrendingUp else Icons.Default.ReceiptLong,
                                        contentDescription = null,
                                        tint = if (tx.isInc) Color(0xFF66BB6A) else PurplePrimary
                                    )
                                }
                                Spacer(modifier = Modifier.width(15.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(tx.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text("${tx.category} • ${tx.date}", fontSize = 11.sp, color = TextGray)
                                }
                                Text(
                                    text = if (tx.isInc) "+$${tx.amount}" else "-$${tx.amount}",
                                    color = if (tx.isInc) Color(0xFF66BB6A) else Color(0xFFEF5350),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

// --- FUNCIONES AUXILIARES (FUERA DE HOMESCREEN) ---

@Composable
fun BusinessPieChart(data: List<PieChartData>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(25.dp),
        colors   = CardDefaults.cardColors(containerColor = White)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                "Distribución de Gastos",
                fontWeight = FontWeight.Bold,
                color      = PurplePrimary,
                fontSize   = 16.sp
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (data.isEmpty()) {
                Box(
                    modifier         = Modifier.fillMaxWidth().height(80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Sin gastos registrados aún", fontSize = 13.sp, color = TextGray)
                }
            } else {
                // ─── PIE CHART SÓLIDO — centrado ──────────────────────────────
                Box(
                    modifier         = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(200.dp)) {
                        // Separación visual entre rebanadas (blanco entre slices)
                        val gapDeg       = if (data.size > 1) 2f else 0f
                        var startAngle   = -90f   // 12 en punto
                        var drawnDegrees = 0f

                        data.forEachIndexed { index, slice ->
                            // El último slice llena exactamente los grados restantes → sin gap float
                            val sweep = if (index == data.lastIndex)
                                360f - drawnDegrees
                            else
                                (slice.value / 100f) * 360f

                            drawArc(
                                color      = slice.color,
                                startAngle = startAngle + gapDeg / 2f,
                                sweepAngle = maxOf(sweep - gapDeg, 0.5f),
                                useCenter  = true,
                                size       = Size(size.width, size.height)
                            )
                            startAngle   += sweep
                            drawnDegrees += sweep
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // ─── LEYENDA EN 2 COLUMNAS ────────────────────────────────────
                val filas = data.chunked(2)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    filas.forEach { fila ->
                        Row(modifier = Modifier.fillMaxWidth()) {
                            fila.forEach { slice ->
                                Row(
                                    modifier              = Modifier.weight(1f),
                                    verticalAlignment     = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .background(slice.color, CircleShape)
                                    )
                                    Column {
                                        Text(
                                            text     = slice.label,
                                            fontSize = 12.sp,
                                            maxLines = 1,
                                            color    = Color(0xFF222222)
                                        )
                                        Text(
                                            text     = "${"%.1f".format(slice.value)}%",
                                            fontSize = 10.sp,
                                            color    = TextGray
                                        )
                                    }
                                }
                            }
                            if (fila.size == 1) Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun NotificationItem(title: String, desc: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(45.dp).background(color.copy(0.1f), CircleShape), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(15.dp))
        Column {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(desc, fontSize = 12.sp, color = TextGray)
        }
    }
}

// ─── SECCIÓN HOGAR ────────────────────────────────────────────────────────────

@Composable
fun HogarSection(state: HomeState) {
    val reservadoEnMetas = state.metas.sumOf { it.targetAmount }
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        if (reservadoEnMetas > 0) {
            AvailableMoneyCard(
                balance       = state.balanceDisponible,
                monthlyBudget = state.monthlyBudget
            )
        }
        if (state.metas.isNotEmpty()) {
            MetasSection(metas = state.metas)
        }
    }
}

// ─── SECCIÓN ESTUDIANTE ───────────────────────────────────────────────────────

@Composable
fun EstudianteSection(state: HomeState) {
    AvailableMoneyCard(balance = state.balance, monthlyBudget = state.monthlyBudget)
}

// ─── COMPONENTES COMPARTIDOS ──────────────────────────────────────────────────

@Composable
fun MonthlyBudgetCard(label: String, amount: Double, color: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(18.dp),
        colors   = CardDefaults.cardColors(containerColor = White)
    ) {
        Row(
            modifier          = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier        = Modifier.size(44.dp).background(color.copy(0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = color, modifier = Modifier.size(22.dp))
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(label, fontSize = 12.sp, color = TextGray)
                Text("$${"%.2f".format(amount)} MXN", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}

@Composable
fun AvailableMoneyCard(balance: Double, monthlyBudget: Double) {
    val deficit   = balance < 0
    val sobrante  = if (monthlyBudget > 0) monthlyBudget - (monthlyBudget - balance).coerceAtLeast(0.0)
                    else balance
    val cardColor = if (deficit) Color(0xFFFFEBEE) else Color(0xFFE8F5E9)
    val textColor = if (deficit) Color(0xFFEF5350) else Color(0xFF43A047)
    val icon      = if (deficit) Icons.Default.TrendingDown else Icons.Default.TrendingUp
    val msg       = if (deficit) "Déficit este mes" else "Saldo disponible"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(18.dp),
        colors   = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Row(
            modifier          = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = textColor, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(msg, fontSize = 12.sp, color = textColor)
                Text("$${"%.2f".format(balance)} MXN", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = textColor)
            }
        }
    }
}

@Composable
fun MetasSection(metas: List<MetaItem>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Metas de ahorro", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        metas.forEach { meta ->
            val progress = if (meta.targetAmount > 0)
                (meta.currentAmount / meta.targetAmount).toFloat().coerceIn(0f, 1f)
            else 0f
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape    = RoundedCornerShape(14.dp),
                colors   = CardDefaults.cardColors(containerColor = White)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(meta.name, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Text(
                            "$${"%.0f".format(meta.currentAmount)} / $${"%.0f".format(meta.targetAmount)}",
                            fontSize = 12.sp, color = TextGray
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress            = { progress },
                        modifier            = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                        color               = SecondaryPurple,
                        trackColor          = Color(0xFFEDE7F6)
                    )
                }
            }
        }
    }
}

@Composable
fun CategorizedTransactionsList(transactions: List<Transaction>) {
    val grouped = transactions.groupBy { it.category.ifBlank { "Otros" } }
    if (grouped.isEmpty()) return

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Por categoría", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        grouped.forEach { (categoria, items) ->
            val total = items.filter { !it.isInc }.sumOf { it.amount }
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape    = RoundedCornerShape(14.dp),
                colors   = CardDefaults.cardColors(containerColor = White)
            ) {
                Row(
                    modifier              = Modifier.padding(14.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier         = Modifier.size(36.dp).background(Color(0xFFEDE7F6), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Category, contentDescription = null, tint = SecondaryPurple, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(categoria, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Text("${items.size} movimiento${if (items.size != 1) "s" else ""}", fontSize = 11.sp, color = TextGray)
                        }
                    }
                    Text("-$${"%.2f".format(total)}", fontWeight = FontWeight.Bold, color = Color(0xFFEF5350), fontSize = 14.sp)
                }
            }
        }
    }
}