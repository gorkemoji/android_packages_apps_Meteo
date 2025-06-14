package com.gorkemoji.meteo.ui.view

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.MenuRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
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
import com.gorkemoji.meteo.ui.view.fragment.SearchCityBottomSheetFragment
import com.gorkemoji.meteo.ui.viewmodel.ForecastViewModel
import com.gorkemoji.meteo.utils.PreferencesHelper
import com.gorkemoji.meteo.utils.WeatherBackgroundHelper.getGradientForWeather
import com.gorkemoji.meteo.utils.WeatherIconMatcher.getWeatherIconResId
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ForecastActivity : AppCompatActivity() {
    private lateinit var viewModel: ForecastViewModel
    private lateinit var hourlyAdapter: HourlyForecastAdapter
    private lateinit var dailyCardBinding: DailyForecastItemBinding
    private lateinit var binding: ActivityForecastBinding
    //private lateinit var appLanguage: String

    companion object {
        const val REQUEST_KEY_CITY_SELECTED = "city_selected_request_key"
        const val BUNDLE_KEY_CITY_NAME = "city_name_bundle_key"
        const val BUNDLE_KEY_COUNTRY_CODE = "country_code_bundle_key"
        const val BUNDLE_KEY_LAT = "lat_bundle_key"
        const val BUNDLE_KEY_LON = "lon_bundle_key"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForecastBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //appLanguage = PreferencesHelper.get(this, "APP_LANGUAGE").toString()

        setupViewModel()
        setupRecyclerView()
        setupDailyCard()
        setupSwipeRefresh()
        observeViewModel()
        setupFragmentResultListener()

        checkAndLoadCityData()
    }

    private fun checkAndLoadCityData() {
        val selectedCityName = PreferencesHelper.get(this, "SELECTED_CITY_NAME")
        val selectedCityLat = PreferencesHelper.get(this, "SELECTED_CITY_LAT")
        val selectedCityLon = PreferencesHelper.get(this, "SELECTED_CITY_LON")

        val hasValidCity = !selectedCityName.isNullOrBlank() &&
                !selectedCityLat.isNullOrBlank() && selectedCityLat.toDoubleOrNull() != 0.0 &&
                !selectedCityLon.isNullOrBlank() && selectedCityLon.toDoubleOrNull() != 0.0

        if (hasValidCity) {
            if (selectedCityLat != null) if (selectedCityLon != null)
                loadWeatherData(selectedCityLat.toDouble(), selectedCityLon.toDouble())

            binding.dropdownIcon.visibility = View.VISIBLE
            binding.dropdownIcon.setOnClickListener { showCitySelectionPopup(it, R.menu.choose_city_popup_menu) }
        } else {
            binding.dropdownIcon.visibility = View.INVISIBLE
            showSearchCityBottomSheet()
        }
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
            val selectedCityLat = PreferencesHelper.get(this, "SELECTED_CITY_LAT")
            val selectedCityLon = PreferencesHelper.get(this, "SELECTED_CITY_LON")

            if (!selectedCityLat.isNullOrBlank() && !selectedCityLon.isNullOrBlank()
                && selectedCityLat.toDoubleOrNull() != 0.0 && selectedCityLon.toDoubleOrNull() != 0.0) {
                loadWeatherData(selectedCityLat.toDouble(), selectedCityLon.toDouble())
            } else {
                binding.swipeRefreshLayout.isRefreshing = false
                showSearchCityBottomSheet()
                Toast.makeText(this, getString(R.string.choose_city_to_use), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadWeatherData(latitude: Double, longitude: Double) {
        viewModel.fetchWeatherForecastByCoords(latitude, longitude, "metric", Locale.getDefault().toString())
    }

    private fun observeViewModel() {
        viewModel.weatherData.observe(this) { weatherResponse -> weatherResponse?.let { updateUI(it) } }
        viewModel.isLoading.observe(this) { isLoading -> binding.swipeRefreshLayout.isRefreshing = isLoading }
    }

    private fun updateUI(weatherResponse: WeatherResponse) {
        try {
            updateCurrentWeatherInfo(weatherResponse)
            updateHourlyForecast(weatherResponse)
            fillDailyForecastCard(weatherResponse.list)

            val currentWeather = weatherResponse.list.firstOrNull()

            val iconCode = currentWeather?.weather?.firstOrNull()?.icon
            val iconResId = getWeatherIconResId(iconCode)

            binding.currentWeatherIcon.setImageResource(iconResId)
            binding.root.background = getGradientForWeather(this, iconCode)

            binding.dropdownIcon.visibility = View.VISIBLE
            binding.dropdownIcon.setOnClickListener { showCitySelectionPopup(it, R.menu.choose_city_popup_menu) }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, getString(R.string.error) + ": " + e.message, Toast.LENGTH_LONG).show()
            binding.dropdownIcon.visibility = View.INVISIBLE
        }
    }

    private fun showSearchCityBottomSheet() {
        val bottomSheetFragment = SearchCityBottomSheetFragment()
        bottomSheetFragment.show(supportFragmentManager, bottomSheetFragment.tag)
    }

    private fun setupFragmentResultListener() {
        supportFragmentManager.setFragmentResultListener(REQUEST_KEY_CITY_SELECTED, this) { _, bundle ->
            val cityName = bundle.getString(BUNDLE_KEY_CITY_NAME)
            val countryCode = bundle.getString(BUNDLE_KEY_COUNTRY_CODE)
            val lat = bundle.getDouble(BUNDLE_KEY_LAT)
            val lon = bundle.getDouble(BUNDLE_KEY_LON)

            if (!cityName.isNullOrBlank() && !countryCode.isNullOrBlank() && lat != 0.0 && lon != 0.0) {
                PreferencesHelper.save(this, "SELECTED_CITY_NAME", cityName)
                PreferencesHelper.save(this, "SELECTED_CITY_COUNTRY", countryCode)
                PreferencesHelper.save(this, "SELECTED_CITY_LAT", lat.toString())
                PreferencesHelper.save(this, "SELECTED_CITY_LON", lon.toString())
                loadWeatherData(lat, lon)

                binding.dropdownIcon.visibility = View.VISIBLE
                binding.dropdownIcon.setOnClickListener { showCitySelectionPopup(it, R.menu.choose_city_popup_menu) }
            } else Toast.makeText(this, getString(R.string.choose_city_to_use), Toast.LENGTH_LONG).show()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateCurrentWeatherInfo(weatherResponse: WeatherResponse) {
        binding.cityNameTxt.text = weatherResponse.city.name

        val currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        binding.lastUpdatedTxt.text = getString(R.string.updated_at) + " $currentTime"

        val currentWeather = weatherResponse.list.firstOrNull()

        val todayStartCalendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val tomorrowStartCalendar = Calendar.getInstance().apply {
            timeInMillis = todayStartCalendar.timeInMillis
            add(Calendar.DAY_OF_YEAR, 1)
        }

        val todayForecasts = weatherResponse.list.filter { item ->
            val itemDate = Date(item.dt * 1000L)
            itemDate.time >= todayStartCalendar.timeInMillis && itemDate.time < tomorrowStartCalendar.timeInMillis
        }

        val overallMinTempToday = todayForecasts.minOfOrNull { it.main.tempMin }?.toInt()
        val overallMaxTempToday = todayForecasts.maxOfOrNull { it.main.tempMax }?.toInt()

        currentWeather?.let { weather ->
            binding.currentTempTxt.text = weather.main.temp.toInt().toString() + " °C"

            val description = weather.weather.firstOrNull()?.description
            binding.descriptionTxt.text = description?.replaceFirstChar { char ->
                if (char.isLowerCase()) char.titlecase(Locale.getDefault()) else char.toString()
            } ?: getString(R.string.description)

            binding.lowHighTxt.text = getString(R.string.low) + ": " + (overallMinTempToday?.toString() ?: "-") + " °C / " + getString(R.string.high) + ": " + (overallMaxTempToday?.toString() ?: "-") + " °C"
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
            dailyCardBinding.day1Name, dailyCardBinding.day1Description, dailyCardBinding.day1Temp, dailyCardBinding.day1Icon)
        fillDayData(dailyDataList, 1,
            dailyCardBinding.day2Name, dailyCardBinding.day2Description, dailyCardBinding.day2Temp, dailyCardBinding.day2Icon)
        fillDayData(dailyDataList, 2,
            dailyCardBinding.day3Name, dailyCardBinding.day3Description, dailyCardBinding.day3Temp, dailyCardBinding.day3Icon)
        fillDayData(dailyDataList, 3,
            dailyCardBinding.day4Name, dailyCardBinding.day4Description, dailyCardBinding.day4Temp, dailyCardBinding.day4Icon)
        fillDayData(dailyDataList, 4,
            dailyCardBinding.day5Name, dailyCardBinding.day5Description, dailyCardBinding.day5Temp, dailyCardBinding.day5Icon)
    }

    @SuppressLint("SetTextI18n")
    private fun fillDayData(dailyDataList: List<DailyForecast>, index: Int, nameView: android.widget.TextView, descView: android.widget.TextView, tempView: android.widget.TextView, iconView: ImageView) {
        if (index < dailyDataList.size) {
            val dayData = dailyDataList[index]
            nameView.text = dayData.date
            descView.text = dayData.description
            tempView.text = "${dayData.minTemp}° / ${dayData.maxTemp}°"

            val iconResId = getWeatherIconResId(dayData.icon)
            iconView.setImageResource(iconResId)
        } else {
            nameView.text = "-"
            descView.text = "-"
            tempView.text = "-"
            iconView.setImageResource(R.drawable.ic_weather_day_clear_24)
        }
    }

    private fun processDailyForecastsForStaticCard(forecastList: List<WeatherItem>): List<DailyForecast> {
        val dailyMap = mutableMapOf<String, MutableList<WeatherItem>>()
        val sdfDay = SimpleDateFormat("EEEE", Locale.getDefault())
        val sdfDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        val todayCalendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val desiredDates = mutableSetOf<String>()
        for (i in 0 until 5) {
            val calendar = Calendar.getInstance().apply {
                timeInMillis = todayCalendar.timeInMillis
                add(Calendar.DAY_OF_YEAR, i + 1)
            }
            desiredDates.add(sdfDate.format(calendar.time))
        }

        forecastList.forEach { forecast ->
            val forecastDate = Date(forecast.dt * 1000L)
            val dateKey = sdfDate.format(forecastDate)
            if (desiredDates.contains(dateKey))
                dailyMap.getOrPut(dateKey) { mutableListOf() }.add(forecast)
        }

        val resultList = mutableListOf<DailyForecast>()

        dailyMap.entries.sortedBy { it.key }.take(5).forEachIndexed { index, (dateKey, dailyForecasts) ->
            if (dailyForecasts.isNotEmpty()) {
                val minTemp = dailyForecasts.minOfOrNull { it.main.tempMin }?.toInt() ?: 0
                val maxTemp = dailyForecasts.maxOfOrNull { it.main.tempMax }?.toInt() ?: 0

                val representativeForecast = findRepresentativeForecast(dailyForecasts)
                val currentDayAsDate = sdfDate.parse(dateKey)

                val dayName = when (index) {
                    0 -> getString(R.string.tomorrow)
                    else -> currentDayAsDate?.let {
                        sdfDay.format(it).replaceFirstChar { char ->
                            if (char.isLowerCase()) char.titlecase(Locale.getDefault()) else char.toString()
                        }
                    } ?: getString(R.string.unknown)
                }

                resultList.add(
                    DailyForecast(
                        date = dayName,
                        icon = representativeForecast.weather.firstOrNull()?.icon ?: "01d",
                        description = representativeForecast.weather.firstOrNull()?.description?.replaceFirstChar { char ->
                            if (char.isLowerCase()) char.titlecase(Locale.getDefault()) else char.toString()
                        } ?: getString(R.string.description),
                        minTemp = minTemp,
                        maxTemp = maxTemp
                    )
                )
            }
        }
        return resultList
    }

    private fun findRepresentativeForecast(dailyForecasts: List<WeatherItem>): WeatherItem {
        val preferred = dailyForecasts.find { forecast ->
            val hour = Calendar.getInstance().apply { timeInMillis = forecast.dt * 1000L }.get(Calendar.HOUR_OF_DAY)
            hour in 11..14
        }

        return preferred ?: dailyForecasts.first()
    }

    private fun showCitySelectionPopup(anchorView: View, @MenuRes menuRes: Int) {
        val currentCityName = binding.cityNameTxt.text.toString()

        val popup = PopupMenu(this, anchorView)
        popup.menuInflater.inflate(menuRes, popup.menu)

        popup.menu.findItem(R.id.currentCity)?.title = currentCityName

        popup.setOnMenuItemClickListener { menuItem: MenuItem ->
            when (menuItem.itemId) {
                R.id.currentCity -> true
                R.id.addNewCity -> {
                    showSearchCityBottomSheet()
                    true
                }
                else -> false
            }
        }
        popup.show()
    }
}