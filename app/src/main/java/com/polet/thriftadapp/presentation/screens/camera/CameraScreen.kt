package com.polet.thriftadapp.presentation.screens.camera

import android.content.ContentValues
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.content.Context
import android.location.LocationManager
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.polet.thriftadapp.presentation.viewmodels.PlacesViewModel
import com.polet.thriftadapp.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraScreen(
    state: CameraState,
    onEvent: (CameraEvent) -> Unit,
    placesViewModel: PlacesViewModel
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember { PreviewView(context) }
    val imageCapture: ImageCapture = remember { ImageCapture.Builder().build() }
    val cameraExecutor: ExecutorService = remember { Executors.newSingleThreadExecutor() }
    var isCameraReady by remember { mutableStateOf(false) }

    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)

    // --- LÓGICA DE CÁMARA ---
    LaunchedEffect(Unit) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            try {
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().also { it.setSurfaceProvider(previewView.surfaceProvider) }
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageCapture)
                isCameraReady = true
            } catch (e: Exception) { Log.e("CameraScreen", "Error: ${e.message}") }
        }, ContextCompat.getMainExecutor(context))
    }

    // --- FUNCIÓN DE DISPARO (Fuera del Scaffold para evitar errores de scope) ---
    fun takePhoto() {
        val name = "ThriftAd_" + SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/ThriftAd-Tickets")
            }
        }
        val outputOptions = ImageCapture.OutputFileOptions.Builder(
            context.contentResolver,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        ).build()

        imageCapture.takePicture(outputOptions, cameraExecutor, object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                onEvent(CameraEvent.OnPhotoTaken(outputFileResults.savedUri?.toString() ?: ""))
                onEvent(CameraEvent.CapturePhoto)
            }
            override fun onError(exception: ImageCaptureException) { Log.e("CameraScreen", "Error") }
        })
    }

    // --- DISEÑO DE LA INTERFAZ ---
    Scaffold(containerColor = LightBG) { padding ->
        // Si ya hay foto, permitimos scroll en toda la pantalla para que bajen las casillas y botones juntos
        val screenModifier = if (state.isPhotoTaken) {
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())
        } else {
            Modifier.fillMaxSize().padding(padding)
        }

        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = screenModifier.padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // 1. AVISO GPS
                if (!isGpsEnabled) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOff, null, tint = Color.Red, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Enciende tu GPS para ubicación exacta", color = Color.Red, fontSize = 12.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                Text("📸 Captura de Ticket", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = PurplePrimary)
                Spacer(modifier = Modifier.height(20.dp))

                // 2. BOX DE LA CÁMARA / VISTA PREVIA
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(360.dp)
                        .shadow(12.dp, RoundedCornerShape(30.dp))
                        .clip(RoundedCornerShape(30.dp))
                        .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    if (isCameraReady && !state.isPhotoTaken) {
                        AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize())
                    } else if (state.isPhotoTaken) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.CheckCircle, null, tint = Color.Green, modifier = Modifier.size(50.dp))
                            Text("Foto Capturada", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        CircularProgressIndicator(color = PurplePrimary)
                    }
                }

                // 3. RESUMEN Y BOTONES (tras capturar foto)
                if (state.isPhotoTaken) {
                    Spacer(modifier = Modifier.height(20.dp))

                    // Tarjeta de resumen (solo lectura)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Text(
                                "Resumen del gasto",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = PurplePrimary,
                                modifier = Modifier.padding(bottom = 14.dp)
                            )
                            SummaryRow(label = "Nombre",    value = state.concept.ifBlank { "—" })
                            SummaryRow(label = "Monto",     value = if (state.amount.isNotBlank()) "$${state.amount}" else "—")
                            SummaryRow(label = "Categoría", value = state.categoria.ifBlank { "—" })
                            SummaryRow(label = "Fecha",     value = state.fecha.ifBlank { "—" })
                            SummaryRow(
                                label = "Ubicación",
                                value = if (isGpsEnabled) "GPS activado" else "GPS no disponible",
                                valueColor = if (isGpsEnabled) Color(0xFF388E3C) else Color(0xFFB71C1C)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // BOTONES DE ACCIÓN
                    Button(
                        onClick = { onEvent(CameraEvent.ConfirmAndSave(state.concept, state.amount)) },
                        modifier = Modifier.fillMaxWidth().height(55.dp),
                        shape = RoundedCornerShape(15.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                    ) {
                        Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Confirmar Gasto", fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = { onEvent(CameraEvent.EditInformation) },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(15.dp),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, PurplePrimary)
                    ) {
                        Icon(Icons.Default.Edit, null, tint = PurplePrimary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Editar", color = PurplePrimary)
                    }

                    TextButton(onClick = { onEvent(CameraEvent.CancelAndRepeat) }) {
                        Text("Repetir Foto", color = Color.Red)
                    }

                    Spacer(modifier = Modifier.height(30.dp))
                }
            }

            // 4. BOTÓN DE CAPTURA FIJO (Solo visible antes de la foto)
            if (!state.isPhotoTaken) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(bottom = 50.dp),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    IconButton(
                        onClick = { takePhoto() },
                        modifier = Modifier
                            .size(85.dp)
                            .background(Color.White, CircleShape)
                            .border(5.dp, PurplePrimary, CircleShape)
                            .shadow(8.dp, CircleShape)
                    ) {
                        Box(modifier = Modifier.size(60.dp).background(PurplePrimary, CircleShape))
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryRow(
    label: String,
    value: String,
    valueColor: Color = Color(0xFF212121)
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = Color(0xFF757575),
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(0.4f)
        )
        Text(
            text = value,
            fontSize = 13.sp,
            color = valueColor,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(0.6f)
        )
    }
}