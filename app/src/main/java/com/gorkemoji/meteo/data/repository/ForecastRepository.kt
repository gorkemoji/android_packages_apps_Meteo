package com.gorkemoji.meteo.data.repository

import com.gorkemoji.meteo.data.model.WeatherResponse
import com.gorkemoji.meteo.data.network.ForecastApiService
import retrofit2.Response

class ForecastRepository(private val apiService: ForecastApiService) {
    suspend fun getWeatherForecast(
        city: String,
        apiKey: String,
        units: String,
        lang: String
    ): Response<WeatherResponse> {
        return apiService.getWeatherForecast(city, apiKey, units, lang)
    }

    suspend fun getWeatherForecastByCoords(
        lat: Double,
        lon: Double,
        apiKey: String,
        units: String,
        lang: String
    ): Response<WeatherResponse> {
        return apiService.getWeatherForecastByCoords(lat, lon, apiKey, units, lang)
    }
}