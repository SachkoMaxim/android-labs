package com.sachkomaxim.lab5.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.graphics.toColorInt
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.sachkomaxim.lab5.R
import com.sachkomaxim.lab5.database.AppDatabase
import com.sachkomaxim.lab5.utils.AnalyticsUtils
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import android.util.Log

class DailyAnalyticsFragment : Fragment() {
    private lateinit var tvStepsCount: TextView
    private lateinit var tvDistance: TextView
    private lateinit var tvCalories: TextView
    private lateinit var barChart: BarChart
    private lateinit var lineChart: LineChart

    private lateinit var analyticsUtils: AnalyticsUtils
    private lateinit var database: AppDatabase

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_daily_analytics, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvStepsCount = view.findViewById(R.id.tv_steps_count)
        tvDistance = view.findViewById(R.id.tv_distance)
        tvCalories = view.findViewById(R.id.tv_calories)
        barChart = view.findViewById(R.id.bar_chart)
        lineChart = view.findViewById(R.id.line_chart)

        analyticsUtils = AnalyticsUtils(requireContext())
        database = AppDatabase.getDatabase(requireContext())

        loadDailyData()
        setupBarChart()
        setupLineChart()
    }

    private fun loadDailyData() {
        lifecycleScope.launch {
            val startOfDay = analyticsUtils.getStartOfDayTimestamp()
            val endOfDay = analyticsUtils.getEndOfDayTimestamp()

            val totalSteps = database.stepDataDao().getTotalStepsInRange(startOfDay, endOfDay) ?: 0
            val distance = analyticsUtils.calculateDistance(totalSteps)
            val calories = analyticsUtils.calculateCalories(totalSteps)

            Log.d("AnalyticsFragment", "Total steps today: $totalSteps")

            tvStepsCount.text = getString(R.string.steps_count, totalSteps)
            tvDistance.text = getString(R.string.distance, distance)
            tvCalories.text = getString(R.string.calories, calories)

            val hourlyData = getHourlyStepData()
            updateBarChart(hourlyData)

            val weeklyData = getWeeklyStepData()
            updateLineChart(weeklyData)
        }
    }

    private suspend fun getHourlyStepData(): List<Pair<Int, Int>> {
        val startOfDay = analyticsUtils.getStartOfDayTimestamp()
        val endOfDay = analyticsUtils.getEndOfDayTimestamp()

        val stepData = database.stepDataDao().getStepDataInRange(startOfDay, endOfDay)
        Log.d("AnalyticsFragment", "Step data entries for today: ${stepData.size}")

        val hourlySteps = mutableMapOf<Int, Int>()

        for (data in stepData) {
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = data.timestamp
            val hour = calendar.get(Calendar.HOUR_OF_DAY)

            hourlySteps[hour] = (hourlySteps[hour] ?: 0) + data.steps
            Log.d("AnalyticsFragment", "Hour: $hour, Steps: ${data.steps}, Total for hour: ${hourlySteps[hour]}")
        }

        return (0..23).map { hour ->
            Pair(hour, hourlySteps[hour] ?: 0)
        }
    }

    private suspend fun getWeeklyStepData(): List<Pair<String, Int>> {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("EEE", Locale.getDefault())
        val result = mutableListOf<Pair<String, Int>>()

        // Get data for the last 7 days
        for (i in 6 downTo 0) {
            val currentCalendar = Calendar.getInstance()
            currentCalendar.add(Calendar.DAY_OF_YEAR, -i)

            val dayStart = currentCalendar.apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis

            val dayEnd = currentCalendar.apply {
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 999)
            }.timeInMillis

            val steps = database.stepDataDao().getTotalStepsInRange(dayStart, dayEnd) ?: 0
            val dayName = dateFormat.format(Date(currentCalendar.timeInMillis))

            Log.d("AnalyticsFragment", "Day: $dayName, Steps: $steps")
            result.add(Pair(dayName, steps))
        }

        return result
    }

    private fun setupBarChart() {
        barChart.description.isEnabled = false
        barChart.setDrawGridBackground(false)
        barChart.setDrawBarShadow(false)
        barChart.setDrawValueAboveBar(true)
        barChart.setPinchZoom(false)
        barChart.setScaleEnabled(false)

        val xAxis = barChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.granularity = 1f
        xAxis.labelCount = 24

        val leftAxis = barChart.axisLeft
        leftAxis.setDrawGridLines(true)
        leftAxis.axisMinimum = 0f

        barChart.axisRight.isEnabled = false
        barChart.legend.isEnabled = true
    }

    private fun updateBarChart(hourlyData: List<Pair<Int, Int>>) {
        val entries = hourlyData.mapIndexed { index, (hour, steps) ->
            BarEntry(hour.toFloat(), steps.toFloat())
        }

        val dataSet = BarDataSet(entries, "Steps per Hour")
        dataSet.color = "#4CAF50".toColorInt()

        val data = BarData(dataSet)
        data.setValueTextSize(10f)
        data.barWidth = 0.9f

        barChart.data = data

        barChart.xAxis.valueFormatter = IndexAxisValueFormatter(
            hourlyData.map { (hour, _) -> hour.toString() }
        )

        barChart.invalidate()
    }

    private fun setupLineChart() {
        lineChart.description.isEnabled = false
        lineChart.setDrawGridBackground(false)
        lineChart.setTouchEnabled(true)
        lineChart.setPinchZoom(true)

        val xAxis = lineChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.granularity = 1f

        val leftAxis = lineChart.axisLeft
        leftAxis.setDrawGridLines(true)
        leftAxis.axisMinimum = 0f

        lineChart.axisRight.isEnabled = false
        lineChart.legend.isEnabled = true
    }

    private fun updateLineChart(weeklyData: List<Pair<String, Int>>) {
        val entries = weeklyData.mapIndexed { index, (_, steps) ->
            Entry(index.toFloat(), steps.toFloat())
        }

        val dataSet = LineDataSet(entries, "Weekly Steps")
        dataSet.color = "#2196F3".toColorInt()
        dataSet.setCircleColor("#2196F3".toColorInt())
        dataSet.lineWidth = 2f
        dataSet.circleRadius = 4f
        dataSet.setDrawCircleHole(false)
        dataSet.valueTextSize = 10f
        dataSet.setDrawFilled(true)
        dataSet.fillColor = "#4D2196F3".toColorInt()

        val lineData = LineData(dataSet)
        lineChart.data = lineData

        lineChart.xAxis.valueFormatter = IndexAxisValueFormatter(
            weeklyData.map { (day, _) -> day }
        )

        lineChart.invalidate()
    }
}
