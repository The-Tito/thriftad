package com.polet.thriftadapp.presentation.screens.camera

// ESTADO: La única fuente de verdad
data class CameraState(
    val userId: Int = -1,
    val isPhotoTaken: Boolean = false,
    val capturedImageUri: String? = null,
    val concept: String = "",
    val amount: String = "",
    val categoria: String = "",
    val fecha: String = "",
    val errorMessage: String? = null
)

// EVENTOS: Lo que la pantalla le avisa al ViewModel
sealed class CameraEvent {
    data class UserIdSet(val userId: Int) : CameraEvent()
    object CapturePhoto : CameraEvent()
    data class OnPhotoTaken(val uri: String) : CameraEvent()
    object CancelAndRepeat : CameraEvent()
    object EditInformation : CameraEvent()

    data class OnConceptChange(val concept: String) : CameraEvent()
    data class OnAmountChange(val amount: String) : CameraEvent()
    data class OnCategoriaChange(val categoria: String) : CameraEvent()
    data class OnFechaChange(val fecha: String) : CameraEvent()

    data class ConfirmAndSave(val concept: String, val amount: String) : CameraEvent()
}