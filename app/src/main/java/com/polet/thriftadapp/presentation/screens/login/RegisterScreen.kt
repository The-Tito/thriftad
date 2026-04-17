package com.polet.thriftadapp.presentation.screens.login

import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import com.polet.thriftadapp.presentation.viewmodels.RegisterViewModel
import com.polet.thriftadapp.ui.theme.*
import androidx.core.content.ContextCompat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    viewModel: RegisterViewModel = hiltViewModel(),
    onRegisterSuccess: (Int, String) -> Unit,
    onBackToLogin: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    var expanded by remember { mutableStateOf(false) }

    val biometricManager = remember { androidx.biometric.BiometricManager.from(context) }
    val canUseBiometric = biometricManager.canAuthenticate(
        androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
    ) == androidx.biometric.BiometricManager.BIOMETRIC_SUCCESS

    // --- ESCUCHA AL SERVIDOR ---
    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) {
            val idFinal  = state.userId
            val activity = context as? FragmentActivity

            if (canUseBiometric && state.wantsBiometric && activity != null) {
                showBiometricPrompt(activity,
                    onAuthSuccess = {
                        viewModel.saveBiometricEnabled()
                        onRegisterSuccess(idFinal, state.selectedRole.lowercase())
                    },
                    onSkip = {
                        onRegisterSuccess(idFinal, state.selectedRole.lowercase())
                    }
                )
            } else {
                onRegisterSuccess(idFinal, state.selectedRole.lowercase())
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LightBG)
            .padding(horizontal = 30.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Shield,
            contentDescription = null,
            tint = PurplePrimary,
            modifier = Modifier.size(80.dp)
        )

        Text("Nueva Cuenta", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = PurplePrimary)

        Spacer(modifier = Modifier.height(30.dp))

        OutlinedTextField(
            value = state.nombreCompleto,
            onValueChange = { viewModel.onNombreCompletoChange(it) },
            label = { Text("Nombre completo (o apodo)") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            leadingIcon = { Icon(Icons.Default.Badge, null, tint = PurplePrimary) },
            singleLine = true
        )

        Spacer(modifier = Modifier.height(15.dp))

        OutlinedTextField(
            value = state.userOrEmail,
            onValueChange = { viewModel.onUserOrEmailChange(it) },
            label = { Text("Nombre de usuario o Correo") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            leadingIcon = { Icon(Icons.Default.Person, null, tint = PurplePrimary) },
            singleLine = true
        )

        Spacer(modifier = Modifier.height(15.dp))

        OutlinedTextField(
            value = state.password,
            onValueChange = { viewModel.onPasswordChange(it) },
            label = { Text("Contraseña (min. 6 caracteres)") },
            visualTransformation = if (state.isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { viewModel.togglePasswordVisibility() }) {
                    Icon(
                        imageVector = if (state.isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = null
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            leadingIcon = { Icon(Icons.Default.Lock, null, tint = PurplePrimary) },
            singleLine = true
        )

        Spacer(modifier = Modifier.height(15.dp))

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = state.selectedRole,
                onValueChange = {},
                readOnly = true,
                label = { Text("Rol de Usuario") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                leadingIcon = { Icon(Icons.Default.Badge, null, tint = PurplePrimary) }
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                listOf("Estudiante", "Hogar", "Negocio").forEach { role ->
                    DropdownMenuItem(
                        text = { Text(role) },
                        onClick = {
                            viewModel.onRoleChange(role)
                            expanded = false
                        }
                    )
                }
            }
        }

        if (state.error != null) {
            Text(
                text = state.error!!,
                color = Color.Red,
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        if (canUseBiometric) {
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Checkbox(
                    checked = state.wantsBiometric,
                    onCheckedChange = { viewModel.toggleBiometric() },
                    colors = CheckboxDefaults.colors(checkedColor = PurplePrimary)
                )
                Text(
                    "Registrar huella dactilar para próximos inicios",
                    fontSize = 13.sp,
                    color = Color.DarkGray,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = { viewModel.onRegisterClicked() },
            enabled = state.userOrEmail.length >= 3 && state.password.length >= 6 && !state.isLoading,
            modifier = Modifier.fillMaxWidth().height(55.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary),
            shape = RoundedCornerShape(12.dp)
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text("Crear Cuenta", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        TextButton(onClick = onBackToLogin) {
            Text("¿Ya tienes cuenta? Inicia sesión", color = Color.Gray)
        }
    }
}

fun showBiometricPrompt(
    activity: FragmentActivity,
    onAuthSuccess: () -> Unit,
    onSkip: () -> Unit = {}
) {
    val executor = ContextCompat.getMainExecutor(activity)
    val biometricPrompt = BiometricPrompt(activity, executor, object : BiometricPrompt.AuthenticationCallback() {
        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
            super.onAuthenticationSucceeded(result)
            onAuthSuccess()
        }
        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
            super.onAuthenticationError(errorCode, errString)
            // El usuario canceló → continuar sin huella
            onSkip()
        }
    })

    val promptInfo = BiometricPrompt.PromptInfo.Builder()
        .setTitle("Vincular Huella Dactilar")
        .setSubtitle("Usa tu sensor para futuros inicios de sesión")
        .setNegativeButtonText("Omitir")
        .build()

    biometricPrompt.authenticate(promptInfo)
}