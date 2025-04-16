package com.sachkomaxim.lab6

import android.app.DatePickerDialog
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.sachkomaxim.lab6.api.RetrofitClient
import com.sachkomaxim.lab6.model.Currency
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDate

class MainActivity : AppCompatActivity() {
    private lateinit var currencyTextView: TextView
    private lateinit var fetchButton: Button
    private var completedCalls = 0
    private val combinedRates = HashMap<String, Double>()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        currencyTextView = findViewById(R.id.currencyRateTextViewMainActivity)
        fetchButton = findViewById(R.id.fetchButton)

        fetchButton.setOnClickListener {
            getDateAndGetCurrencyData()
        }

        fetchCurrencyData("USD", "EUR")
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getDateAndGetCurrencyData() {
        val currentDate = LocalDate.now()
        val dialog = DatePickerDialog(this, { _, year, month, dayOfMonth ->
            val date = "$year${String.format("%02d", month + 1)}${String.format("%02d", dayOfMonth)}"
            Log.d("MainActivity", "Selected date: $date")
            fetchCurrencyData("USD", "EUR", date)
        }, currentDate.year, currentDate.monthValue - 1, currentDate.dayOfMonth)
        dialog.show()
    }

    private fun fetchCurrencyData(currencyCode1: String, currencyCode2: String, date: String? = null) {
        combinedRates.clear()
        completedCalls = 0

        fetchCurrencyData(currencyCode1, object : Callback<List<Currency>> {
            override fun onResponse(call: Call<List<Currency>>, response: Response<List<Currency>>) {
                handleResponse(response, currencyCode1)
            }

            override fun onFailure(call: Call<List<Currency>>, t: Throwable) {
                handleFailure()
            }
        }, date)

        fetchCurrencyData(currencyCode2, object : Callback<List<Currency>> {
            override fun onResponse(call: Call<List<Currency>>, response: Response<List<Currency>>) {
                handleResponse(response, currencyCode2)
            }

            override fun onFailure(call: Call<List<Currency>>, t: Throwable) {
                handleFailure()
            }
        }, date)
    }

    private fun fetchCurrencyData(currencyCode: String, callback: Callback<List<Currency>>, date: String? = null) {
        RetrofitClient.currencyApi.getCurrency(currencyCode, date).enqueue(callback)
    }

    private fun handleResponse(response: Response<List<Currency>>, currencyCode: String) {
        if (response.isSuccessful && response.body()?.isNotEmpty() == true) {
            val currency = response.body()!![0]
            val rate = currency.rate

            combinedRates[currencyCode] = rate.toDouble()
            completedCalls++

            if (completedCalls == 2) { // Both requests completed
                val combinedRate = "USD: ${combinedRates["USD"]}\nEUR: ${combinedRates["EUR"]}"
                currencyTextView.text = combinedRate
                Log.d("rates", combinedRate)
            }
        }
    }

    private fun handleFailure() {
        currencyTextView.text = "Failed to fetch currency rates"
    }
}
