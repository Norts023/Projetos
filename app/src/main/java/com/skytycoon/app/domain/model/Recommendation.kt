package com.skytycoon.app.domain.model

enum class RecommendationType { SCHEDULE_FLIGHT, HIRE_EMPLOYEE, MAINTENANCE, GENERATE_CONTRACTS, ACCEPT_CONTRACT }

data class Recommendation(
    val id: String,
    val type: RecommendationType,
    val title: String,
    val description: String,
    val estimatedGainCoins: Long = 0,
    val actionData: Map<String, String> = emptyMap()
)
