package com.sachkomaxim.lab3

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.sachkomaxim.lab3.viewmodel.TravelViewModel

class OutputFragment : Fragment() {
    private lateinit var view: View
    private lateinit var viewModel: TravelViewModel

    private lateinit var infoHeader: TextView
    private lateinit var infoTV: TextView
    private lateinit var cancelButton: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_output, container, false)

        viewModel = ViewModelProvider(requireActivity())[TravelViewModel::class.java]

        infoHeader = view.findViewById(R.id.infoHeader)
        infoTV = view.findViewById(R.id.infoTV)
        cancelButton = view.findViewById(R.id.btnCancel)

        setInputFragmentResultListener()
        setCancelButtonOnClickListener()

        observeViewModel()

        return view
    }

    private fun observeViewModel() {
        viewModel.resultText.observe(viewLifecycleOwner) { resultText ->
            infoTV.text = resultText
        }

        viewModel.showResult.observe(viewLifecycleOwner) { showResult ->
            infoHeader.visibility = if (showResult) View.VISIBLE else View.INVISIBLE
        }
    }

    private fun setInputFragmentResultListener() {
        parentFragmentManager.setFragmentResultListener("task", this) { _, bundle ->
            val departure = bundle.getString("departure", "")
            val arrival = bundle.getString("arrival", "")
            val time = bundle.getString("time", "")

            val resultText = "Travel route: $departure -> $arrival\nDeparts at: $time"

            viewModel.setResultText(resultText)

            MainActivity.createDialogWindow(requireContext(), "Train \"March\"", resultText)
        }
    }

    private fun setCancelButtonOnClickListener() {
        cancelButton.setOnClickListener {
            viewModel.hideResult()
            parentFragmentManager.setFragmentResult("reset", Bundle())
        }
    }
}