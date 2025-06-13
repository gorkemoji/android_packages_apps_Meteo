package com.gorkemoji.meteo.utils

import com.gorkemoji.meteo.R

object WeatherIconMatcher {
    fun getWeatherIconResId(iconCode: String?): Int {
        return when (iconCode) {
            "01d" -> R.drawable.ic_weather_day_clear_24
            "01n" -> R.drawable.ic_weather_night_clear_24
            "02d" -> R.drawable.ic_weather_cloudy_24 // Parçalı bulutlu (gündüz)
            "02n" -> R.drawable.ic_weather_night_partly_cloudy_24
            "03d", "03n" -> R.drawable.ic_weather_cloudy_24 // Dağınık bulutlar
            "04d", "04n" -> R.drawable.ic_weather_cloudy_24
            "09d", "09n" -> R.drawable.ic_weather_rainy_24
            "10d" -> R.drawable.ic_weather_rainy_24
            "10n" -> R.drawable.ic_weather_rainy_24
            "11d", "11n" -> R.drawable.ic_weather_thunderstorm_24
            "13d", "13n" -> R.drawable.ic_weather_snowy_24
            "50d", "50n" -> R.drawable.ic_weather_foggy_24
            else -> R.drawable.ic_weather_day_clear_24
        }
    }
}