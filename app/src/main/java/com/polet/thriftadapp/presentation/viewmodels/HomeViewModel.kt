package com.polet.thriftadapp.presentation.viewmodels

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.polet.thriftadapp.data.local.dao.GoalDao
import com.polet.thriftadapp.data.local.dao.HiddenTransactionDao
import com.polet.thriftadapp.data.local.dao.TicketDao
import com.polet.thriftadapp.data.local.entities.GoalEntity
import com.polet.thriftadapp.data.local.entities.HiddenTransactionEntity
import com.polet.thriftadapp.data.remote.ApiService
import com.polet.thriftadapp.domain.model.MovimientoRequest
import com.polet.thriftadapp.domain.model.TransactionResponse
import com.polet.thriftadapp.domain.use_case.GetHomeDataUseCase
import com.polet.thriftadapp.presentation.screens.home.HomeState
import com.polet.thriftadapp.presentation.screens.home.MetaItem
import com.polet.thriftadapp.presentation.screens.home.PieChartData
import com.polet.thriftadapp.presentation.screens.home.Transaction
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalDate
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val ticketDao: TicketDao,
    private val goalDao: GoalDao,
    private val hiddenTransactionDao: HiddenTransactionDao,
    private val getHomeDataUseCase: GetHomeDataUseCase,
    private val apiService: ApiService,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences("thriftad_prefs", Context.MODE_PRIVATE)
    }

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    var currentUserId: Int by mutableStateOf(-1)
        private set

    // Saldo en memoria para validaciones instantáneas (+/- modal)
    private var saldoActualPersistente = 0.0

    companion object {
        private const val TAG = "HomeViewModel"
    }

    // Escucha cambios en TicketEntity de Room y recarga el backend cuando hay uno nuevo
    fun startTicketObserver(userId: Int) {
        viewModelScope.launch {
            ticketDao.getVisibleTickets(userId).drop(1).collect {
                Log.d(TAG, "Ticket nuevo en Room — recargando backend (userId=$userId)")
                loadUserData(userId)
            }
        }
    }

    // Refresca el nombre de pantalla desde prefs tras volver de AccountSettings
    fun refreshNombreCompleto() {
        val nombre = prefs.getString("user_nombre", "")?.takeIf { it.isNotBlank() }
            ?: prefs.getString("user_name", "") ?: ""
        if (nombre.isNotBlank()) {
            _state.update { it.copy(nombreCompleto = nombre) }
        }
    }

    // ── CARGA PRINCIPAL — solo backend ──────────────────────────────────────────
    fun loadUserData(userId: Int) {
        Log.d(TAG, "loadUserData($userId) iniciado")
        currentUserId = userId

        viewModelScope.launch {
            val nombreDePrefs = prefs.getString("user_nombre", "")?.takeIf { it.isNotBlank() }
                ?: prefs.getString("user_name", "") ?: ""

            _state.update {
                it.copy(
                    transactions   = emptyList(),
                    balance        = 0.0,
                    isLoading      = true,
                    error          = null,
                    nombreCompleto = nombreDePrefs
                )
            }
            saldoActualPersistente = 0.0

            try {
                val response = getHomeDataUseCase(userId)
                if (response.isSuccessful && response.body() != null) {
                    val data = response.body()!!
                    Log.d(TAG, "Backend OK — balance=${data.balance} role='${data.role}' movimientos=${data.transactions.size}")

                    saldoActualPersistente = data.balance

                    val hiddenIds     = hiddenTransactionDao.getHiddenIds(userId).toSet()
                    val goalEntities  = goalDao.getGoalsByUserId(userId)
                    val metas = goalEntities.map { g ->
                        MetaItem(
                            name          = g.goalName,
                            targetAmount  = g.targetAmount,
                            currentAmount = g.currentAmount
                        )
                    }
                    val reservadoEnMetas  = metas.sumOf { it.targetAmount }
                    val balanceDisponible = (data.balance - reservadoEnMetas).coerceAtLeast(0.0)
                    val monthlyIncome     = data.transactions.filter { it.isInc }.sumOf { it.amount }
                    val visibleTxs        = data.transactions.filter { it.id !in hiddenIds }

                    _state.update {
                        it.copy(
                            userName          = data.userName,
                            role              = data.role,
                            balance           = data.balance,
                            balanceDisponible = balanceDisponible,
                            savingMeta        = reservadoEnMetas,
                            monthlyBudget     = monthlyIncome,
                            metas             = metas,
                            chartData         = calcularChartData(data.transactions),
                            transactions      = visibleTxs.map { t ->
                                Transaction(
                                    id       = t.id,
                                    name     = t.name,
                                    amount   = t.amount,
                                    category = t.category,
                                    date     = t.date,
                                    isInc    = t.isInc
                                )
                            },
                            error     = null,
                            isLoading = false
                        )
                    }
                } else {
                    Log.w(TAG, "Backend HTTP ${response.code()}")
                    _state.update {
                        it.copy(isLoading = false, error = "Error del servidor (${response.code()})")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Sin conexión: ${e.message}")
                _state.update { it.copy(isLoading = false, error = "Sin conexión al servidor") }
            }
        }
    }

    // ── MODAL DE AJUSTE (+/-/Meta) ───────────────────────────────────────────────
    fun onShowModal(isVisible: Boolean, isMeta: Boolean, isSum: Boolean) {
        _state.update { it.copy(isModalVisible = isVisible, isEditingMeta = isMeta, isAdding = isSum) }
    }

    fun onAmountChange(newAmount: String) {
        _state.update { it.copy(quickAmount = newAmount) }
    }

    fun onConfirmAdjustment() {
        val amount   = _state.value.quickAmount.toDoubleOrNull() ?: 0.0
        val esMeta   = _state.value.isEditingMeta
        val esAñadir = _state.value.isAdding

        _state.update { it.copy(error = null) }

        if (!esAñadir && !esMeta && amount > saldoActualPersistente) {
            _state.update { it.copy(error = "Saldo insuficiente. ¡Agrega un poco de dinero primero! ✨") }
            return
        }

        if (esMeta) {
            guardarMeta("Meta de ahorro", amount)
            _state.update { it.copy(isModalVisible = false, quickAmount = "") }
        } else {
            if (esAñadir) saldoActualPersistente += amount else saldoActualPersistente -= amount
            _state.update { it.copy(
                balance        = saldoActualPersistente,
                isModalVisible = false,
                quickAmount    = ""
            )}
            enviarAjusteAlBackend(amount = amount, esIngreso = esAñadir)
        }
    }

    // Actualiza saldo en memoria cuando se confirma un ticket desde Cámara
    fun registrarGastoDesdeTicket(monto: Double) {
        saldoActualPersistente -= monto
        _state.update { it.copy(balance = saldoActualPersistente) }
    }

    // Envía el ajuste de saldo al backend con hasta 10 reintentos cada 30 segundos
    private fun enviarAjusteAlBackend(amount: Double, esIngreso: Boolean) {
        if (currentUserId == -1) return
        val request = MovimientoRequest(
            idUsuario      = currentUserId,
            idCategoria    = 0,
            nombreProducto = if (esIngreso) "Ingreso manual" else "Gasto manual",
            monto          = amount,
            fecha          = LocalDate.now().toString(),
            descripcion    = if (esIngreso) "Saldo añadido desde Home" else "Saldo descontado desde Home",
            esIngreso      = esIngreso,
            ubicacion      = "",
            imagenBase64   = ""
        )

        viewModelScope.launch {
            var enviado = false
            var intento = 0
            while (!enviado && intento < 10) {
                try {
                    val response = apiService.crearMovimiento(request)
                    if (response.isSuccessful) {
                        enviado = true
                        Log.d(TAG, "Ajuste enviado al backend ✓")
                        loadUserData(currentUserId)
                        return@launch
                    } else {
                        Log.w(TAG, "Backend rechazó ajuste HTTP ${response.code()} — intento $intento")
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Sin conexión — reintentando en 30s (intento $intento): ${e.message}")
                }
                intento++
                if (!enviado) delay(30_000)
            }
            Log.e(TAG, "Ajuste no enviado tras $intento intentos")
            _state.update { it.copy(error = "Sin conexión. Verifica tu red e intenta de nuevo.") }
        }
    }

    // ── METAS ────────────────────────────────────────────────────────────────────
    fun guardarMeta(nombre: String, targetAmount: Double) {
        if (nombre.isBlank() || targetAmount <= 0.0) return
        viewModelScope.launch {
            val existing = goalDao.getGoalsByUserId(currentUserId)
                .firstOrNull { it.goalName.equals(nombre, ignoreCase = true) }
            if (existing != null) {
                goalDao.update(existing.copy(targetAmount = targetAmount))
            } else {
                goalDao.insert(GoalEntity(
                    userId       = currentUserId,
                    goalName     = nombre,
                    targetAmount = targetAmount
                ))
            }
            if (currentUserId != -1) loadUserData(currentUserId)
        }
    }

    // ── GRÁFICO ──────────────────────────────────────────────────────────────────

    // Mapea IDs numéricos a nombres de categoría.
    // IDs 1-8: hogar/estudiante. IDs 9-14: negocio. 0/"": Otros.
    private fun mapearCategoria(raw: String): String = when (raw.trim()) {
        "1"  -> "Alimentación"
        "2"  -> "Transporte"
        "3"  -> "Entretenimiento"
        "4"  -> "Salud"
        "5"  -> "Ropa"
        "6"  -> "Hogar"
        "7"  -> "Materiales de estudio"
        "8"  -> "Educación"
        "9"  -> "Ventas"
        "10" -> "Compras"
        "11" -> "Gastos operacionales"
        "12" -> "Salarios"
        "13" -> "Impuestos"
        "14" -> "Servicios"
        "0", "" -> "Otros"
        else -> raw   // nombre directo si el backend lo envía así
    }

    // Color fijo por categoría — consistente entre sesiones y roles
    private fun colorParaCategoria(categoria: String): Color = when (categoria) {
        // Hogar / Estudiante
        "Alimentación"          -> Color(0xFF9575CD)
        "Transporte"            -> Color(0xFF5C6BC0)
        "Entretenimiento"       -> Color(0xFFEF5350)
        "Salud"                 -> Color(0xFF26A69A)
        "Ropa"                  -> Color(0xFF66BB6A)
        "Hogar"                 -> Color(0xFFFFCA28)
        "Materiales de estudio" -> Color(0xFF8D6E63)
        "Educación"             -> Color(0xFF42A5F5)
        // Negocio
        "Ventas"                -> Color(0xFF7E57C2)
        "Compras"               -> Color(0xFF26C6DA)
        "Gastos operacionales"  -> Color(0xFFEF5350)
        "Salarios"              -> Color(0xFF66BB6A)
        "Impuestos"             -> Color(0xFFFF7043)
        "Servicios"             -> Color(0xFFFFCA28)
        // Fallback
        else                    -> Color(0xFF78909C)
    }

    private fun calcularChartData(transactions: List<TransactionResponse>): List<PieChartData> {
        Log.d(TAG, "calcularChartData: ${transactions.size} totales | " +
            "gastos=${transactions.count { !it.isInc }} ingresos=${transactions.count { it.isInc }}")

        if (transactions.isEmpty()) return emptyList()

        // Prioridad: solo gastos (isInc=false).
        // Fallback: todos los movimientos, por si isInc no se deserializó correctamente.
        val fuente = transactions.filter { !it.isInc }.takeIf { it.isNotEmpty() }
            ?: transactions.also {
                Log.w(TAG, "Sin gastos detectados — usando todos los movimientos como fallback")
            }

        val topEntries = fuente
            .groupBy { mapearCategoria(it.category.ifBlank { "0" }) }
            .entries
            .sortedByDescending { (_, items) -> items.sumOf { it.amount } }
            .take(6)   // hasta 6 categorías

        val totalShown = topEntries.sumOf { (_, items) -> items.sumOf { it.amount } }
        if (totalShown <= 0) return emptyList()

        return topEntries.map { (categoria, items) ->
            val pct = (items.sumOf { it.amount } / totalShown * 100).toFloat()
            Log.d(TAG, "  → '$categoria' ${"%.1f".format(pct)}%")
            PieChartData(categoria, pct, colorParaCategoria(categoria))
        }
    }

    // ── OCULTAR MOVIMIENTO DE LA VISTA HOME ──────────────────────────────────────
    // Elimina de la lista en memoria Y persiste el ocultamiento en Room.
    // NO llama al backend, NO modifica el saldo.
    // El registro sigue visible en Lugares y Galería.
    fun onDeleteTransaction(movimientoId: String) {
        _state.update { it.copy(
            transactions = it.transactions.filter { t -> t.id != movimientoId }
        )}
        viewModelScope.launch {
            hiddenTransactionDao.hide(HiddenTransactionEntity(movimientoId, currentUserId))
        }
    }
}
