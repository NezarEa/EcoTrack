package com.ecotrack.Data

data class EnergySavingRecommendation(
    val id: Int,
    val appliance: String,
    val recommendation: String,
    val potential_annual_savings_kwh: Double,
    val potential_annual_savings_mad: Double
)