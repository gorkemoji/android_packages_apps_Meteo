package com.gorkemoji.meteo.data.repository

import com.gorkemoji.meteo.data.model.GeocodingResponse
import com.gorkemoji.meteo.data.network.GeoApiService

class CityRepository(private val api: GeoApiService) {
    suspend fun searchCity(cityName: String, apiKey: String): List<GeocodingResponse>? {
        return try { api.getCityDetails(cityName, 5, apiKey) }
        catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}