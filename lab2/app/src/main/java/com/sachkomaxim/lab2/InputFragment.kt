package com.sachkomaxim.lab2

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import androidx.fragment.app.Fragment

class InputFragment : Fragment() {
    private lateinit var view: View

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_input, container, false)

        setOkButtonClickListener()
        setOutputFragmentResultListener()

        return view
    }

    private fun setOkButtonClickListener() {
        val btnOk = view.findViewById<Button>(R.id.btnOk)!!

        btnOk.setOnClickListener {
            if (!validateInput()) {
                return@setOnClickListener
            }

            val departure = view.findViewById<EditText>(R.id.taskEditTextDeparture)!!.text.toString().trim()
            val arrival = view.findViewById<EditText>(R.id.taskEditTextArrival)!!.text.toString().trim()
            val spinnerDepartureTime = view.findViewById<Spinner>(R.id.spinnerDepartureTime)!!
            val time = spinnerDepartureTime.selectedItem.toString()

            val result = Bundle()
            result.putString("departure", departure)
            result.putString("arrival", arrival)
            result.putString("time", time)
            parentFragmentManager.setFragmentResult("task", result)
        }
    }

    private fun validateInput(): Boolean {
        val missingFields = mutableListOf<String>()

        val etDeparture = view.findViewById<EditText>(R.id.taskEditTextDeparture)!!
        val etArrival = view.findViewById<EditText>(R.id.taskEditTextArrival)!!
        val spinnerDepartureTime = view.findViewById<Spinner>(R.id.spinnerDepartureTime)!!

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
            MainActivity.createDialogWindow(requireContext(), "Помилка", message)

            return false
        }

        return true
    }

    private fun setOutputFragmentResultListener() {
        parentFragmentManager.setFragmentResultListener("reset", this) { _, _ ->
            val etDeparture = view.findViewById<EditText>(R.id.taskEditTextDeparture)!!
            val etArrival = view.findViewById<EditText>(R.id.taskEditTextArrival)!!
            val spinnerDepartureTime = view.findViewById<Spinner>(R.id.spinnerDepartureTime)!!

            etDeparture.setText("")
            etArrival.setText("")
            spinnerDepartureTime.setSelection(0)
        }
    }
}