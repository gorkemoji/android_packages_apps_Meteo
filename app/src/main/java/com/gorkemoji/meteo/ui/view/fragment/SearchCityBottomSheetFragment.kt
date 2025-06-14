package com.gorkemoji.meteo.ui.view.fragment

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.gorkemoji.meteo.R
import com.gorkemoji.meteo.adapter.CityAdapter
import com.gorkemoji.meteo.data.model.GeocodingResponse
import com.gorkemoji.meteo.data.network.RetrofitInstance
import com.gorkemoji.meteo.data.repository.CityRepository
import com.gorkemoji.meteo.databinding.FragmentSearchCityBottomSheetBinding
import com.gorkemoji.meteo.ui.factory.SearchCityViewModelFactory
import com.gorkemoji.meteo.ui.view.ForecastActivity
import com.gorkemoji.meteo.ui.viewmodel.SearchCityViewModel

class SearchCityBottomSheetFragment : BottomSheetDialogFragment() {
    private var _binding: FragmentSearchCityBottomSheetBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: SearchCityViewModel
    private lateinit var adapter: CityAdapter
    private var selectedCity: GeocodingResponse? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchCityBottomSheetBinding.inflate(inflater, container, false)

        val apiService = RetrofitInstance.geoApi
        val repository = CityRepository(apiService)
        val factory = SearchCityViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[SearchCityViewModel::class.java]

        adapter = CityAdapter(emptyList()) { city ->
            selectedCity = city
            binding.saveBtn.isEnabled = true
        }

        binding.rvResults.layoutManager = LinearLayoutManager(requireContext())
        binding.rvResults.adapter = adapter

        binding.searchBtn.setOnClickListener {
            val cityName = binding.cityName.text.toString().trim()
            if (cityName.isNotBlank())
                viewModel.searchCity(cityName)
        }

        binding.saveBtn.setOnClickListener {
            selectedCity?.let { city ->
                setFragmentResult(
                    ForecastActivity.REQUEST_KEY_CITY_SELECTED,
                    bundleOf(
                        ForecastActivity.BUNDLE_KEY_CITY_NAME to city.name,
                        ForecastActivity.BUNDLE_KEY_COUNTRY_CODE to city.country,
                        ForecastActivity.BUNDLE_KEY_LAT to city.lat,
                        ForecastActivity.BUNDLE_KEY_LON to city.lon
                    )
                )
                dismiss()
            }
        }

        viewModel.cities.observe(viewLifecycleOwner) { cities ->
            adapter.updateList(cities)
            binding.saveBtn.isEnabled = false
            selectedCity = null
            if (cities.isEmpty() && binding.cityName.text?.isNotBlank() == true) {
                Toast.makeText(requireContext(), getString(R.string.city_not_found), Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(requireContext(), getString(R.string.error) + ": " + it, Toast.LENGTH_LONG).show()
            }
        }

        return binding.root
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        if (selectedCity == null) {
            setFragmentResult(
                ForecastActivity.REQUEST_KEY_CITY_SELECTED,
                bundleOf(
                    ForecastActivity.BUNDLE_KEY_CITY_NAME to null,
                    ForecastActivity.BUNDLE_KEY_COUNTRY_CODE to null,
                    ForecastActivity.BUNDLE_KEY_LAT to null,
                    ForecastActivity.BUNDLE_KEY_LON to null
                )
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}