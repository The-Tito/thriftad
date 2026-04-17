package com.polet.thriftadapp.presentation.screens.login

import android.content.Context
import androidx.biometric.BiometricPrompt
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.polet.thriftadapp.R
import com.polet.thriftadapp.ui.theme.PurplePrimary
import com.polet.thriftadapp.ui.theme.LightBG

@Composable
fun LoginScreen(
    state: LoginState,
    onEvent: (LoginEvent) -> Unit,
    onNavigateToRegister: () -> Unit,
    onNavigateToForgotPassword: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    var passwordVisible by remember { mutableStateOf(false) }

    val biometricManager = remember { androidx.biometric.BiometricManager.from(context) }
    val hardwareOk = biometricManager.canAuthenticate(
        androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
    ) == androidx.biometric.BiometricManager.BIOMETRIC_SUCCESS

    // Solo mostrar el botón si el hardware está disponible Y el usuario ya registró su huella
    val prefs = remember { context.getSharedPreferences("thriftad_prefs", Context.MODE_PRIVATE) }
    val biometricEnabled = remember { prefs.getBoolean("biometric_enabled", false) }
    val canUseBiometric = hardwareOk && biometricEnabled

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LightBG)
            .verticalScroll(scrollState)
            .padding(bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(50.dp))

        Box(
            modifier = Modifier
                .size(110.dp)
                .clip(CircleShape)
                .background(Color.White)
                .border(3.dp, PurplePrimary, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.panda_logo),
                contentDescription = "Logo",
                modifier = Modifier.size(75.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))
        Text(text = "ThriftAd", color = PurplePrimary, fontSize = 32.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(30.dp))

        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {

                Text(text = "USUARIO", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = PurplePrimary)
                OutlinedTextField(
                    value = state.usuario,
                    onValueChange = { onEvent(LoginEvent.UsuarioChanged(it)) },
                    placeholder = { Text("Nombre o Correo", color = Color.LightGray) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                Text(text = "CONTRASEÑA", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = PurplePrimary)
                OutlinedTextField(
                    value = state.contrasena,
                    onValueChange = { onEvent(LoginEvent.ContrasenaChanged(it)) },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = null,
                                tint = PurplePrimary
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    )
                )

                if (state.error != null) {
                    Text(text = state.error, color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = { onEvent(LoginEvent.EntrarClicked) },
                    modifier = Modifier.fillMaxWidth().height(55.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary),
                    enabled = !state.isLoading
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text("Entrar", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        AnimatedVisibility(visible = canUseBiometric) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(modifier = Modifier.height(20.dp))
                OutlinedButton(
                    onClick = {
                        val activity = context as? FragmentActivity
                        if (activity != null) {
                            showLoginBiometricPrompt(activity,
                                onSuccess = { onEvent(LoginEvent.BiometricLoginTriggered) },
                                onError   = { msg -> onEvent(LoginEvent.ContrasenaChanged("")) }
                            )
                        }
                    },
                    modifier = Modifier.size(60.dp),
                    shape = CircleShape,
                    border = androidx.compose.foundation.BorderStroke(1.dp, PurplePrimary),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(Icons.Default.Fingerprint, null, tint = PurplePrimary, modifier = Modifier.size(35.dp))
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text("Entrar con huella", fontSize = 11.sp, color = PurplePrimary)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        TextButton(onClick = onNavigateToRegister) {
            Text("¿No tienes cuenta? Regístrate aquí", color = PurplePrimary, fontWeight = FontWeight.Bold)
        }
    }
}

fun showLoginBiometricPrompt(
    activity: FragmentActivity,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    val executor = ContextCompat.getMainExecutor(activity)
    val prompt = BiometricPrompt(activity, executor, object : BiometricPrompt.AuthenticationCallback() {
        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
            super.onAuthenticationSucceeded(result)
            onSuccess()
        }
        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
            super.onAuthenticationError(errorCode, errString)
            if (errorCode != BiometricPrompt.ERROR_NEGATIVE_BUTTON &&
                errorCode != BiometricPrompt.ERROR_USER_CANCELED) {
                onError(errString.toString())
            }
        }
        override fun onAuthenticationFailed() {
            super.onAuthenticationFailed()
            onError("Huella no reconocida")
        }
    })

    val info = BiometricPrompt.PromptInfo.Builder()
        .setTitle("Autenticación biométrica")
        .setSubtitle("Usa tu huella para acceder")
        .setNegativeButtonText("Cancelar")
        .build()

    prompt.authenticate(info)
}