package com.skytycoon.app.domain.usecase

import com.skytycoon.app.domain.model.Recommendation
import com.skytycoon.app.domain.model.RecommendationType
import com.skytycoon.app.domain.model.UseCaseResult
import javax.inject.Inject

class AutoPlannerApplyUseCase @Inject constructor(
    private val generateContractsUseCase: GenerateContractsUseCase
) {
    suspend operator fun invoke(recommendation: Recommendation): UseCaseResult<String> {
        return when (recommendation.type) {
            RecommendationType.GENERATE_CONTRACTS -> {
                generateContractsUseCase()
                UseCaseResult.Success("Contratos gerados com sucesso")
            }
            RecommendationType.SCHEDULE_FLIGHT -> UseCaseResult.Success("Abra a tela de Agendamento para agendar voos")
            RecommendationType.MAINTENANCE -> UseCaseResult.Success("Abra a tela de Frota para fazer manutenção")
            RecommendationType.HIRE_EMPLOYEE -> UseCaseResult.Success("Abra a tela de Funcionários para contratar")
            RecommendationType.ACCEPT_CONTRACT -> UseCaseResult.Success("Aceite contratos na tela de Agendamento")
        }
    }
}
