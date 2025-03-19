package com.sachkomaxim.lab3

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sachkomaxim.lab3.database.room.TravelRoute
import com.sachkomaxim.lab3.database.sqlite.TravelContract.TravelEntry
import com.sachkomaxim.lab3.viewmodel.TravelViewModel

class TravelRoutesActivity : AppCompatActivity() {

    //private lateinit var dbHelper: TravelDbHelper
    private lateinit var viewModel: TravelViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_travel_routes)

        viewModel = ViewModelProvider(this)[TravelViewModel::class.java]

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        //dbHelper = TravelDbHelper(this)

        recyclerView = findViewById(R.id.recyclerView)
        emptyView = findViewById(R.id.emptyView)

        recyclerView.layoutManager = LinearLayoutManager(this)

        observeViewModel()

        viewModel.loadTravelRoutes()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun observeViewModel() {
        /*val cursor = dbHelper.getAllTravelRoutes()

        if (cursor.count > 0) {
            recyclerView.visibility = View.VISIBLE
            emptyView.visibility = View.GONE
            recyclerView.adapter = TravelRoutesAdapter(cursor)
        } else {
            recyclerView.visibility = View.GONE
            emptyView.visibility = View.VISIBLE
        }*/
        viewModel.travelRoutes.observe(this) { travelRoutes ->
            if (travelRoutes.isNotEmpty()) {
                recyclerView.visibility = View.VISIBLE
                emptyView.visibility = View.GONE
                recyclerView.adapter = TravelRoutesAdapter(travelRoutes)
            } else {
                recyclerView.visibility = View.GONE
                emptyView.visibility = View.VISIBLE
            }
        }
    }

    /*override fun onDestroy() {
        dbHelper.close()
        super.onDestroy()
    }*/

    private inner class TravelRoutesAdapter(private val travelRoutes: List<TravelRoute>) :
        RecyclerView.Adapter<TravelRoutesAdapter.ViewHolder>() {

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val routeTextView: TextView = view.findViewById(R.id.routeTextView)
            val timeTextView: TextView = view.findViewById(R.id.timeTextView)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.travel_route_item, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            /*cursor.moveToPosition(position)

            val departure = cursor.getString(cursor.getColumnIndexOrThrow(TravelEntry.COLUMN_DEPARTURE))
            val arrival = cursor.getString(cursor.getColumnIndexOrThrow(TravelEntry.COLUMN_ARRIVAL))
            val time = cursor.getString(cursor.getColumnIndexOrThrow(TravelEntry.COLUMN_TIME))

            holder.routeTextView.text = "$departure → $arrival"
            holder.timeTextView.text = "Departs at: $time"*/

            val travelRoute = travelRoutes[position]

            holder.routeTextView.text = "${travelRoute.departure} → ${travelRoute.arrival}"
            holder.timeTextView.text = "Departs at: ${travelRoute.time}"
        }

        override fun getItemCount(): Int = travelRoutes.size
    }
}
