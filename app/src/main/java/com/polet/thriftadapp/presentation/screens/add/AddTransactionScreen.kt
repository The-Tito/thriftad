package com.polet.thriftadapp.presentation.screens.add

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.foundation.layout.size
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.polet.thriftadapp.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    state: AddTransactionState,
    onEvent: (AddTransactionEvent) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    // --- LÓGICA DE VALIDACIÓN ---
    // El formulario es válido si el nombre no está vacío y el monto es un número válido
    val isFormValid = state.nombre.isNotBlank() &&
            state.monto.isNotBlank() &&
            (state.monto.toDoubleOrNull() ?: 0.0) > 0.0

    Scaffold(
        containerColor = LightBG
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(25.dp)
        ) {
            Text(
                text = "Registrar Movimiento",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 20.dp)
            )

            // --- TIPO: INGRESO / GASTO ---
            FormInputLabel(text = "TIPO DE MOVIMIENTO")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(12.dp))
                    .background(White, RoundedCornerShape(12.dp))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text  = if (state.esIngreso) "Ingreso (+)" else "Gasto (−)",
                    fontSize     = 15.sp,
                    fontWeight   = FontWeight.SemiBold,
                    color        = if (state.esIngreso) Color(0xFF43A047) else Color(0xFFEF5350)
                )
                Switch(
                    checked        = state.esIngreso,
                    onCheckedChange = { onEvent(AddTransactionEvent.ToggleEsIngreso(it)) },
                    colors         = SwitchDefaults.colors(
                        checkedThumbColor   = White,
                        checkedTrackColor   = Color(0xFF43A047),
                        uncheckedThumbColor = White,
                        uncheckedTrackColor = Color(0xFFEF5350)
                    )
                )
            }
            FormSpacer()

            // --- NOMBRE DEL PRODUCTO (Solo letras) ---
            FormInputLabel(text = "NOMBRE DEL PRODUCTO")
            OutlinedTextField(
                value = state.nombre,
                onValueChange = { newValue ->
                    // Filtro: Solo permite letras y espacios
                    if (newValue.all { it.isLetter() || it.isWhitespace() }) {
                        onEvent(AddTransactionEvent.NombreChanged(newValue))
                    }
                },
                placeholder = { Text("Ej: Jabón, Materia Prima...", color = TextGray) },
                modifier = Modifier.fillMaxWidth().shadow(4.dp, RoundedCornerShape(12.dp)),
                shape = RoundedCornerShape(12.dp),
                colors = textFieldStyle()
            )
            FormSpacer()

            // --- MONTO (Solo números) ---
            FormInputLabel(text = "MONTO")
            OutlinedTextField(
                value = state.monto,
                onValueChange = { newValue ->
                    // Filtro: Solo permite números y un punto
                    if (newValue.all { it.isDigit() || it == '.' }) {
                        onEvent(AddTransactionEvent.MontoChanged(newValue))
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), // Teclado numérico
                placeholder = { Text("0.00", color = TextGray) },
                modifier = Modifier.fillMaxWidth().shadow(4.dp, RoundedCornerShape(12.dp)),
                shape = RoundedCornerShape(12.dp),
                colors = textFieldStyle()
            )
            FormSpacer()

            // --- CATEGORÍA ---
            FormInputLabel(text = "CATEGORÍA")
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = state.categoria,
                    onValueChange = { },
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth().shadow(4.dp, RoundedCornerShape(12.dp)),
                    shape = RoundedCornerShape(12.dp),
                    trailingIcon = {
                        IconButton(onClick = { onEvent(AddTransactionEvent.ToggleCategoriaMenu) }) {
                            Icon(Icons.Default.ArrowDropDown, null, tint = PurplePrimary)
                        }
                    },
                    colors = textFieldStyle()
                )
                DropdownMenu(
                    expanded = state.isCategoriaExpanded,
                    onDismissRequest = { onEvent(AddTransactionEvent.ToggleCategoriaMenu) },
                    modifier = Modifier.fillMaxWidth(0.85f).background(White)
                ) {
                    state.categoriasDisponibles.forEach { cat ->
                        DropdownMenuItem(
                            text = { Text(cat) },
                            onClick = { onEvent(AddTransactionEvent.CategoriaSelected(cat)) }
                        )
                    }
                }
            }
            FormSpacer()

            // --- FILA: UNIDAD Y CANTIDAD ---
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(15.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    FormInputLabel(text = "UNIDAD")
                    Box {
                        OutlinedTextField(
                            value = state.unidad,
                            onValueChange = { },
                            readOnly = true,
                            modifier = Modifier.fillMaxWidth().shadow(4.dp, RoundedCornerShape(12.dp)),
                            shape = RoundedCornerShape(12.dp),
                            trailingIcon = {
                                IconButton(onClick = { onEvent(AddTransactionEvent.ToggleUnidadMenu) }) {
                                    Icon(Icons.Default.ArrowDropDown, null, tint = PurplePrimary)
                                }
                            },
                            colors = textFieldStyle()
                        )
                        DropdownMenu(
                            expanded = state.isUnidadExpanded,
                            onDismissRequest = { onEvent(AddTransactionEvent.ToggleUnidadMenu) }
                        ) {
                            state.unidadesDisponibles.forEach { unit ->
                                DropdownMenuItem(
                                    text = { Text(unit) },
                                    onClick = { onEvent(AddTransactionEvent.UnidadSelected(unit)) }
                                )
                            }
                        }
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    FormInputLabel(text = "CANT.")
                    OutlinedTextField(
                        value = state.cantidad,
                        onValueChange = { onEvent(AddTransactionEvent.CantidadChanged(it)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth().shadow(4.dp, RoundedCornerShape(12.dp)),
                        shape = RoundedCornerShape(12.dp),
                        colors = textFieldStyle()
                    )
                }
            }
            FormSpacer()

            // --- FECHA CON CALENDARIO ---
            FormInputLabel(text = "FECHA")
            var showDatePicker by remember { mutableStateOf(false) }
            val datePickerState = rememberDatePickerState()

            if (showDatePicker) {
                DatePickerDialog(
                    onDismissRequest = { showDatePicker = false },
                    confirmButton = {
                        TextButton(onClick = {
                            datePickerState.selectedDateMillis?.let { millis ->
                                val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                                formatter.timeZone = TimeZone.getTimeZone("UTC")
                                onEvent(AddTransactionEvent.FechaChanged(formatter.format(Date(millis))))
                            }
                            showDatePicker = false
                        }) { Text("OK", color = PurplePrimary) }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDatePicker = false }) { Text("Cancelar") }
                    }
                ) {
                    DatePicker(state = datePickerState)
                }
            }

            OutlinedTextField(
                value = state.fecha,
                onValueChange = { },
                readOnly = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(12.dp))
                    .clickable { showDatePicker = true },
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Default.DateRange, null, tint = PurplePrimary)
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = textFieldStyle()
            )
            FormSpacer()

            // --- DESCRIPCIÓN ---
            FormInputLabel(text = "DESCRIPCIÓN (OPCIONAL)")
            OutlinedTextField(
                value = state.descripcion,
                onValueChange = { onEvent(AddTransactionEvent.DescripcionChanged(it)) },
                placeholder = { Text("Notas sobre tu compra...", color = TextGray) },
                modifier = Modifier.fillMaxWidth().height(100.dp).shadow(4.dp, RoundedCornerShape(12.dp)),
                shape = RoundedCornerShape(12.dp),
                colors = textFieldStyle()
            )

            FormSpacer()

            // --- ERROR ---
            if (state.error != null) {
                Text(
                    text = state.error,
                    color = androidx.compose.ui.graphics.Color.Red,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            // --- BOTÓN FINAL CON VALIDACIÓN ---
            Button(
                onClick = onNext,
                enabled = isFormValid && !state.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .shadow(if (isFormValid) 10.dp else 0.dp, RoundedCornerShape(18.dp)),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PurplePrimary,
                    disabledContainerColor = Color.Gray
                )
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(color = White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                } else {
                    Text("Continuar a Cámara", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = White)
                }
            }
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

// Estilo reutilizable para los campos
@Composable
fun textFieldStyle() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = PurplePrimary,
    unfocusedBorderColor = BorderGray,
    focusedContainerColor = White,
    unfocusedContainerColor = White,
    disabledBorderColor = BorderGray,
    disabledTextColor = Color.Black
)

@Composable
fun FormInputLabel(text: String) {
    Text(
        text = text,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = PurplePrimary,
        modifier = Modifier.padding(bottom = 5.dp)
    )
}

@Composable
fun FormSpacer() {
    Spacer(modifier = Modifier.height(18.dp))
}