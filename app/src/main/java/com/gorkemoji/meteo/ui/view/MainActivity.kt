package com.gorkemoji.meteo.ui.view

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.blongho.country_data.World
import com.gorkemoji.meteo.R
import com.gorkemoji.meteo.databinding.ActivityMainBinding
import com.gorkemoji.meteo.utils.PreferencesHelper

class MainActivity : AppCompatActivity() {
    private lateinit var binding : ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        World.init(this)

        when (PreferencesHelper.get(this, "SELECTED_CITY_NAME")) {
            null -> {
                startActivity(Intent(this, SearchCityActivity::class.java))
                finish()
            }
            else -> {
                startActivity(Intent(this, ForecastActivity::class.java))
                finish()
            }
        }
    }
}