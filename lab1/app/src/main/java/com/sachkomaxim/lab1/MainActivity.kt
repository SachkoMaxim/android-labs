package com.sachkomaxim.lab1

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setOkButtonClickListener()
    }

    private fun setOkButtonClickListener() {
        val btnOk = findViewById<Button>(R.id.btnOk)

        btnOk.setOnClickListener {
            if (!validateInput()) {
                return@setOnClickListener
            }

            val departure = findViewById<EditText>(R.id.taskEditTextDeparture).text.toString().trim()
            val arrival = findViewById<EditText>(R.id.taskEditTextArrival).text.toString().trim()
            val spinnerDepartureTime = findViewById<Spinner>(R.id.spinnerDepartureTime)
            val time = spinnerDepartureTime.selectedItem.toString()

            val resultText = "Шлях подорожі: $departure -> $arrival\n" +
                    "Відправляється о: $time"

            createDialogWindow("Потяг \"Березневий\"", resultText)
        }
    }

    private fun validateInput(): Boolean {
        val missingFields = mutableListOf<String>()

        val etDeparture = findViewById<EditText>(R.id.taskEditTextDeparture)
        val etArrival = findViewById<EditText>(R.id.taskEditTextArrival)
        val spinnerDepartureTime = findViewById<Spinner>(R.id.spinnerDepartureTime)

        if (etDeparture.text.toString().trim().isEmpty()) {
            missingFields.add("\nПункт відправлення")
        }
        if (etArrival.text.toString().trim().isEmpty()) {
            missingFields.add("\nПункт прибуття")
        }
        if (spinnerDepartureTime.selectedItemPosition == 0) {
            missingFields.add("\nЧас відправлення")
        }

        if (missingFields.isNotEmpty()) {
            val message = "Не заповнено: " + missingFields.joinToString("; ")
            createDialogWindow("Помилка", message)

            return false
        }

        return true
    }

    private fun createDialogWindow (title: String, text: String) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_window, null)
        val tvDialogText = dialogView.findViewById<TextView>(R.id.tvDialogText)
        tvDialogText.text = text

        AlertDialog.Builder(this)
            .setTitle(title)
            .setView(dialogView)
            .setPositiveButton("ОК", null)
            .show()
    }
}
