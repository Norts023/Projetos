package com.skytycoon.app.domain.usecase

import com.skytycoon.app.config.GameBalanceConfig
import com.skytycoon.app.data.repository.EmployeeRepository
import com.skytycoon.app.data.repository.GameStateRepository
import com.skytycoon.app.domain.model.Employee
import com.skytycoon.app.domain.model.EmployeeType
import com.skytycoon.app.domain.model.UseCaseResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject

class HireEmployeeUseCase @Inject constructor(
    private val employeeRepository: EmployeeRepository,
    private val gameStateRepository: GameStateRepository
) {
    suspend operator fun invoke(
        name: String,
        type: EmployeeType,
        level: Int
    ): UseCaseResult<Employee> = withContext(Dispatchers.IO) {
        if (level !in 1..5) {
            return@withContext UseCaseResult.Failure("Employee level must be between 1 and 5")
        }

        val baseSalary = when (type) {
            EmployeeType.PILOT -> GameBalanceConfig.EmployeeDailySalary.PILOT
            EmployeeType.HELICOPTER_PILOT -> GameBalanceConfig.EmployeeDailySalary.HELICOPTER_PILOT
            EmployeeType.COPILOT -> GameBalanceConfig.EmployeeDailySalary.COPILOT
            EmployeeType.FLIGHT_ATTENDANT -> GameBalanceConfig.EmployeeDailySalary.FLIGHT_ATTENDANT
            EmployeeType.MECHANIC -> GameBalanceConfig.EmployeeDailySalary.MECHANIC
            EmployeeType.ADMIN -> GameBalanceConfig.EmployeeDailySalary.ADMIN
        }

        val levelFactor = when (level) {
            1 -> 1.0
            2 -> 1.2
            3 -> 1.5
            4 -> 1.8
            5 -> 2.2
            else -> 1.0
        }

        val dailySalary = (baseSalary * levelFactor).toLong()
        val hiringFee = dailySalary * 7L // one week upfront

        val state = gameStateRepository.get().first()
            ?: return@withContext UseCaseResult.Failure("No active game state found")

        if (state.balanceCoins < hiringFee) {
            return@withContext UseCaseResult.Failure(
                "Insufficient funds for hiring fee. Required: $hiringFee, Available: ${state.balanceCoins}"
            )
        }

        val employee = Employee(
            id = 0L,
            name = name,
            type = type,
            level = level,
            dailySalaryCoins = dailySalary,
            fatigue = 0,
            currentFlightId = null
        )

        val newId = employeeRepository.insert(employee)
        gameStateRepository.update(state.copy(balanceCoins = state.balanceCoins - hiringFee))

        UseCaseResult.Success(employee.copy(id = newId))
    }
}
