package com.sachkomaxim.lab2

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentTransaction

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Begin the transaction
        val ft: FragmentTransaction = supportFragmentManager.beginTransaction()
        // Replace the contents of the container with the new fragment
        ft.replace(R.id.fragment_input_layout, InputFragment())
        ft.replace(R.id.fragment_output_layout, OutputFragment())
        // or ft.add(R.id.your_placeholder, new FooFragment());
        // Complete the changes added above
        ft.commit()
    }

    companion object {
        fun createDialogWindow(context: Context, title: String, text: String) {
            val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_window, null)
            val tvDialogText = dialogView.findViewById<TextView>(R.id.tvDialogText)
            tvDialogText.text = text

            AlertDialog.Builder(context)
                .setTitle(title)
                .setView(dialogView)
                .setPositiveButton("ОК", null)
                .show()
        }
    }
}
