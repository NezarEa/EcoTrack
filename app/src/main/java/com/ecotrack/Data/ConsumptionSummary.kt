package com.ecotrack.Data

data class ConsumptionSummary(
    val total_annual_consumption_kwh: Double,
    val total_annual_cost_mad: Double,
    val average_monthly_consumption_kwh: Double,
    val highest_consumption_month: String,
    val lowest_consumption_month: String,
    val appliance_totals: ApplianceTotals
)
