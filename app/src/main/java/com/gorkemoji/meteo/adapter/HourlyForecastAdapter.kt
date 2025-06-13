package com.gorkemoji.meteo.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.gorkemoji.meteo.data.model.WeatherItem
import com.gorkemoji.meteo.databinding.HourlyForecastItemBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HourlyForecastAdapter(private var hourlyForecastList: List<WeatherItem>) :
    RecyclerView.Adapter<HourlyForecastAdapter.HourlyForecastViewHolder>() {

    inner class HourlyForecastViewHolder(val binding: HourlyForecastItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HourlyForecastViewHolder {
        val binding = HourlyForecastItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HourlyForecastViewHolder(binding)
    }

    override fun getItemCount(): Int = hourlyForecastList.size

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: HourlyForecastViewHolder, position: Int) {
        val hourlyForecast = hourlyForecastList[position]

        val hourFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val date = Date(hourlyForecast.dt * 1000L)

        holder.binding.hourlyTimeTxt.text = hourFormat.format(date)
        holder.binding.hourlyTempTxt.text = "${hourlyForecast.main.temp.toInt()}°C"
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newHourlyForecastList: List<WeatherItem>) {
        hourlyForecastList = newHourlyForecastList
        notifyDataSetChanged()
    }
}