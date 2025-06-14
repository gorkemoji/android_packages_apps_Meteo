package com.gorkemoji.meteo.utils

import android.content.Context
import android.graphics.drawable.GradientDrawable
import androidx.core.content.ContextCompat
import com.gorkemoji.meteo.R
import java.util.Calendar

object WeatherBackgroundHelper {
    fun getGradientForTime(context: Context): GradientDrawable {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)

        val (startColorResId, endColorResId) = when (hour) {
            in 6..11 -> Pair(R.color.morning_start, R.color.morning_end)
            in 12..17 -> Pair(R.color.afternoon_start, R.color.afternoon_end)
            in 18..20 -> Pair(R.color.evening_start, R.color.evening_end)
            else -> Pair(R.color.night_start, R.color.night_end)
        }

        return createGradient(context, startColorResId, endColorResId)
    }

    fun getGradientForWeather(context: Context, iconCode: String?): GradientDrawable {
        val colorPair: Pair<Int, Int>? = when (iconCode) {
            "01d" -> Pair(R.color.sunny_start, R.color.sunny_end)
            "01n" -> Pair(R.color.clear_night_start, R.color.clear_night_end)
            "02d", "02n" -> Pair(R.color.few_clouds_start, R.color.few_clouds_end)
            "03d", "03n", "04d", "04n" -> Pair(R.color.cloudy_start, R.color.cloudy_end)
            "09d", "09n", "10d", "10n" -> Pair(R.color.rainy_start, R.color.rainy_end)
            "11d", "11n" -> Pair(R.color.storm_start, R.color.storm_end)
            "13d", "13n" -> Pair(R.color.snow_start, R.color.snow_end)
            "50d", "50n" -> Pair(R.color.foggy_start, R.color.foggy_end)
            else -> null
        }

        return if (colorPair != null)
            createGradient(context, colorPair.first, colorPair.second)
        else getGradientForTime(context)

    }

    private fun createGradient(context: Context, startColor: Int, endColor: Int): GradientDrawable {
        return GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM,
            intArrayOf(
                ContextCompat.getColor(context, startColor),
                ContextCompat.getColor(context, endColor)
            )
        )
    }
}