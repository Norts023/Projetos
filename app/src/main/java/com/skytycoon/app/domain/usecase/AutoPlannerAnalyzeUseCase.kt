package com.skytycoon.app.domain.usecase

import com.skytycoon.app.data.repository.ContractRepository
import com.skytycoon.app.data.repository.FlightRepository
import com.skytycoon.app.data.repository.GameStateRepository
import com.skytycoon.app.data.repository.OwnedAircraftRepository
import com.skytycoon.app.domain.model.Recommendation
import com.skytycoon.app.domain.model.RecommendationType
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class AutoPlannerAnalyzeUseCase @Inject constructor(
    private val gameStateRepository: GameStateRepository,
    private val contractRepository: ContractRepository,
    private val ownedAircraftRepository: OwnedAircraftRepository,
    private val flightRepository: FlightRepository
) {
    suspend operator fun invoke(): List<Recommendation> {
        val gameState = gameStateRepository.get().first() ?: return emptyList()
        val recommendations = mutableListOf<Recommendation>()

        val aircraft = ownedAircraftRepository.getAll().first()
        val contracts = contractRepository.getAvailable().first()
        val activeFlights = flightRepository.getAll().first().filter { it.isActive }

        val idleAircraft = aircraft.filter { a -> a.isOperational && activeFlights.none { it.aircraftId == a.id } }
        if (idleAircraft.isNotEmpty() && contracts.isNotEmpty()) {
            recommendations.add(Recommendation(
                id = "idle_aircraft",
                type = RecommendationType.SCHEDULE_FLIGHT,
                title = "Aeronaves ociosas detectadas",
                description = "${idleAircraft.size} aeronave(s) disponível(is). ${contracts.size} contrato(s) disponível(is).",
                estimatedGainCoins = contracts.take(idleAircraft.size).sumOf { it.totalValueCoins },
                actionData = mapOf("aircraftCount" to idleAircraft.size.toString())
            ))
        }

        aircraft.filter { it.needsMaintenance }.forEach { a ->
            recommendations.add(Recommendation(
                id = "maintenance_${a.id}",
                type = RecommendationType.MAINTENANCE,
                title = "Manutenção recomendada: ${a.registrationCode}",
                description = "Aeronave ${a.registrationCode} precisa de manutenção (${a.conditionLabel}).",
                estimatedGainCoins = 0
            ))
        }

        if (contracts.isEmpty()) {
            recommendations.add(Recommendation(
                id = "gen_contracts",
                type = RecommendationType.GENERATE_CONTRACTS,
                title = "Gerar novos contratos",
                description = "Não há contratos disponíveis. Gere novos para manter o fluxo de receita.",
                estimatedGainCoins = 0
            ))
        }

        if (gameState.reputation < 50 && aircraft.isEmpty()) {
            recommendations.add(Recommendation(
                id = "hire_pilot",
                type = RecommendationType.HIRE_EMPLOYEE,
                title = "Contrate um piloto",
                description = "Você precisa de pilotos para operar voos.",
                estimatedGainCoins = 0
            ))
        }

        return recommendations
    }
}
