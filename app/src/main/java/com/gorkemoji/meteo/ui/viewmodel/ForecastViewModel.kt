package com.gorkemoji.meteo.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gorkemoji.meteo.data.model.WeatherResponse
import com.gorkemoji.meteo.data.repository.ForecastRepository

import kotlinx.coroutines.launch

class ForecastViewModel(private val repository: ForecastRepository) : ViewModel() {
    private val _weatherData = MutableLiveData<WeatherResponse?>()
    val weatherData: LiveData<WeatherResponse?> = _weatherData

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun fetchWeatherForecastByName(city: String, apiKey: String, metric: String, lang: String) {
        _isLoading.value = true

        viewModelScope.launch {
            try {
                val response = repository.getWeatherForecast(city, apiKey, metric, lang)

                if (response.isSuccessful) _weatherData.value = response.body()
                else _weatherData.value = null
            } catch (e: Exception) {
                _weatherData.value = null
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun fetchWeatherForecastByCoords(lat: Double, lon: Double, apiKey: String, metric: String, lang: String) {
        _isLoading.value = true

        viewModelScope.launch {
            try {
                val response = repository.getWeatherForecastByCoords(lat, lon, apiKey, metric, lang)

                if (response.isSuccessful) _weatherData.value = response.body()
                else _weatherData.value = null
            } catch (e: Exception) {
                _weatherData.value = null
            } finally {
                _isLoading.value = false
            }
        }
    }
}