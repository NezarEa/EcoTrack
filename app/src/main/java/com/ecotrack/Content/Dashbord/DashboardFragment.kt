package com.ecotrack.Content.Dashbord

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.ecotrack.Data.ApplianceBreakdown
import com.ecotrack.Data.ApplianceTotals
import com.ecotrack.Data.ConsumptionSummary
import com.ecotrack.Data.EnergyConsumption
import com.ecotrack.Data.EnergyCosts
import com.ecotrack.Data.EnergySavingRecommendation
import com.ecotrack.R

class DashboardFragment : Fragment() {

    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var tvCurrentConsumption: TextView
    private lateinit var tvTotalCost: TextView
    private lateinit var tvHighestConsumptionMonth: TextView
    private lateinit var tvSavingRecommendations: TextView

    // Hardcoded list of energy consumption data
    private val energyConsumptionList = listOf(
        EnergyConsumption(
            month = "May 2024",
            total_consumption_kwh = 420.0,
            cost_mad = 504.00,
            appliance_breakdown = ApplianceBreakdown(
                AirConditioner = 126.0,
                Refrigerator = 84.0,
                WashingMachine = 63.0,
                Lighting = 50.4,
                TV = 33.6,
                Microwave = 25.2,
                Other = 37.8
            )
        ),
        EnergyConsumption(
            month = "June 2024",
            total_consumption_kwh = 480.0,
            cost_mad = 576.00,
            appliance_breakdown = ApplianceBreakdown(
                AirConditioner = 168.0,
                Refrigerator = 91.2,
                WashingMachine = 67.2,
                Lighting = 48.0,
                TV = 33.6,
                Microwave = 28.8,
                Other = 43.2
            )
        )
    )

    // Hardcoded energy saving recommendations
    private val energySavingRecommendations = listOf(
        EnergySavingRecommendation(
            id = 1,
            appliance = "Air Conditioner",
            recommendation = "Increase thermostat by 2-3°C to save 10% on cooling costs.",
            potential_annual_savings_kwh = 169.1,
            potential_annual_savings_mad = 202.90
        ),
        EnergySavingRecommendation(
            id = 2,
            appliance = "Lighting",
            recommendation = "Switch to LEDs to save up to 80% on lighting energy.",
            potential_annual_savings_kwh = 611.5,
            potential_annual_savings_mad = 733.80
        )
    )


    // Consumption summary data
    private val consumptionSummary = ConsumptionSummary(
        total_annual_consumption_kwh = 5790.0,
        total_annual_cost_mad = 6948.00,
        average_monthly_consumption_kwh = 482.5,
        highest_consumption_month = "August 2024",
        lowest_consumption_month = "May 2025",
        appliance_totals = ApplianceTotals(
            AirConditioner = 1690.7,
            Refrigerator = 1294.0,
            WashingMachine = 958.9,
            Lighting = 764.4,
            TV = 507.8,
            Microwave = 343.2,
            Other = 511.0
        )
    )

    private val energyCosts = EnergyCosts(
        kwh_rate = 1.20,
        currency = "MAD"
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_dashbord, container, false)

        // Initialize views
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout)
        tvCurrentConsumption = view.findViewById(R.id.tvCurrentConsumption)
        tvTotalCost = view.findViewById(R.id.tvTotalCost)
        tvHighestConsumptionMonth = view.findViewById(R.id.tvHighestConsumptionMonth)
        tvSavingRecommendations = view.findViewById(R.id.tvSavingRecommendations)

        // Fetch the most recent data (you can replace this with dynamic data from an API or database)
        val latestEnergyConsumption = energyConsumptionList.last()  // Get the last energy consumption entry
        val currentConsumption = latestEnergyConsumption.total_consumption_kwh
        val totalCost = latestEnergyConsumption.cost_mad
        val highestConsumptionMonth = consumptionSummary.highest_consumption_month
        val savingRecommendationsText = energySavingRecommendations.joinToString("\n") {
            "${it.appliance}: ${it.recommendation}"
        }

        // Update the TextViews with the fetched data
        tvCurrentConsumption.text = "Current Consumption: ${currentConsumption} kWh"
        tvTotalCost.text = "Total Cost: ${totalCost} MAD"
        tvHighestConsumptionMonth.text = "Highest Consumption: $highestConsumptionMonth"
        tvSavingRecommendations.text = "Energy Saving Recommendations:\n$savingRecommendationsText"

        // Set up swipe-to-refresh listener (no need to change data in this example)
        swipeRefreshLayout.setOnRefreshListener {
            swipeRefreshLayout.isRefreshing = false // Stop the refreshing animation
        }

        return view
    }
}
