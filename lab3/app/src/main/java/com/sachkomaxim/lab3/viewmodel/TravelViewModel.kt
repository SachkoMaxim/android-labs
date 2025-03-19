package com.sachkomaxim.lab3.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.sachkomaxim.lab3.database.room.TravelRepository
import com.sachkomaxim.lab3.database.room.TravelRoute
import kotlinx.coroutines.launch

class TravelViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = TravelRepository(application)

    // Input form state
    private val _departure = MutableLiveData<String>()
    val departure: LiveData<String> = _departure

    private val _arrival = MutableLiveData<String>()
    val arrival: LiveData<String> = _arrival

    private val _timePosition = MutableLiveData<Int>()
    val timePosition: LiveData<Int> = _timePosition

    // Output state
    private val _resultText = MutableLiveData<String>()
    val resultText: LiveData<String> = _resultText

    private val _showResult = MutableLiveData<Boolean>()
    val showResult: LiveData<Boolean> = _showResult

    // Travel routes
    private val _travelRoutes = MutableLiveData<List<TravelRoute>>()
    val travelRoutes: LiveData<List<TravelRoute>> = _travelRoutes

    init {
        _departure.value = ""
        _arrival.value = ""
        _timePosition.value = 0
        _showResult.value = false
        _resultText.value = ""
    }

    fun setDeparture(departure: String) {
        _departure.value = departure
    }

    fun setArrival(arrival: String) {
        _arrival.value = arrival
    }

    fun setTimePosition(position: Int) {
        _timePosition.value = position
    }

    fun setResultText(text: String) {
        _resultText.value = text
        _showResult.value = true
    }

    fun hideResult() {
        _showResult.value = false
        _resultText.value = ""
    }

    fun resetInputs() {
        _departure.value = ""
        _arrival.value = ""
        _timePosition.value = 0
    }

    fun insertTravelRoute(departure: String, arrival: String, time: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val id = repository.insertTravelRoute(departure, arrival, time)
            if (id != -1L) {
                onSuccess()
            }
        }
    }

    fun loadTravelRoutes() {
        viewModelScope.launch {
            _travelRoutes.value = repository.getAllTravelRoutes()
        }
    }

    fun clearTravelRoutes(onSuccess: (Int) -> Unit) {
        viewModelScope.launch {
            val deletedRows = repository.clearAllTravelRoutes()
            onSuccess(deletedRows)
        }
    }
}
