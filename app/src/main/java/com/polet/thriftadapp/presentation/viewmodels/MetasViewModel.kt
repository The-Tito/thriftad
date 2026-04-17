package com.polet.thriftadapp.presentation.viewmodels

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.polet.thriftadapp.data.local.dao.GoalDao
import com.polet.thriftadapp.data.local.entities.GoalEntity
import com.polet.thriftadapp.data.remote.ApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MetasViewModel @Inject constructor(
    private val goalDao: GoalDao,
    private val apiService: ApiService
) : ViewModel() {

    data class MetasState(
        val goals: List<GoalEntity> = emptyList(),
        val availableBalance: Double = 0.0,
        val totalGoalsAmount: Double = 0.0,
        val isLoading: Boolean = false,
        val error: String? = null
    )

    private val _state = mutableStateOf(MetasState())
    val state: State<MetasState> = _state

    fun loadGoals(userId: Int) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            try {
                val goals      = goalDao.getGoalsByUserId(userId)
                val totalGoals = goals.sumOf { it.targetAmount }
                _state.value = _state.value.copy(
                    goals            = goals,
                    totalGoalsAmount = totalGoals,
                    isLoading        = false
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = e.message, isLoading = false)
            }
        }
    }

    fun createGoal(userId: Int, goalName: String, targetAmount: Double) {
        viewModelScope.launch {
            try {
                goalDao.insert(GoalEntity(userId = userId, goalName = goalName, targetAmount = targetAmount))
                loadGoals(userId)
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = e.message)
            }
        }
    }

    fun updateGoal(goalId: Int, userId: Int, newAmount: Double) {
        viewModelScope.launch {
            try {
                val goal = goalDao.getGoalById(goalId)
                if (goal != null) {
                    goalDao.update(goal.copy(targetAmount = newAmount))
                    loadGoals(userId)
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = e.message)
            }
        }
    }

    fun deleteGoal(goalId: Int, userId: Int) {
        viewModelScope.launch {
            try {
                val goal = goalDao.getGoalById(goalId)
                if (goal != null) {
                    goalDao.delete(goal)
                    loadGoals(userId)
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = e.message)
            }
        }
    }
}
