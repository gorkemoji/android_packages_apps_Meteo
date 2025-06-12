package com.gorkemoji.meteo.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.gorkemoji.meteo.R
import com.gorkemoji.meteo.data.model.GeocodingResponse
import com.gorkemoji.meteo.databinding.CityLayoutBinding
import com.blongho.country_data.World

class CityAdapter(
    private var cityList: List<GeocodingResponse>,
    private var selectedPosition: Int = RecyclerView.NO_POSITION,
    private val onItemClick: (GeocodingResponse) -> Unit
) : RecyclerView.Adapter<CityAdapter.CityViewHolder>() {

    inner class CityViewHolder(val binding: CityLayoutBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CityViewHolder {
        val binding = CityLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CityViewHolder(binding)
    }

    override fun getItemCount() = cityList.size

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: CityViewHolder, position: Int) {
        val city = cityList[position]
        holder.binding.cityName.text = "${city.name}, ${city.country}"
        holder.binding.coordsTxt.text = "${city.lat}, ${city.lon}"

        val flagResId = World.getFlagOf(city.country)
        holder.binding.countryFlag.setImageResource(flagResId)

        if (position == selectedPosition)
            holder.binding.cardLayout.setBackgroundColor(holder.itemView.context.getColor(R.color.card_selected))
        else
            holder.binding.cardLayout.setBackgroundColor(holder.itemView.context.getColor(R.color.card_normal))

        holder.binding.cardLayout.setOnClickListener {
            val previousPosition = selectedPosition
            selectedPosition = position
            notifyItemChanged(previousPosition)
            notifyItemChanged(position)
            onItemClick(city)
        }
    }

    fun updateList(newList: List<GeocodingResponse>) {
        cityList = newList
        notifyDataSetChanged()
    }
}