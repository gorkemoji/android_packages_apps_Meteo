package com.gorkemoji.meteo.ui.view

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.blongho.country_data.World
import com.gorkemoji.meteo.databinding.ActivityMainBinding
import com.gorkemoji.meteo.utils.Constants
import com.gorkemoji.meteo.utils.PreferencesHelper
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private lateinit var binding : ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        World.init(this)

        val currentLanguage = PreferencesHelper.get(this, "APP_LANGUAGE")
        val currentApiKey = PreferencesHelper.get(this, "API_KEY")
        val currentMetricType = PreferencesHelper.get(this, "METRIC_TYPE")

        if (currentLanguage.isNullOrEmpty()) {
            val deviceLanguage = Locale.getDefault().language
            PreferencesHelper.save(this, "APP_LANGUAGE", deviceLanguage)
        }

        if (currentApiKey.isNullOrEmpty()) {
            val apiKey = Constants.DEFAULT_API_KEY
            PreferencesHelper.save(this, "API_KEY", apiKey)
        }

        if (currentMetricType.isNullOrEmpty()) {
            val metricType = "metric"
            PreferencesHelper.save(this, "METRIC_TYPE", metricType)
        }

        startActivity(Intent(this, ForecastActivity::class.java))
        finish()
    }
}