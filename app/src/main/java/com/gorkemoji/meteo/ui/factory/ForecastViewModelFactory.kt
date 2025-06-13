package com.gorkemoji.meteo.ui.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.gorkemoji.meteo.data.repository.ForecastRepository
import com.gorkemoji.meteo.ui.viewmodel.ForecastViewModel

class ForecastViewModelFactory(
    private val repository: ForecastRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ForecastViewModel::class.java)) {
            return ForecastViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}