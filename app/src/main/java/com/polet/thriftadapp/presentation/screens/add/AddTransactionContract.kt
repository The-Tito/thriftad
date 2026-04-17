package com.polet.thriftadapp.presentation.screens.add

// El "DNI" de lo que la pantalla muestra
data class AddTransactionState(
    val userId: Int = -1,
    val role: String = "estudiante",
    val nombre: String = "",
    val monto: String = "",
    val categoria: String = "Materiales de estudio",
    val unidad: String = "Piezas",
    val cantidad: String = "1",
    val fecha: String = "22/03/2026",
    val descripcion: String = "",
    val isCategoriaExpanded: Boolean = false,
    val isUnidadExpanded: Boolean = false,
    val categoriasDisponibles: List<String> = categoriasPorRol("estudiante"),
    val unidadesDisponibles: List<String> = listOf("Piezas", "Kilogramos", "Litros"),
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null
)

// Todo lo que el usuario puede tocar en tu diseño
sealed class AddTransactionEvent {
    data class UserIdSet(val userId: Int) : AddTransactionEvent()
    data class RoleSet(val role: String) : AddTransactionEvent()
    data class NombreChanged(val value: String) : AddTransactionEvent()
    data class MontoChanged(val value: String) : AddTransactionEvent()
    data class CategoriaSelected(val value: String) : AddTransactionEvent()
    data class UnidadSelected(val value: String) : AddTransactionEvent()
    data class CantidadChanged(val value: String) : AddTransactionEvent()
    data class DescripcionChanged(val value: String) : AddTransactionEvent()
    data class FechaChanged(val value: String) : AddTransactionEvent()
    object ToggleCategoriaMenu : AddTransactionEvent()
    object ToggleUnidadMenu : AddTransactionEvent()
    object SaveTransaction : AddTransactionEvent()
    object ResetSaved : AddTransactionEvent()
}

fun categoriasPorRol(role: String): List<String> = when (role.lowercase()) {
    "negocio" -> listOf("Ventas", "Compras", "Gastos operacionales", "Salarios", "Impuestos", "Servicios", "Otro")
    "hogar"   -> listOf("Alimentación", "Servicios", "Transporte", "Entretenimiento", "Salud", "Hogar", "Otro")
    else      -> listOf("Materiales de estudio", "Transporte", "Alimentación", "Entretenimiento", "Educación", "Otro")
}
