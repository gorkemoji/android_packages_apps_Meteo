package com.gorkemoji.meteo.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gorkemoji.meteo.data.model.GeocodingResponse
import com.gorkemoji.meteo.data.repository.CityRepository
import kotlinx.coroutines.launch

class SearchCityViewModel(private val repository: CityRepository) : ViewModel() {
    private val _cities = MutableLiveData<List<GeocodingResponse>>()
    val cities: LiveData<List<GeocodingResponse>> = _cities

    fun searchCity(cityName: String, apiKey: String) {
        viewModelScope.launch {
            val result = repository.searchCity(cityName, apiKey)
            _cities.postValue(result ?: emptyList())
        }
    }
}