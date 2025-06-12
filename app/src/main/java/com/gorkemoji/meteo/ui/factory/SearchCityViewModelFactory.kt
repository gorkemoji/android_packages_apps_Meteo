package com.gorkemoji.meteo.ui.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.gorkemoji.meteo.data.repository.CityRepository
import com.gorkemoji.meteo.ui.viewmodel.SearchCityViewModel

class SearchCityViewModelFactory(
    private val repository: CityRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SearchCityViewModel::class.java))
            return SearchCityViewModel(repository) as T
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}