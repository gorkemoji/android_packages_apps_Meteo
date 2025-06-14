package com.gorkemoji.meteo.ui.view

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.blongho.country_data.World
import com.gorkemoji.meteo.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding : ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        World.init(this)

        /*val currentLanguage = PreferencesHelper.get(this, "APP_LANGUAGE")

        if (currentLanguage.isNullOrEmpty()) {
            val deviceLanguage = Locale.getDefault().language
            PreferencesHelper.save(this, "APP_LANGUAGE", deviceLanguage)
        }*/

        startActivity(Intent(this, ForecastActivity::class.java))
        finish()
    }
}