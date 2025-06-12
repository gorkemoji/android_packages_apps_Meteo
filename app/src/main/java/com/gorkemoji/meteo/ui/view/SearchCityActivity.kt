package com.gorkemoji.meteo.ui.view

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.gorkemoji.meteo.adapter.CityAdapter
import com.gorkemoji.meteo.data.model.GeocodingResponse
import com.gorkemoji.meteo.data.network.RetrofitInstance
import com.gorkemoji.meteo.data.repository.CityRepository
import com.gorkemoji.meteo.databinding.ActivitySearchCityBinding
import com.gorkemoji.meteo.ui.factory.SearchCityViewModelFactory
import com.gorkemoji.meteo.ui.viewmodel.SearchCityViewModel
import com.gorkemoji.meteo.utils.Constants.API_KEY
import com.gorkemoji.meteo.utils.PreferencesHelper

class SearchCityActivity : AppCompatActivity() {
    private lateinit var viewModel: SearchCityViewModel
    private lateinit var adapter: CityAdapter
    private lateinit var selectedCity: GeocodingResponse
    private lateinit var binding: ActivitySearchCityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySearchCityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val apiService = RetrofitInstance.geoApi

        val repository = CityRepository(apiService)
        val factory = SearchCityViewModelFactory(repository)

        viewModel = ViewModelProvider(this, factory)[SearchCityViewModel::class.java]

        adapter = CityAdapter(emptyList()) { city ->
            selectedCity = city
            binding.saveBtn.isEnabled = true
        }

        binding.rvResults.apply {
            layoutManager = LinearLayoutManager(this@SearchCityActivity)
            this.adapter = this@SearchCityActivity.adapter
        }

        binding.searchBtn.setOnClickListener {
            val cityName = binding.cityName.text.toString()
            viewModel.searchCity(cityName, API_KEY)
        }

        binding.saveBtn.setOnClickListener {
            PreferencesHelper.save(this, "SELECTED_CITY_LAT",selectedCity.lat.toString())
            PreferencesHelper.save(this, "SELECTED_CITY_LON",selectedCity.lon.toString())
            PreferencesHelper.save(this, "SELECTED_CITY_NAME",selectedCity.name)
            PreferencesHelper.save(this, "SELECTED_CITY_COUNTRY",selectedCity.country)

            Toast.makeText(this, "${selectedCity.name}, ${selectedCity.country} saved!", Toast.LENGTH_SHORT).show()

            startActivity(Intent(this, ForecastActivity::class.java))
            finish()
        }

        viewModel.cities.observe(this) { adapter.updateList(it) }
    }
}