package com.sachkomaxim.lab2

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment

class OutputFragment : Fragment() {
    private lateinit var view: View

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_output, container, false)

        setInputFragmentResultListener()
        setCancelButtonOnClickListener()

        return view
    }

    private fun setInputFragmentResultListener() {
        parentFragmentManager.setFragmentResultListener("task", this) { _, bundle ->
            val infoHeader = view.findViewById<TextView>(R.id.infoHeader)!!
            val infoTV = view.findViewById<TextView>(R.id.infoTV)!!

            val resultText = "Шлях подорожі: ${bundle.getString("departure")} -> " +
                    "${bundle.getString("arrival")}\n" +
                    "Відправляється о: ${bundle.getString("time")}"

            infoHeader.visibility = View.VISIBLE
            infoTV.text = resultText

            MainActivity.createDialogWindow(requireContext(), "Потяг \"Березневий\"", resultText)
        }
    }

    private fun setCancelButtonOnClickListener() {
        val cancelButton = view.findViewById<Button>(R.id.btnCancel)!!
        cancelButton.setOnClickListener {
            val infoHeader = view.findViewById<TextView>(R.id.infoHeader)!!
            val infoTV = view.findViewById<TextView>(R.id.infoTV)!!
            infoHeader.visibility = View.INVISIBLE
            infoTV.text = ""
            parentFragmentManager.setFragmentResult("reset", Bundle())
        }
    }
}