package com.gorkemoji.meteo.data.model

data class DailyForecast(
    val date: String,
    val icon: String,
    val description: String,
    val minTemp: Int,
    val maxTemp: Int
)