package com.gorkemoji.meteo.ui.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.gorkemoji.meteo.R
import com.gorkemoji.meteo.databinding.FragmentSettingsBottomSheetBinding
import com.gorkemoji.meteo.utils.PreferencesHelper

class SettingsBottomSheetFragment : BottomSheetDialogFragment() {
    private var _binding: FragmentSettingsBottomSheetBinding? = null
    private val binding get() = _binding!!
    private var apiKey: String = ""
    private var selectedMetricType: String = "metric"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSettingsBottomSheetBinding.inflate(inflater, container, false)

        binding.saveBtn.isEnabled = false
        loadCurrentSettings()
        setupListeners()

        return binding.root
    }

    private fun loadCurrentSettings() {
        val currentApiKey = PreferencesHelper.get(requireContext(), "API_KEY")
        val currentMetricType = PreferencesHelper.get(requireContext(), "METRIC_TYPE")

        binding.apiKey.setText(currentApiKey)

        if (currentApiKey != null) apiKey = currentApiKey

        if (currentMetricType != null) selectedMetricType = currentMetricType

        when (currentMetricType) {
            "metric" -> binding.metricCelciusChip.isChecked = true
            "imperial" -> binding.metricFahrenheitChip.isChecked = true
            "standard" -> binding.metricKelvinChip.isChecked = true
        }
    }

    private fun setupListeners() {
        binding.apiKey.addTextChangedListener { editable ->
            apiKey = editable.toString().trim()
            checkIfCanSave()
        }

        binding.chipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            if (checkedIds.isEmpty()) {
                binding.metricCelciusChip.isChecked = true
                selectedMetricType = "metric"
            } else {
                when (checkedIds[0]) {
                    R.id.metricCelciusChip -> selectedMetricType = "metric"
                    R.id.metricFahrenheitChip -> selectedMetricType = "imperial"
                    R.id.metricKelvinChip -> selectedMetricType = "standard"
                }
            }
            checkIfCanSave()
        }

        binding.saveBtn.setOnClickListener { saveSettings() }
    }

    private fun checkIfCanSave() {
        val currentApiKey = PreferencesHelper.get(requireContext(), "API_KEY")
        val currentMetricType = PreferencesHelper.get(requireContext(), "METRIC_TYPE")

        val hasChanges = apiKey != currentApiKey || selectedMetricType != currentMetricType
        val isValid = apiKey.isNotEmpty()

        binding.saveBtn.isEnabled = hasChanges && isValid
    }

    private fun saveSettings() {
        try {
            PreferencesHelper.save(requireContext(), "API_KEY", apiKey)
            PreferencesHelper.save(requireContext(), "METRIC_TYPE", selectedMetricType)

            Toast.makeText(requireContext(), getString(R.string.restart_for_effects), Toast.LENGTH_LONG).show()
            dismiss()
        } catch (e: Exception) {
            Toast.makeText(requireContext(), getString(R.string.error) + e.message, Toast.LENGTH_LONG).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}