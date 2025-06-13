package com.gorkemoji.meteo.ui.view

import android.annotation.SuppressLint
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.gorkemoji.meteo.R
import com.gorkemoji.meteo.adapter.HourlyForecastAdapter
import com.gorkemoji.meteo.data.model.DailyForecast
import com.gorkemoji.meteo.data.model.WeatherItem
import com.gorkemoji.meteo.data.model.WeatherResponse
import com.gorkemoji.meteo.data.network.RetrofitInstance
import com.gorkemoji.meteo.data.repository.ForecastRepository
import com.gorkemoji.meteo.databinding.ActivityForecastBinding
import com.gorkemoji.meteo.databinding.DailyForecastItemBinding
import com.gorkemoji.meteo.ui.factory.ForecastViewModelFactory
import com.gorkemoji.meteo.ui.viewmodel.ForecastViewModel
import com.gorkemoji.meteo.utils.PreferencesHelper
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ForecastActivity : AppCompatActivity() {
    private lateinit var viewModel: ForecastViewModel
    private lateinit var hourlyAdapter: HourlyForecastAdapter
    private lateinit var dailyCardBinding: DailyForecastItemBinding
    private lateinit var binding: ActivityForecastBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForecastBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.root.background = getGradientForTime()

        setupViewModel()
        setupRecyclerView()
        setupDailyCard()
        setupSwipeRefresh()
        observeViewModel()

        val selectedCityLat = PreferencesHelper.get(this, "SELECTED_CITY_LAT")
        val selectedCityLon = PreferencesHelper.get(this, "SELECTED_CITY_LON")

        loadWeatherData(selectedCityLat?.toDouble() ?: 0.0, selectedCityLon?.toDouble() ?: 0.0)
    }

    private fun setupViewModel() {
        val apiService = RetrofitInstance.forecastApi
        val repository = ForecastRepository(apiService)
        val factory = ForecastViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[ForecastViewModel::class.java]
    }

    private fun setupRecyclerView() {
        hourlyAdapter = HourlyForecastAdapter(emptyList())
        binding.hourlyForecastRv.apply {
            layoutManager = LinearLayoutManager(this@ForecastActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = hourlyAdapter
        }
    }

    private fun setupDailyCard() {
        dailyCardBinding = DailyForecastItemBinding.inflate(LayoutInflater.from(this))
        binding.dailyForecastContainer.addView(dailyCardBinding.root)
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            val cityName = binding.cityNameTxt.text.toString()
            if (cityName.isNotBlank())
                viewModel.fetchWeatherForecastByName(cityName, "metric", "tr")
        }
    }

    private fun loadWeatherData(latitude: Double, longitude: Double) {
        viewModel.fetchWeatherForecastByCoords(latitude, longitude, "metric", "tr")
    }

    private fun observeViewModel() {
        viewModel.weatherData.observe(this) { weatherResponse ->
            weatherResponse?.let { updateUI(it) }
        }

        viewModel.isLoading.observe(this) { isLoading ->
            binding.swipeRefreshLayout.isRefreshing = isLoading
        }
    }

    private fun getGradientForTime(): GradientDrawable {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)

        val startColor: Int
        val endColor: Int

        when (hour) {
            in 6..11 -> {
                startColor = ContextCompat.getColor(this, R.color.morning_start)
                endColor = ContextCompat.getColor(this, R.color.morning_end)
            }
            in 12..17 -> {
                startColor = ContextCompat.getColor(this, R.color.afternoon_start)
                endColor = ContextCompat.getColor(this, R.color.afternoon_end)
            }
            in 18..20 -> {
                startColor = ContextCompat.getColor(this, R.color.evening_start)
                endColor = ContextCompat.getColor(this, R.color.evening_end)
            }
            else -> {
                startColor = ContextCompat.getColor(this, R.color.night_start)
                endColor = ContextCompat.getColor(this, R.color.night_end)
            }
        }

        return GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM,
            intArrayOf(startColor, endColor)
        )
    }


    private fun updateUI(weatherResponse: WeatherResponse) {
        try {
            updateCurrentWeatherInfo(weatherResponse)
            updateHourlyForecast(weatherResponse)
            fillDailyForecastCard(weatherResponse.list)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateCurrentWeatherInfo(weatherResponse: WeatherResponse) {
        binding.cityNameTxt.text = weatherResponse.city.name

        val currentTime = SimpleDateFormat("HH:mm", Locale("tr")).format(Date())
        binding.lastUpdatedTxt.text = getString(R.string.updated_at) + ": $currentTime"

        val currentWeather = weatherResponse.list.firstOrNull()
        currentWeather?.let { weather ->
            binding.currentTempTxt.text = weather.main.temp.toInt().toString() + " °C"

            val description = weather.weather.firstOrNull()?.description
            binding.descriptionTxt.text = description?.replaceFirstChar { char ->
                if (char.isLowerCase()) char.titlecase(Locale("tr")) else char.toString()
            } ?: getString(R.string.description)

            binding.lowHighTxt.text = getString(R.string.low) + ": " + weather.main.tempMin.toInt().toString() + " °C / " + getString(R.string.high) + ": " + weather.main.tempMax.toInt().toString() + " °C"
            binding.feelsLikeTxt.text = getString(R.string.feels_like) + ": " + weather.main.feelsLike.toInt().toString() + " °C"
        }
    }

    private fun updateHourlyForecast(weatherResponse: WeatherResponse) {
        val currentTimestamp = System.currentTimeMillis() / 1000L
        val hourlyData = weatherResponse.list.filter { forecast ->
            forecast.dt >= currentTimestamp && forecast.dt < (currentTimestamp + (24 * 60 * 60))
        }.take(24)

        hourlyAdapter.updateData(hourlyData)
    }

    private fun fillDailyForecastCard(forecastList: List<WeatherItem>) {
        val dailyDataList = processDailyForecastsForStaticCard(forecastList)

        fillDayData(dailyDataList, 0,
            dailyCardBinding.day1Name, dailyCardBinding.day1Description, dailyCardBinding.day1Temp)
        fillDayData(dailyDataList, 1,
            dailyCardBinding.day2Name, dailyCardBinding.day2Description, dailyCardBinding.day2Temp)
        fillDayData(dailyDataList, 2,
            dailyCardBinding.day3Name, dailyCardBinding.day3Description, dailyCardBinding.day3Temp)
        fillDayData(dailyDataList, 3,
            dailyCardBinding.day4Name, dailyCardBinding.day4Description, dailyCardBinding.day4Temp)
        fillDayData(dailyDataList, 4,
            dailyCardBinding.day5Name, dailyCardBinding.day5Description, dailyCardBinding.day5Temp)
    }

    @SuppressLint("SetTextI18n")
    private fun fillDayData(dailyDataList: List<DailyForecast>, index: Int, nameView: android.widget.TextView, descView: android.widget.TextView, tempView: android.widget.TextView) {
        if (index < dailyDataList.size) {
            val dayData = dailyDataList[index]
            nameView.text = dayData.date
            descView.text = dayData.description
            tempView.text = "${dayData.minTemp}° / ${dayData.maxTemp}°"
        } else {
            nameView.text = "-"
            descView.text = "-"
            tempView.text = "-"
        }
    }

    private fun processDailyForecastsForStaticCard(forecastList: List<WeatherItem>): List<DailyForecast> {
        val dailyMap = mutableMapOf<String, MutableList<WeatherItem>>()
        val sdfDay = SimpleDateFormat("EEEE", Locale("tr"))
        val sdfDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        val todayCalendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        forecastList.forEach { forecast ->
            val forecastDate = Date(forecast.dt * 1000L)
            val forecastCalendar = Calendar.getInstance().apply { time = forecastDate }

            if (forecastCalendar.timeInMillis >= todayCalendar.timeInMillis) {
                val dateKey = sdfDate.format(forecastDate)
                dailyMap.getOrPut(dateKey) { mutableListOf() }.add(forecast)
            }
        }

        val resultList = mutableListOf<DailyForecast>()

        dailyMap.entries.sortedBy { it.key }.take(5).forEachIndexed { index, (_, dailyForecasts) ->
            if (dailyForecasts.isNotEmpty()) {
                val minTemp = dailyForecasts.minOfOrNull { it.main.tempMin }?.toInt() ?: 0
                val maxTemp = dailyForecasts.maxOfOrNull { it.main.tempMax }?.toInt() ?: 0

                val representativeForecast = findRepresentativeForecast(dailyForecasts)
                val forecastDate = Date(representativeForecast.dt * 1000L)

                val dayName = when (index) {
                    0 -> "Bugün"
                    1 -> "Yarın"
                    else -> sdfDay.format(forecastDate).replaceFirstChar {
                        if (it.isLowerCase()) it.titlecase(Locale("tr")) else it.toString()
                    }
                }

                resultList.add(
                    DailyForecast(
                        date = dayName,
                        icon = representativeForecast.weather.firstOrNull()?.icon ?: "01d",
                        description = representativeForecast.weather.firstOrNull()?.description?.replaceFirstChar {
                            if (it.isLowerCase()) it.titlecase(Locale("tr")) else it.toString()
                        } ?: "Bilinmiyor",
                        minTemp = minTemp,
                        maxTemp = maxTemp
                    )
                )
            }
        }

        return resultList
    }

    private fun findRepresentativeForecast(dailyForecasts: List<WeatherItem>): WeatherItem {
        val noonForecast = dailyForecasts.find { forecast ->
            val hour = Calendar.getInstance().apply {
                timeInMillis = forecast.dt * 1000L
            }.get(Calendar.HOUR_OF_DAY)
            hour in 11..14
        }

        return noonForecast ?: dailyForecasts.first()
    }
}