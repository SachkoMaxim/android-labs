package com.sachkomaxim.lab5

import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.View.OnClickListener
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import androidx.core.content.edit
import com.sachkomaxim.lab5.database.AppDatabase
import com.sachkomaxim.lab5.database.StepData
import com.sachkomaxim.lab5.utils.AnalyticsUtils

class MainActivity : AppCompatActivity(), SensorEventListener, OnClickListener {

    private val sensorManager: SensorManager by lazy { getSystemService(SENSOR_SERVICE) as SensorManager }
    private lateinit var progressBar: ProgressBar
    private lateinit var stepsTextView: TextView

    private var isRunning = false
    private var totalStepsCount = 0
    private var initialStepCount = 0
    private var previousTotalStepsCount = 0
    private var isGoalReached = false
    private var lastSavedSteps = 0

    private val database: AppDatabase by lazy { AppDatabase.getDatabase(this) }
    private val analyticsUtils: AnalyticsUtils by lazy { AnalyticsUtils(this) }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize views
        progressBar = findViewById(R.id.progressBar)
        stepsTextView = findViewById(R.id.textView)

        // Set up toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACTIVITY_RECOGNITION)
            != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.ACTIVITY_RECOGNITION),
                123) // some random magic number
        }

        findViewById<View>(R.id.resetButton).setOnClickListener(this)
        findViewById<View>(R.id.clearCacheButton).setOnClickListener(this)

        loadStepsCount()

        progressBar.max = GOAL_STEPS_COUNT

        updateStepsText(0)
        progressBar.progress = 0
    }

    private fun clearStepsCache() {
        val sharedPreferences = getSharedPreferences("stepsPrefs", MODE_PRIVATE)
        sharedPreferences.edit {
            clear()
            apply()
        }

        initialStepCount = 0
        previousTotalStepsCount = 0
        lastSavedSteps = 0
        isGoalReached = false

        updateStepsText(0)
        progressBar.progress = 0

        lifecycleScope.launch {
            try {
                database.stepDataDao().deleteAllStepData()
                Toast.makeText(this@MainActivity, "Steps cache cleared successfully", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Error clearing database: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateStepsText(currentSteps: Int) {
        stepsTextView.text = getString(R.string.total_steps, currentSteps, GOAL_STEPS_COUNT)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_analytics -> {
                val intent = Intent(this, AnalyticsActivity::class.java)
                startActivity(intent)
                true
            }
            R.id.action_settings -> {
                Toast.makeText(this, "Settings clicked", Toast.LENGTH_SHORT).show()
                true
            }
            R.id.action_about -> {
                Toast.makeText(this, "About clicked", Toast.LENGTH_SHORT).show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onResume() {
        super.onResume()
        isRunning = true
        val stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        if (stepCounterSensor == null) {
            Toast.makeText(this, "No sensor detected on this device", Toast.LENGTH_SHORT).show()
        } else {
            sensorManager.registerListener(this, stepCounterSensor, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onPause() {
        super.onPause()
        isRunning = false
        sensorManager.unregisterListener(this)
        saveStepsCount()
    }

    override fun onSensorChanged(sensorEvent: SensorEvent?) {
        if (isRunning && sensorEvent != null) {
            val currentTotalSteps = sensorEvent.values[0].toInt()

            Log.d("StepCounter", "Sensor value: $currentTotalSteps")

            if (initialStepCount == 0) {
                initialStepCount = currentTotalSteps

                if (previousTotalStepsCount == 0) {
                    previousTotalStepsCount = initialStepCount
                }

                if (lastSavedSteps == 0) {
                    lastSavedSteps = initialStepCount
                }

                Log.d("StepCounter", "Initial step count set to: $initialStepCount")
                Log.d("StepCounter", "Previous total steps count: $previousTotalStepsCount")
                Log.d("StepCounter", "Last saved steps: $lastSavedSteps")
            }

            totalStepsCount = currentTotalSteps

            val currentStepsCount = totalStepsCount - previousTotalStepsCount

            Log.d("StepCounter", "Current steps count: $currentStepsCount")

            updateStepsText(currentStepsCount)
            progressBar.progress = currentStepsCount

            if (currentStepsCount >= GOAL_STEPS_COUNT && !isGoalReached) {
                Log.d("StepCounter", "Goal reached!")
                Toast.makeText(this, "Congratulations! You've reached your goal!", Toast.LENGTH_LONG).show()
                isGoalReached = true
            }

            if (totalStepsCount > lastSavedSteps) {
                val newSteps = totalStepsCount - lastSavedSteps
                if (newSteps > 0) {
                    saveStepDataForAnalytics(newSteps)
                    lastSavedSteps = totalStepsCount
                    Log.d("StepCounter", "Updated lastSavedSteps to: $lastSavedSteps")
                }
            }
        }
    }

    private fun saveStepDataForAnalytics(steps: Int) {
        if (steps <= 0) return

        lifecycleScope.launch {
            val stepData = StepData(
                steps = steps,
                timestamp = System.currentTimeMillis()
            )
            database.stepDataDao().insertStepData(stepData)
            Log.d("StepCounter", "Saved $steps steps to database")
        }
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
    }

    override fun onClick(p0: View?) {
        when (p0!!.id) {
            R.id.resetButton -> {
                previousTotalStepsCount = totalStepsCount
                updateStepsText(0)
                progressBar.progress = 0
                isGoalReached = false
                Log.d("StepCounter", "Reset button clicked. Previous total steps set to: $previousTotalStepsCount")
            }
            R.id.clearCacheButton -> {
                androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Clear Steps Cache")
                    .setMessage("Are you sure you want to clear all saved steps data? This action cannot be undone.")
                    .setPositiveButton("Clear") { _, _ ->
                        clearStepsCache()
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        }
    }

    private fun saveStepsCount() {
        val sharedPreferences = getSharedPreferences("stepsPrefs", MODE_PRIVATE)
        sharedPreferences.edit {
            putInt("initialStepCount", initialStepCount)
            putInt("previousTotalStepsCount", previousTotalStepsCount)
            putInt("lastSavedSteps", lastSavedSteps)
            apply()
        }
        Log.d("StepCounter", "Saved. Initial: $initialStepCount, Previous: $previousTotalStepsCount, LastSaved: $lastSavedSteps")
    }

    private fun loadStepsCount() {
        val sharedPreferences = getSharedPreferences("stepsPrefs", MODE_PRIVATE)
        initialStepCount = sharedPreferences.getInt("initialStepCount", 0)
        previousTotalStepsCount = sharedPreferences.getInt("previousTotalStepsCount", 0)
        lastSavedSteps = sharedPreferences.getInt("lastSavedSteps", 0)
        Log.d("StepCounter", "Loaded. Initial: $initialStepCount, Previous: $previousTotalStepsCount, LastSaved: $lastSavedSteps")
    }

    companion object {
        const val GOAL_STEPS_COUNT = 50
    }
}
