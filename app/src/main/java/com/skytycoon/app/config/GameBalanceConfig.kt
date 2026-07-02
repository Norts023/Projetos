package com.skytycoon.app.config

import com.skytycoon.app.domain.model.GameMode

/**
 * Central balancing file. Real-world costs divided by ECONOMY_SCALE_FACTOR (100x) to keep
 * progression fun: player starts with ~5M coins and can buy charter aircraft immediately,
 * regional jets mid-game, wide-bodies late-game.
 */
object GameBalanceConfig {

    const val ECONOMY_SCALE_FACTOR = 100.0

    // Starting balance (coins)
    const val STARTING_BALANCE_REALISTIC = 5_000_000L
    const val STARTING_BALANCE_FICTIONAL = 12_000_000L

    // Starting reputation
    const val STARTING_REPUTATION = 30

    // Game time: 1 real minute = 60 game minutes (1 game hour) when manually advancing
    // Each "advance" press adds ADVANCE_STEP_MINUTES game minutes
    const val ADVANCE_STEP_MINUTES = 60L      // 1 game hour per press
    const val FAST_ADVANCE_STEP_MINUTES = 480L // 8 game hours per fast press

    // Fuel price (coins per liter) — scaled down from ~$0.80/L real price
    fun fuelPricePerLiter(mode: GameMode) = when (mode) {
        GameMode.REALISTIC -> 0.008
        GameMode.FICTIONAL -> 0.003
    }

    // Demand multipliers (passenger fill rate modifier)
    fun demandMultiplier(mode: GameMode) = when (mode) {
        GameMode.REALISTIC -> 1.0
        GameMode.FICTIONAL -> 1.4
    }

    // Airport landing tax (coins per landing)
    object AirportTax {
        const val SMALL = 200L
        const val MEDIUM = 500L
        const val LARGE = 1_000L
        const val HUB = 2_000L
    }

    // Hourly operating cost base by category (crew + overhead, NOT fuel)
    object HourlyCostBase {
        const val AIRLINER = 800L
        const val CHARTER = 200L
        const val HELICOPTER = 300L
    }

    // Employee daily salary (coins)
    object EmployeeDailySalary {
        const val PILOT = 600L
        const val HELICOPTER_PILOT = 700L
        const val COPILOT = 350L
        const val FLIGHT_ATTENDANT = 180L
        const val MECHANIC = 300L
        const val ADMIN = 250L
    }

    // Fatigue — each flight hour adds this fatigue; rest removes 20/game-hour
    const val FATIGUE_PER_FLIGHT_HOUR = 8
    const val FATIGUE_RECOVERY_PER_GAME_HOUR = 20

    // Maintenance
    const val MAINTENANCE_INTERVAL_HOURS = 100.0
    const val CONDITION_LOSS_PER_FLIGHT_HOUR = 0.8
    const val MECHANIC_SPEED_BONUS_PER_LEVEL = 0.15 // 15% faster per mechanic level
    fun maintenanceCostMultiplier(mode: GameMode) = when (mode) {
        GameMode.REALISTIC -> 1.0
        GameMode.FICTIONAL -> 0.6
    }

    // Reputation changes
    const val REPUTATION_ON_TIME_BONUS = 1
    const val REPUTATION_DELAY_PENALTY = 2
    const val REPUTATION_CANCELLATION_PENALTY = 5
    const val REPUTATION_CONTRACT_COMPLETED_BONUS = 2
    const val REPUTATION_MAX = 100
    const val REPUTATION_MIN = 0

    // Contract generation
    const val CONTRACT_MIN_VALUE_CHARTER = 5_000L
    const val CONTRACT_MAX_VALUE_CHARTER = 50_000L
    const val CONTRACT_MIN_VALUE_HELICOPTER = 2_000L
    const val CONTRACT_MAX_VALUE_HELICOPTER = 20_000L
    const val CONTRACT_BONUS_RATE = 0.20 // 20% of base value as on-time bonus
    const val CONTRACT_EXPIRY_GAME_HOURS = 48L
    const val MAX_AVAILABLE_CONTRACTS = 12

    // Research
    const val RESEARCH_POINTS_PER_MISSION_REWARD = 5
    const val RESEARCH_POINTS_PER_GAME_DAY = 2

    // Demand base fill rate per reputation tier (% of seats filled)
    fun baseFillRate(reputation: Int, mode: GameMode): Double {
        val base = when {
            reputation >= 90 -> 0.92
            reputation >= 75 -> 0.82
            reputation >= 60 -> 0.70
            reputation >= 40 -> 0.55
            reputation >= 20 -> 0.38
            else -> 0.20
        }
        return base * demandMultiplier(mode)
    }

    // Ticket price range per km (coins per passenger per km)
    const val TICKET_PRICE_PER_KM_MIN = 0.05
    const val TICKET_PRICE_PER_KM_MAX = 0.15

    // Realistic mode crew requirements
    object CrewRequirements {
        const val AIRLINER_PILOTS = 2
        const val AIRLINER_ATTENDANTS_PER_50_PAX = 1
        const val CHARTER_PILOTS = 1
        const val HELICOPTER_PILOTS = 1
    }
}
