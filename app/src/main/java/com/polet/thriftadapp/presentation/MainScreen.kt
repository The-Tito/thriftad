package com.polet.thriftadapp.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.polet.thriftadapp.navigation.Screen
import com.polet.thriftadapp.navigation.NavGraph
import com.polet.thriftadapp.presentation.components.BottomMenuBar
import com.polet.thriftadapp.ui.theme.LightBG

@Composable
fun MainScreen() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("thriftad_prefs", android.content.Context.MODE_PRIVATE) }

    val startDestination = remember {
        val savedId = prefs.getInt("user_id", -1)
        if (savedId > 0) {
            Screen.Home.createRoute(
                savedId,
                prefs.getString("user_role", "estudiante") ?: "estudiante",
                prefs.getString("user_name", "Usuario") ?: "Usuario",
                prefs.getString("user_nombre", "") ?: ""
            )
        } else {
            Screen.Login.route
        }
    }

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Guardamos los datos del usuario la última vez que visitamos Home.
    // Esto asegura que al navegar desde Gallery/Profile/Add hacia Home,
    // los argumentos siguen siendo correctos (las otras rutas no los tienen).
    val initialId   = remember { prefs.getInt("user_id", -1).takeIf { it > 0 } ?: -1 }
    val initialRole = remember { prefs.getString("user_role", "estudiante") ?: "estudiante" }
    val initialName = remember { prefs.getString("user_name", "Usuario") ?: "Usuario" }
    val initialNom  = remember { prefs.getString("user_nombre", "") ?: "" }

    var savedUserId   by remember { mutableIntStateOf(initialId) }
    var savedRole     by remember { mutableStateOf(initialRole) }
    var savedUserName by remember { mutableStateOf(initialName) }
    var savedNombre   by remember { mutableStateOf(initialNom) }

    LaunchedEffect(currentRoute) {
        if (currentRoute?.startsWith("home/") == true) {
            savedUserId   = navBackStackEntry?.arguments?.getInt("userId")           ?: -1
            savedRole     = navBackStackEntry?.arguments?.getString("role")          ?: "estudiante"
            savedUserName = navBackStackEntry?.arguments?.getString("userName")      ?: "Usuario"
            savedNombre   = navBackStackEntry?.arguments?.getString("nombreCompleto")
                               ?.takeIf { it != "_" } ?: ""
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = LightBG
    ) {
        Scaffold(
            modifier = Modifier.statusBarsPadding(),
            bottomBar = {
                val authRoutes = listOf("login", "register", "forgot_password")
                val hideBottomBar = currentRoute == null || authRoutes.any { currentRoute.contains(it) }

                if (!hideBottomBar) {
                    BottomMenuBar(currentRoute = currentRoute) { targetRoute ->
                        val finalRoute = if (targetRoute == "home") {
                            Screen.Home.createRoute(savedUserId, savedRole, savedUserName, savedNombre)
                        } else {
                            targetRoute
                        }

                        navController.navigate(finalRoute) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            }
        ) { padding ->
            Box(modifier = Modifier.padding(padding)) {
                NavGraph(navController = navController, startDestination = startDestination)
            }
        }
    }
}