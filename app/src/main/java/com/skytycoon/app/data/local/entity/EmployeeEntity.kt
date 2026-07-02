package com.skytycoon.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.skytycoon.app.domain.model.Employee
import com.skytycoon.app.domain.model.EmployeeType

@Entity(tableName = "employees")
data class EmployeeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: String,
    val level: Int,
    val dailySalaryCoins: Long,
    val fatigue: Int,
    val currentFlightId: Long?
) {
    fun toDomain(): Employee = Employee(
        id = id,
        name = name,
        type = EmployeeType.entries.firstOrNull { it.name == type } ?: EmployeeType.PILOT,
        level = level,
        dailySalaryCoins = dailySalaryCoins,
        fatigue = fatigue,
        currentFlightId = currentFlightId
    )

    companion object {
        fun fromDomain(employee: Employee): EmployeeEntity = EmployeeEntity(
            id = employee.id,
            name = employee.name,
            type = employee.type.name,
            level = employee.level,
            dailySalaryCoins = employee.dailySalaryCoins,
            fatigue = employee.fatigue,
            currentFlightId = employee.currentFlightId
        )
    }
}
