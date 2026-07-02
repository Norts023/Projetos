package com.skytycoon.app.ui.screens.employees

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skytycoon.app.data.repository.EmployeeRepository
import com.skytycoon.app.domain.model.Employee
import com.skytycoon.app.domain.model.EmployeeType
import com.skytycoon.app.domain.model.UseCaseResult
import com.skytycoon.app.domain.usecase.HireEmployeeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EmployeesUiState(
    val employees: List<Employee> = emptyList(),
    val isLoading: Boolean = false,
    val showHireDialog: Boolean = false,
    val hireError: String? = null
)

@HiltViewModel
class EmployeesViewModel @Inject constructor(
    private val employeeRepository: EmployeeRepository,
    private val hireEmployeeUseCase: HireEmployeeUseCase
) : ViewModel() {

    private data class ExtraState(
        val isLoading: Boolean = false,
        val showHireDialog: Boolean = false,
        val hireError: String? = null
    )

    private val _extraState = MutableStateFlow(ExtraState())

    val uiState: StateFlow<EmployeesUiState> = combine(
        employeeRepository.getAll(),
        _extraState
    ) { employees, extra ->
        EmployeesUiState(
            employees = employees,
            isLoading = extra.isLoading,
            showHireDialog = extra.showHireDialog,
            hireError = extra.hireError
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = EmployeesUiState(isLoading = true)
    )

    fun onHire(name: String, type: EmployeeType, level: Int) {
        viewModelScope.launch {
            _extraState.update { it.copy(isLoading = true, hireError = null) }
            when (val result = hireEmployeeUseCase.invoke(name, type, level)) {
                is UseCaseResult.Success -> {
                    _extraState.update { it.copy(isLoading = false, showHireDialog = false) }
                }
                is UseCaseResult.Failure -> {
                    _extraState.update { it.copy(isLoading = false, hireError = result.message) }
                }
            }
        }
    }

    fun onFire(id: Long) {
        viewModelScope.launch {
            employeeRepository.delete(id)
        }
    }

    fun onShowHireDialog(show: Boolean) {
        _extraState.update { it.copy(showHireDialog = show, hireError = null) }
    }

    fun onDismissError() {
        _extraState.update { it.copy(hireError = null) }
    }
}
