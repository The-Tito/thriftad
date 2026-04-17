package com.polet.thriftadapp.navigation

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext

import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.hilt.navigation.compose.hiltViewModel

import com.polet.thriftadapp.presentation.screens.login.*
import com.polet.thriftadapp.presentation.screens.home.*
import com.polet.thriftadapp.presentation.screens.add.*
import com.polet.thriftadapp.presentation.screens.camera.*
import com.polet.thriftadapp.presentation.screens.places.*
import com.polet.thriftadapp.presentation.screens.profile.*
import com.polet.thriftadapp.presentation.screens.gallery.*
import com.polet.thriftadapp.presentation.screens.help.HelpScreen
import com.polet.thriftadapp.presentation.screens.privacy.PrivacyScreen
import com.polet.thriftadapp.presentation.viewmodels.*

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object AddTransaction : Screen("add")
    object Camera : Screen("camera/{nombre}/{monto}/{categoria}/{fecha}") {
        fun createRoute(nombre: String, monto: String, categoria: String, fecha: String) =
            "camera/${Uri.encode(nombre)}/${Uri.encode(monto)}/${Uri.encode(categoria)}/${Uri.encode(fecha)}"
    }
    object Map : Screen("map")
    object Profile : Screen("profile")
    object Home : Screen("home/{userId}/{role}/{userName}/{nombreCompleto}") {
        fun createRoute(userId: Int, role: String, userName: String, nombreCompleto: String = "") =
            "home/$userId/${Uri.encode(role)}/${Uri.encode(userName)}/${Uri.encode(nombreCompleto.ifBlank { "_" })}"
    }
}

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Login.route
) {
    val homeViewModel: HomeViewModel = hiltViewModel()
    val placesViewModel: PlacesViewModel = hiltViewModel()

    NavHost(navController = navController, startDestination = startDestination) {

        // --- LOGIN ---
        composable(Screen.Login.route) {
            val viewModel: LoginViewModel = hiltViewModel()
            val state by viewModel.uiState.collectAsState()
            LaunchedEffect(state.isLoginSuccess) {
                if (state.isLoginSuccess) {
                    navController.navigate(
                        Screen.Home.createRoute(state.userId, state.role, state.userName, state.nombreCompleto)
                    ) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            }
            LoginScreen(
                state = state,
                onEvent = { viewModel.onEvent(it) },
                onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                onNavigateToForgotPassword = { /* ... */ }
            )
        }

        // --- REGISTER ---
        composable(Screen.Register.route) {
            val regViewModel: RegisterViewModel = hiltViewModel()
            val regState by regViewModel.state.collectAsState()

            LaunchedEffect(regState.isSuccess) {
                if (regState.isSuccess) {
                    navController.navigate(
                        Screen.Home.createRoute(
                            regState.userId,
                            regState.selectedRole,
                            regState.userOrEmail,
                            regState.nombreCompleto
                        )
                    ) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            }

            RegisterScreen(
                onRegisterSuccess = { idGenerado, rolElegido ->
                    // Este callback se mantiene por compatibilidad, pero el LaunchedEffect de arriba es el que manda ahora
                },
                onBackToLogin = { navController.popBackStack() }
            )
        }

        // --- HOME ---
        composable(
            route = Screen.Home.route,
            arguments = listOf(
                navArgument("userId")        { type = NavType.IntType },
                navArgument("role")          { type = NavType.StringType },
                navArgument("userName")      { type = NavType.StringType },
                navArgument("nombreCompleto"){ type = NavType.StringType }
            )
        ) { backStackEntry ->
            val userId         = backStackEntry.arguments?.getInt("userId") ?: -1
            val role           = backStackEntry.arguments?.getString("role") ?: "estudiante"
            val userName       = backStackEntry.arguments?.getString("userName") ?: "Usuario"
            val nombreCompleto = backStackEntry.arguments?.getString("nombreCompleto")
                ?.takeIf { it != "_" } ?: ""

            LaunchedEffect(userId) {
                if (userId != -1) {
                    homeViewModel.loadUserData(userId)
                    homeViewModel.startTicketObserver(userId)
                    placesViewModel.setUserId(userId)
                }
            }

            HomeScreen(
                userId         = userId,
                userName       = userName,
                nombreCompleto = nombreCompleto,
                viewModel      = homeViewModel,
                onToggleNotifications = { },
                onShowModal = { _, _, _ -> },
                onAmountChange = { homeViewModel.onAmountChange(it) },
                onAdjustBalance = { homeViewModel.onConfirmAdjustment() },
                onDeleteTransaction = { id -> homeViewModel.onDeleteTransaction(id) }
            )
        }

        // --- RESTO DE PANTALLAS (Mantenidas exactamente igual) ---
        composable(Screen.AddTransaction.route) {
            val addViewModel: AddTransactionViewModel = hiltViewModel()
            val homeState by homeViewModel.state.collectAsState()

            // Pasar userId y role desde homeViewModel (única fuente fiable tras el login)
            LaunchedEffect(homeViewModel.currentUserId) {
                if (homeViewModel.currentUserId != -1) {
                    addViewModel.onEvent(AddTransactionEvent.UserIdSet(homeViewModel.currentUserId))
                }
            }
            LaunchedEffect(homeState.role) {
                if (homeState.role.isNotBlank()) {
                    addViewModel.onEvent(AddTransactionEvent.RoleSet(homeState.role))
                }
            }

            // Cuando el guardado es exitoso, navegamos a Camera con los datos del form
            LaunchedEffect(addViewModel.state.isSaved) {
                if (addViewModel.state.isSaved) {
                    val n   = addViewModel.state.nombre.ifBlank { "Gasto" }
                    val m   = addViewModel.state.monto.ifBlank { "0" }
                    val cat = addViewModel.state.categoria.ifBlank { "Otro" }
                    val f   = addViewModel.state.fecha.ifBlank { "" }
                    // Resetear ANTES de navegar para que al regresar con Editar
                    // este LaunchedEffect no vuelva a dispararse
                    addViewModel.onEvent(AddTransactionEvent.ResetSaved)
                    navController.navigate(Screen.Camera.createRoute(n, m, cat, f))
                }
            }

            AddTransactionScreen(
                state = addViewModel.state,
                onEvent = { addViewModel.onEvent(it) },
                // onNext ahora guarda primero; la navegación la maneja el LaunchedEffect
                onNext = { addViewModel.onEvent(AddTransactionEvent.SaveTransaction) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.Camera.route,
            arguments = listOf(
                navArgument("nombre")    { type = NavType.StringType },
                navArgument("monto")     { type = NavType.StringType },
                navArgument("categoria") { type = NavType.StringType },
                navArgument("fecha")     { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val nArg   = backStackEntry.arguments?.getString("nombre")    ?: ""
            val mArg   = backStackEntry.arguments?.getString("monto")     ?: ""
            val catArg = backStackEntry.arguments?.getString("categoria") ?: ""
            val fArg   = backStackEntry.arguments?.getString("fecha")     ?: ""
            val cameraViewModel: CameraViewModel = hiltViewModel()
            LaunchedEffect(nArg, mArg, catArg, fArg) {
                cameraViewModel.onEvent(CameraEvent.OnConceptChange(nArg))
                cameraViewModel.onEvent(CameraEvent.OnAmountChange(mArg))
                cameraViewModel.onEvent(CameraEvent.OnCategoriaChange(catArg))
                cameraViewModel.onEvent(CameraEvent.OnFechaChange(fArg))
            }
            LaunchedEffect(homeViewModel.currentUserId) {
                if (homeViewModel.currentUserId != -1)
                    cameraViewModel.onEvent(CameraEvent.UserIdSet(homeViewModel.currentUserId))
            }
            CameraScreen(
                state = cameraViewModel.state,
                onEvent = { event ->
                    when (event) {
                        is CameraEvent.ConfirmAndSave -> {
                            val foto = cameraViewModel.state.capturedImageUri?.toString() ?: ""
                            // PlacesViewModel: guarda en Room con userId + captura GPS
                            placesViewModel.registrarNuevoGasto(
                                homeViewModel.currentUserId,
                                event.concept, event.amount, foto
                            ) { total, ticketId ->
                                homeViewModel.registrarGastoDesdeTicket(total)
                                cameraViewModel.setTicketId(ticketId)
                            }
                            // CameraViewModel: sincroniza con backend
                            cameraViewModel.onEvent(event)
                            navController.popBackStack(Screen.Home.route, inclusive = false)
                        }
                        is CameraEvent.EditInformation -> navController.popBackStack()
                        else -> cameraViewModel.onEvent(event)
                    }
                },
                placesViewModel = placesViewModel
            )
        }

        composable(Screen.Map.route) { PlacesHistoryScreen(viewModel = placesViewModel) }

        composable(Screen.Profile.route) {
            val homeState by homeViewModel.state.collectAsState()
            val context = LocalContext.current
            ProfileScreen(
                userName            = homeState.userName.ifBlank { "Usuario" },
                onLogout            = {
                    context.getSharedPreferences("thriftad_prefs", Context.MODE_PRIVATE)
                        .edit().clear().apply()
                    navController.navigate(Screen.Login.route) { popUpTo(0) }
                },
                onNavigateToAccount = { navController.navigate("account_settings") },
                onNavigateToGallery = { navController.navigate("gallery") },
                onNavigateToHelp    = { navController.navigate("help") },
                onNavigateToPrivacy = { navController.navigate("privacy") }
            )
        }

        composable("account_settings") {
            val accountViewModel: AccountViewModel = hiltViewModel()
            // Al salir de esta pantalla, refrescar el nombre en HomeViewModel
            // (el usuario puede haberlo cambiado en Settings)
            DisposableEffect(Unit) {
                onDispose { homeViewModel.refreshNombreCompleto() }
            }
            AccountSettingsScreen(
                state               = accountViewModel.state,
                onIdentifierChange  = { accountViewModel.onIdentifierChange(it) },
                onRoleChange        = { accountViewModel.onRoleChange(it) },
                onVibrationToggle   = { accountViewModel.onVibrationToggle(it) },
                onSoundToggle       = { accountViewModel.onSoundToggle(it) },
                onBack              = { navController.popBackStack() }
            )
        }

        composable("help") {
            HelpScreen(onBack = { navController.popBackStack() })
        }

        composable("privacy") {
            PrivacyScreen(onBack = { navController.popBackStack() })
        }

        composable("gallery") {
            val galleryViewModel: GalleryViewModel = hiltViewModel()
            LaunchedEffect(homeViewModel.currentUserId) {
                if (homeViewModel.currentUserId != -1)
                    galleryViewModel.loadForUser(homeViewModel.currentUserId)
            }
            GalleryScreen(
                state                 = galleryViewModel.state,
                onSearchChange        = { galleryViewModel.onSearchChange(it) },
                onSelectTicket        = { galleryViewModel.onSelectTicket(it) },
                onRequestDelete       = { galleryViewModel.onRequestDelete(it) },
                onDeletePhotoOnly     = { galleryViewModel.deletePhotoOnly(it) },
                onDeleteCompleteGasto = { galleryViewModel.deleteCompleteGasto(it, userId = homeViewModel.currentUserId) },
                onDismissDeleteDialog = { galleryViewModel.onDismissDeleteDialog() },
                onBack                = { navController.popBackStack() }
            )
        }
    }
}