package com.ecotrack.Data

data class EnergyConsumptionResponse(
    val user: User,
    val energy_consumption_monthly: List<EnergyConsumption>,
    val consumption_summary: ConsumptionSummary,
    val energy_saving_recommendations: List<EnergySavingRecommendation>,
    val energy_costs: EnergyCosts
)
