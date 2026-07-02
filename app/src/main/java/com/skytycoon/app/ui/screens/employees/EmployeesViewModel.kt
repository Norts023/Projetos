package com.skytycoon.app.ui.screens.employees

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skytycoon.app.data.repository.EmployeeRepository
import com.skytycoon.app.data.repository.GameStateRepository
import com.skytycoon.app.domain.model.Employee
import com.skytycoon.app.domain.model.EmployeeType
import com.skytycoon.app.domain.model.GameState
import com.skytycoon.app.domain.model.UseCaseResult
import com.skytycoon.app.domain.usecase.HireEmployeeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EmployeesUiState(
    val employees: List<Employee> = emptyList(),
    val gameState: GameState? = null,
    val filterType: EmployeeType? = null,
    val showHireDialog: Boolean = false,
    val isLoading: Boolean = false,
    val errorMsg: String? = null,
    val successMsg: String? = null
)

@HiltViewModel
class EmployeesViewModel @Inject constructor(
    private val employeeRepository: EmployeeRepository,
    private val gameStateRepository: GameStateRepository,
    private val hireEmployeeUseCase: HireEmployeeUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(EmployeesUiState())
    val uiState: StateFlow<EmployeesUiState> = _uiState.asStateFlow()

    val filteredEmployees: StateFlow<List<Employee>> = uiState
        .map { s ->
            if (s.filterType != null) s.employees.filter { it.type == s.filterType }
            else s.employees
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            employeeRepository.getAll().collect { employees ->
                _uiState.update { it.copy(employees = employees) }
            }
        }
        viewModelScope.launch {
            gameStateRepository.get().collect { gameState ->
                _uiState.update { it.copy(gameState = gameState) }
            }
        }
    }

    fun onFilterChange(type: EmployeeType?) {
        _uiState.update { it.copy(filterType = type) }
    }

    fun onShowHireDialog() {
        _uiState.update { it.copy(showHireDialog = true) }
    }

    fun onDismissDialog() {
        _uiState.update { it.copy(showHireDialog = false) }
    }

    fun onHire(name: String, type: EmployeeType, level: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val result = hireEmployeeUseCase(name, type, level)) {
                is UseCaseResult.Success -> _uiState.update {
                    it.copy(
                        isLoading = false,
                        showHireDialog = false,
                        successMsg = "${result.data.name} hired!"
                    )
                }
                is UseCaseResult.Failure -> _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMsg = result.message
                    )
                }
            }
        }
    }

    fun onFire(id: Long) {
        viewModelScope.launch {
            employeeRepository.delete(id)
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMsg = null, successMsg = null) }
    }

    fun clearSuccessMessage() { _uiState.update { it.copy(successMsg = null) } }
    fun clearErrorMessage() { _uiState.update { it.copy(errorMsg = null) } }
}
