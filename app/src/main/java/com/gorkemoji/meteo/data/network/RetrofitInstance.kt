package com.gorkemoji.meteo.data.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    private const val BASE_URL = "https://api.openweathermap.org/"

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val forecastApi: ForecastApiService by lazy { retrofit.create(ForecastApiService::class.java) }
    val geoApi: GeoApiService by lazy { retrofit.create(GeoApiService::class.java) }
}