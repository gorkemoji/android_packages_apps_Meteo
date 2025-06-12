package com.gorkemoji.meteo.data.network

import com.gorkemoji.meteo.data.model.GeocodingResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface GeoApiService {
    @GET("geo/1.0/direct")
    suspend fun getCityDetails(
        @Query("q") cityName: String,
        @Query("limit") limit: Int = 5,
        @Query("appid") apiKey: String
    ): List<GeocodingResponse>
}