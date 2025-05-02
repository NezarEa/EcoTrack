package com.ecotrack.Data

data class EnergyConsumption(
    val month: String,
    val total_consumption_kwh: Double,
    val cost_mad: Double,
    val appliance_breakdown: ApplianceBreakdown
)

