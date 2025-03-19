package com.sachkomaxim.lab3

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.sachkomaxim.lab3.database.sqlite.TravelDbHelper
import com.sachkomaxim.lab3.viewmodel.TravelViewModel

class InputFragment : Fragment() {
    private lateinit var view: View
    private lateinit var viewModel: TravelViewModel

    private lateinit var etDeparture: EditText
    private lateinit var etArrival: EditText
    private lateinit var spinnerDepartureTime: Spinner
    private lateinit var btnOk: Button
    private lateinit var openButton: Button
    private lateinit var clearButton: Button
    //private lateinit var dbHelper: TravelDbHelper

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_input, container, false)

        viewModel = ViewModelProvider(requireActivity())[TravelViewModel::class.java]

        etDeparture = view.findViewById(R.id.taskEditTextDeparture)
        etArrival = view.findViewById(R.id.taskEditTextArrival)
        spinnerDepartureTime = view.findViewById(R.id.spinnerDepartureTime)
        btnOk = view.findViewById(R.id.btnOk)
        openButton = view.findViewById(R.id.btnOpenDB)
        clearButton = view.findViewById(R.id.btnClearDB)

        //dbHelper = TravelDbHelper(requireContext())

        setupTextWatchers()
        setupSpinnerListener()
        setOkButtonClickListener()
        setOpenButtonOnClickListener()
        setClearButtonOnClickListener()
        setOutputFragmentResultListener()

        observeViewModel()

        return view
    }

    /*override fun onDestroy() {
        dbHelper.close()
        super.onDestroy()
    }*/

    private fun setupTextWatchers() {
        etDeparture.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (s.toString() != viewModel.departure.value) {
                    viewModel.setDeparture(s.toString())
                }
            }
        })

        etArrival.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (s.toString() != viewModel.arrival.value) {
                    viewModel.setArrival(s.toString())
                }
            }
        })
    }

    private fun setupSpinnerListener() {
        spinnerDepartureTime.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                viewModel.setTimePosition(position)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun observeViewModel() {
        viewModel.departure.observe(viewLifecycleOwner) { departure ->
            if (etDeparture.text.toString() != departure) {
                etDeparture.setText(departure)
            }
        }

        viewModel.arrival.observe(viewLifecycleOwner) { arrival ->
            if (etArrival.text.toString() != arrival) {
                etArrival.setText(arrival)
            }
        }

        viewModel.timePosition.observe(viewLifecycleOwner) { position ->
            if (spinnerDepartureTime.selectedItemPosition != position) {
                spinnerDepartureTime.setSelection(position)
            }
        }
    }

    private fun setOkButtonClickListener() {
        btnOk.setOnClickListener {
            if (!validateInput()) {
                return@setOnClickListener
            }

            val departure = etDeparture.text.toString().trim()
            val arrival = etArrival.text.toString().trim()
            val time = spinnerDepartureTime.selectedItem.toString()

            // Save to database
            /*val id = dbHelper.insertTravelRoute(departure, arrival, time)

            if (id != -1L) {
                Toast.makeText(requireContext(), R.string.data_saved, Toast.LENGTH_SHORT).show()
            }*/
            viewModel.insertTravelRoute(departure, arrival, time) {
                Toast.makeText(requireContext(), R.string.data_saved, Toast.LENGTH_SHORT).show()
            }

            val resultText = "Travel route: $departure -> $arrival\nDeparts at: $time"
            viewModel.setResultText(resultText)

            val result = Bundle()
            result.putString("departure", departure)
            result.putString("arrival", arrival)
            result.putString("time", time)
            parentFragmentManager.setFragmentResult("task", result)
        }
    }

    private fun validateInput(): Boolean {
        val missingFields = mutableListOf<String>()

        if (etDeparture.text.toString().trim().isEmpty()) {
            missingFields.add("\nDeparture point")
        }
        if (etArrival.text.toString().trim().isEmpty()) {
            missingFields.add("\nArrival point")
        }
        if (spinnerDepartureTime.selectedItemPosition == 0) {
            missingFields.add("\nTime of departure")
        }

        if (missingFields.isNotEmpty()) {
            val message = "Not filled in: " + missingFields.joinToString("; ")
            MainActivity.createDialogWindow(requireContext(), "Error", message)

            return false
        }

        return true
    }

    private fun setOutputFragmentResultListener() {
        parentFragmentManager.setFragmentResultListener("reset", this) { _, _ ->
            viewModel.resetInputs()
        }
    }

    private fun setOpenButtonOnClickListener() {
        openButton.setOnClickListener {
            val intent = Intent(requireContext(), TravelRoutesActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setClearButtonOnClickListener() {
        clearButton.setOnClickListener {
            /*val deletedRows = dbHelper.clearAllTravelRoutes()
            if (deletedRows > 0) {
                Toast.makeText(requireContext(), R.string.data_cleared, Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), R.string.no_saved_routes, Toast.LENGTH_SHORT).show()
            }*/
            viewModel.clearTravelRoutes { deletedRows ->
                if (deletedRows > 0) {
                    Toast.makeText(requireContext(), R.string.data_cleared, Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), R.string.no_saved_routes, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
