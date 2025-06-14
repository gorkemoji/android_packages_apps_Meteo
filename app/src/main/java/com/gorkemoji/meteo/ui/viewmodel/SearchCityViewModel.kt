package com.gorkemoji.meteo.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gorkemoji.meteo.data.model.GeocodingResponse
import com.gorkemoji.meteo.data.repository.CityRepository
import kotlinx.coroutines.launch
import com.gorkemoji.meteo.utils.Constants

class SearchCityViewModel(private val repository: CityRepository) : ViewModel() {
    private val _cities = MutableLiveData<List<GeocodingResponse>>()
    val cities: LiveData<List<GeocodingResponse>> = _cities

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    fun searchCity(cityName: String) {
        _errorMessage.value = null
        viewModelScope.launch {
            try {
                val result = repository.searchCity(cityName, Constants.API_KEY)
                _cities.postValue(result ?: emptyList())
                if (result.isNullOrEmpty())
                    _errorMessage.postValue("Error")
            } catch (e: Exception) {
                _errorMessage.postValue(e.message)
                _cities.postValue(emptyList())
            }
        }
    }
}