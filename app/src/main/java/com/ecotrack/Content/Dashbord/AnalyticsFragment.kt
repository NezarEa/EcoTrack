package com.ecotrack.Content.Dashbord

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ecotrack.R
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

class AnalyticsFragment : Fragment() {

    // Data model for consumption
    private data class ConsumptionData(
        val month: String,
        val totalConsumption: Double,
        val cost: Double,
        val applianceBreakdown: Map<String, Double>
    )

    // For the RecyclerView
    private data class MonthlyUsageItem(
        val month: String,
        val consumption: Double,
        val cost: Double,
        val percentChange: Double
    )

    private val monthlyData = mutableListOf<ConsumptionData>()
    private val displayData = mutableListOf<MonthlyUsageItem>()
    private lateinit var adapter: MonthlyUsageAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_analytics, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Load data
        loadEnergyConsumptionData()

        // Setup UI components
        setupCardValues(view)
        setupPieChart(view)
        setupRecyclerView(view)
    }

    private fun loadEnergyConsumptionData() {
        // In a real app, this would come from an API or local database
        // For demo purposes, we'll use hardcoded JSON data
        try {
            val jsonData = getMockData()
            val dataObject = JSONObject(jsonData)

            // Extract monthly data
            val monthlyConsumption = dataObject.getJSONArray("energy_consumption_monthly")
            for (i in 0 until monthlyConsumption.length()) {
                val monthData = monthlyConsumption.getJSONObject(i)
                val month = monthData.getString("month")
                val totalConsumption = monthData.getDouble("total_consumption_kwh")
                val cost = monthData.getDouble("cost_mad")

                // Extract appliance breakdown
                val breakdownObject = monthData.getJSONObject("appliance_breakdown")
                val applianceBreakdown = mutableMapOf<String, Double>()

                val keys = breakdownObject.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    applianceBreakdown[key] = breakdownObject.getDouble(key)
                }

                monthlyData.add(ConsumptionData(month, totalConsumption, cost, applianceBreakdown))
            }

            // Process data for display
            processDisplayData()

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun processDisplayData() {
        // Clear existing data
        displayData.clear()

        // Sort data by date
        val dateFormat = SimpleDateFormat("MMMM yyyy", Locale.US)
        monthlyData.sortBy { dateFormat.parse(it.month) }

        // Generate display data with percentage changes
        for (i in monthlyData.indices) {
            val currentData = monthlyData[i]
            val percentChange = if (i > 0) {
                val previousConsumption = monthlyData[i-1].totalConsumption
                ((currentData.totalConsumption - previousConsumption) / previousConsumption * 100)
            } else {
                0.0
            }

            displayData.add(
                MonthlyUsageItem(
                    currentData.month,
                    currentData.totalConsumption,
                    currentData.cost,
                    percentChange
                )
            )
        }
    }

    private fun setupCardValues(view: View) {
        // Current month data is the last item in our sorted list
        val currentMonthData = if (monthlyData.isNotEmpty()) monthlyData.last() else null
        val previousMonthData = if (monthlyData.size > 1) monthlyData[monthlyData.size - 2] else null

        // Set live kW (we'll just use a calculated value based on daily usage)
        val tvLiveKwValue = view.findViewById<TextView>(R.id.tvLiveKwValue)
        val currentDailyUsage = currentMonthData?.totalConsumption?.div(31) ?: 0.0 // August has 31 days
        val hourlyUsage = currentDailyUsage / 24
        tvLiveKwValue.text = String.format("%.1f", hourlyUsage)

        // Set kWh/day
        val tvKwhDayValue = view.findViewById<TextView>(R.id.tvKwhDayValue)
        tvKwhDayValue.text = String.format("%.1f", currentDailyUsage)

        // Set month to date text
        val tvKwhDayMonthToDate = view.findViewById<TextView>(R.id.tvKwhDayMonthToDate)
        val currentDate = Calendar.getInstance()
        currentDate.set(2025, Calendar.AUGUST, 15) // Set to August 15, 2025
        val daysInMonth = currentDate.getActualMaximum(Calendar.DAY_OF_MONTH)
        val currentDay = currentDate.get(Calendar.DAY_OF_MONTH)
        val monthToDateConsumption = (currentMonthData?.totalConsumption ?: 0.0) * (currentDay.toDouble() / daysInMonth.toDouble())
        tvKwhDayMonthToDate.text = "Month to Date (${monthToDateConsumption.roundToInt()} / ${currentMonthData?.totalConsumption?.roundToInt()} kWh)"

        // Set previous month data
        val tvPrevMonthValue = view.findViewById<TextView>(R.id.tvPrevMonthValue)
        tvPrevMonthValue.text = previousMonthData?.totalConsumption?.roundToInt()?.toString() ?: "0"

        // Set usage analysis value (comparing with average)
        val tvUsageAnalysisValue = view.findViewById<TextView>(R.id.tvUsageAnalysisValue)
        val averageMonthlyConsumption = monthlyData.map { it.totalConsumption }.average()
        val usageAnalysisPercent = if (currentMonthData != null && averageMonthlyConsumption > 0) {
            ((currentMonthData.totalConsumption - averageMonthlyConsumption) / averageMonthlyConsumption * 100)
        } else {
            0.0
        }

        tvUsageAnalysisValue.text = String.format("%.1f%%", usageAnalysisPercent.absoluteValue)

        // Set usage analysis comparison text and color
        val tvUsageAnalysisPercentage = view.findViewById<TextView>(R.id.tvUsageAnalysisPercentage)
        if (usageAnalysisPercent < 0) {
            tvUsageAnalysisPercentage.text = "↓ ${usageAnalysisPercent.absoluteValue.roundToInt()}% better"
            tvUsageAnalysisPercentage.setTextColor(Color.parseColor("#16A34A")) // Green
        } else {
            tvUsageAnalysisPercentage.text = "↑ ${usageAnalysisPercent.roundToInt()}% worse"
            tvUsageAnalysisPercentage.setTextColor(Color.parseColor("#EF4444")) // Red
        }
    }

    private fun setupPieChart(view: View) {
        val pieChart = view.findViewById<PieChart>(R.id.pieChartMonthlyUsage)

        // If no data is available, return
        if (monthlyData.isEmpty()) return

        // Get the most recent month's data
        val currentMonthData = monthlyData.last()

        // Create entries for the pie chart
        val entries = mutableListOf<PieEntry>()
        val colors = mutableListOf<Int>()

        // Define colors for appliances
        val applianceColors = mapOf(
            "Air Conditioner" to Color.parseColor("#FF9500"),
            "Refrigerator" to Color.parseColor("#34C759"),
            "Washing Machine" to Color.parseColor("#5AC8FA"),
            "Lighting" to Color.parseColor("#FFCC00"),
            "TV" to Color.parseColor("#FF3B30"),
            "Microwave" to Color.parseColor("#5856D6"),
            "Other" to Color.parseColor("#007AFF")
        )

        // Add entries for each appliance
        for ((appliance, consumption) in currentMonthData.applianceBreakdown) {
            entries.add(PieEntry(consumption.toFloat(), appliance))
            colors.add(applianceColors[appliance] ?: ColorTemplate.COLORFUL_COLORS[0])
        }

        // Create the dataset
        val dataSet = PieDataSet(entries, "Appliances")
        dataSet.colors = colors
        dataSet.valueTextSize = 12f
        dataSet.valueTextColor = Color.WHITE

        // Create the data object
        val data = PieData(dataSet)
        data.setValueFormatter(PercentFormatter(pieChart))

        // Configure the chart
        pieChart.data = data
        pieChart.description.isEnabled = false
        pieChart.centerText = "Energy\nUsage"
        pieChart.setCenterTextSize(12f)
        pieChart.setHoleColor(Color.TRANSPARENT)
        pieChart.legend.isEnabled = true
        pieChart.legend.textSize = 12f
        pieChart.animateY(1000)
        pieChart.invalidate()
    }

    private fun setupRecyclerView(view: View) {
        val recyclerView = view.findViewById<RecyclerView>(R.id.rvMonthlyData)
        adapter = MonthlyUsageAdapter(displayData)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter
    }

    // Inner class for RecyclerView Adapter
    private inner class MonthlyUsageAdapter(private val items: List<MonthlyUsageItem>) :
        RecyclerView.Adapter<MonthlyUsageAdapter.ViewHolder>() {

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val tvMonth: TextView = itemView.findViewById(R.id.tvMonth)
            val tvConsumption: TextView = itemView.findViewById(R.id.tvConsumption)
            val tvCost: TextView = itemView.findViewById(R.id.tvCost)
            val tvPercentChange: TextView = itemView.findViewById(R.id.tvPercentChange)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_monthly_usage, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = items[position]

            holder.tvMonth.text = item.month
            holder.tvConsumption.text = "${item.consumption.roundToInt()} kWh"
            holder.tvCost.text = "${item.cost.roundToInt()} MAD"

            // Format percent change
            val percentChange = item.percentChange
            val formattedPercent = String.format("%.1f%%", percentChange.absoluteValue)

            if (percentChange < 0) {
                holder.tvPercentChange.text = "↓ $formattedPercent"
                holder.tvPercentChange.setTextColor(Color.parseColor("#16A34A")) // Green
            } else if (percentChange > 0) {
                holder.tvPercentChange.text = "↑ $formattedPercent"
                holder.tvPercentChange.setTextColor(Color.parseColor("#EF4444")) // Red
            } else {
                holder.tvPercentChange.text = "0.0%"
                holder.tvPercentChange.setTextColor(Color.parseColor("#888888")) // Gray
            }
        }

        override fun getItemCount() = items.size
    }

    // Mock data function - in a real app this would be fetched from a server or local database
    private fun getMockData(): String {
        return """
        {
          "user": {
            "id": "12345",
            "name": "John Doe",
            "email": "johndoe@example.com",
            "preferences": {
              "alert_threshold": 200,
              "time_range": "monthly"
            }
          },
          "energy_consumption_monthly": [
            {
              "month": "May 2024",
              "total_consumption_kwh": 420,
              "cost_mad": 504.00,
              "appliance_breakdown": {
                "Air Conditioner": 126,
                "Refrigerator": 84,
                "Washing Machine": 63,
                "Lighting": 50.4,
                "TV": 33.6,
                "Microwave": 25.2,
                "Other": 37.8
              }
            },
            {
              "month": "June 2024",
              "total_consumption_kwh": 480,
              "cost_mad": 576.00,
              "appliance_breakdown": {
                "Air Conditioner": 168,
                "Refrigerator": 91.2,
                "Washing Machine": 67.2,
                "Lighting": 48,
                "TV": 33.6,
                "Microwave": 28.8,
                "Other": 43.2
              }
            },
            {
              "month": "July 2024",
              "total_consumption_kwh": 540,
              "cost_mad": 648.00,
              "appliance_breakdown": {
                "Air Conditioner": 216,
                "Refrigerator": 97.2,
                "Washing Machine": 70.2,
                "Lighting": 43.2,
                "TV": 37.8,
                "Microwave": 32.4,
                "Other": 43.2
              }
            },
            {
              "month": "August 2024",
              "total_consumption_kwh": 580,
              "cost_mad": 696.00,
              "appliance_breakdown": {
                "Air Conditioner": 243.6,
                "Refrigerator": 104.4,
                "Washing Machine": 69.6,
                "Lighting": 46.4,
                "TV": 40.6,
                "Microwave": 34.8,
                "Other": 40.6
              }
            },
            {
              "month": "September 2024",
              "total_consumption_kwh": 490,
              "cost_mad": 588.00,
              "appliance_breakdown": {
                "Air Conditioner": 156.8,
                "Refrigerator": 98,
                "Washing Machine": 73.5,
                "Lighting": 49,
                "TV": 39.2,
                "Microwave": 29.4,
                "Other": 44.1
              }
            },
            {
              "month": "October 2024",
              "total_consumption_kwh": 430,
              "cost_mad": 516.00,
              "appliance_breakdown": {
                "Air Conditioner": 107.5,
                "Refrigerator": 94.6,
                "Washing Machine": 68.8,
                "Lighting": 60.2,
                "TV": 34.4,
                "Microwave": 25.8,
                "Other": 38.7
              }
            },
            {
              "month": "November 2024",
              "total_consumption_kwh": 450,
              "cost_mad": 540.00,
              "appliance_breakdown": {
                "Air Conditioner": 90,
                "Refrigerator": 99,
                "Washing Machine": 76.5,
                "Lighting": 72,
                "TV": 45,
                "Microwave": 27,
                "Other": 40.5
              }
            },
            {
              "month": "December 2024",
              "total_consumption_kwh": 510,
              "cost_mad": 612.00,
              "appliance_breakdown": {
                "Air Conditioner": 91.8,
                "Refrigerator": 112.2,
                "Washing Machine": 91.8,
                "Lighting": 86.7,
                "TV": 51,
                "Microwave": 30.6,
                "Other": 45.9
              }
            },
            {
              "month": "January 2025",
              "total_consumption_kwh": 530,
              "cost_mad": 636.00,
              "appliance_breakdown": {
                "Air Conditioner": 79.5,
                "Refrigerator": 127.2,
                "Washing Machine": 95.4,
                "Lighting": 95.4,
                "TV": 53,
                "Microwave": 31.8,
                "Other": 47.7
              }
            },
            {
              "month": "February 2025",
              "total_consumption_kwh": 490,
              "cost_mad": 588.00,
              "appliance_breakdown": {
                "Air Conditioner": 88.2,
                "Refrigerator": 112.7,
                "Washing Machine": 83.3,
                "Lighting": 83.3,
                "TV": 49,
                "Microwave": 29.4,
                "Other": 44.1
              }
            },
            {
              "month": "March 2025",
              "total_consumption_kwh": 460,
              "cost_mad": 552.00,
              "appliance_breakdown": {
                "Air Conditioner": 101.2,
                "Refrigerator": 101.2,
                "Washing Machine": 73.6,
                "Lighting": 69,
                "TV": 46,
                "Microwave": 27.6,
                "Other": 41.4
              }
            },
            {
              "month": "April 2025",
              "total_consumption_kwh": 430,
              "cost_mad": 516.00,
              "appliance_breakdown": {
                "Air Conditioner": 107.5,
                "Refrigerator": 90.3,
                "Washing Machine": 64.5,
                "Lighting": 60.2,
                "TV": 38.7,
                "Microwave": 25.8,
                "Other": 43
              }
            },
            {
              "month": "May 2025",
              "total_consumption_kwh": 410,
              "cost_mad": 492.00,
              "appliance_breakdown": {
                "Air Conditioner": 114.8,
                "Refrigerator": 82,
                "Washing Machine": 61.5,
                "Lighting": 49.2,
                "TV": 36.9,
                "Microwave": 24.6,
                "Other": 41
              }
            },
            {
              "month": "June 2025",
              "total_consumption_kwh": 465,
              "cost_mad": 558.00,
              "appliance_breakdown": {
                "Air Conditioner": 162.8,
                "Refrigerator": 83.7,
                "Washing Machine": 65.1,
                "Lighting": 46.5,
                "TV": 37.2,
                "Microwave": 27.9,
                "Other": 41.8
              }
            },
            {
              "month": "July 2025",
              "total_consumption_kwh": 520,
              "cost_mad": 624.00,
              "appliance_breakdown": {
                "Air Conditioner": 208.0,
                "Refrigerator": 88.4,
                "Washing Machine": 67.6,
                "Lighting": 41.6,
                "TV": 41.6,
                "Microwave": 31.2,
                "Other": 41.6
              }
            },
            {
              "month": "August 2025",
              "total_consumption_kwh": 565,
              "cost_mad": 678.00,
              "appliance_breakdown": {
                "Air Conditioner": 242.9,
                "Refrigerator": 90.4,
                "Washing Machine": 73.5,
                "Lighting": 39.5,
                "TV": 45.2,
                "Microwave": 33.9,
                "Other": 39.6
              }
            }
          ],
          "consumption_summary": {
            "total_annual_consumption_kwh": 6250,
            "total_annual_cost_mad": 7500.00,
            "average_monthly_consumption_kwh": 495.8,
            "highest_consumption_month": "August 2025",
            "lowest_consumption_month": "May 2025",
            "appliance_totals": {
              "Air Conditioner": 1935.5,
              "Refrigerator": 1284.5,
              "Washing Machine": 955.9,
              "Lighting": 713.5,
              "TV": 535.9,
              "Microwave": 363.0,
              "Other": 502.4
            }
          },
          "energy_saving_recommendations": [
            {
              "id": 1,
              "appliance": "Air Conditioner",
              "recommendation": "Install smart thermostats to optimize cooling based on occupancy patterns.",
              "potential_annual_savings_kwh": 290.3,
              "potential_annual_savings_mad": 348.40
            },
            {
              "id": 2,
              "appliance": "Lighting",
              "recommendation": "Install motion sensors in less frequently used areas to automatically control lights.",
              "potential_annual_savings_kwh": 142.7,
              "potential_annual_savings_mad": 171.20
            },
            {
              "id": 3,
              "appliance": "Refrigerator",
              "recommendation": "Consider upgrading to an Energy Star certified model if your unit is over 10 years old.",
              "potential_annual_savings_kwh": 256.9,
              "potential_annual_savings_mad": 308.30
            },
            {
              "id": 4,
              "appliance": "General",
              "recommendation": "Participate in our new Energy Management Program to receive personalized efficiency tips.",
              "potential_annual_savings_kwh": 312.5,
              "potential_annual_savings_mad": 375.00
            }
          ],
          "energy_costs": {
            "kwh_rate": 1.20,
            "currency": "MAD"
          }
        }
        """.trimIndent()
    }
}