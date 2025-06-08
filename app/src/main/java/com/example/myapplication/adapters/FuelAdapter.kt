package com.example.myapplication.adapters // Ajuste seu pacote

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.models.Fueling

import java.text.SimpleDateFormat
import java.util.*

class FuelAdapter(
    private val fuelings: MutableList<Fueling>,
    private val onDeleteClick: (Fueling) -> Unit
) : RecyclerView.Adapter<FuelAdapter.FuelViewHolder>() {

    inner class FuelViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvFuelDate: TextView = itemView.findViewById(R.id.tvFuelDate)
        val tvKmAtual: TextView = itemView.findViewById(R.id.tvKmAtual)
        val tvQuantity: TextView = itemView.findViewById(R.id.tvQuantity)
        val tvTotalValue: TextView = itemView.findViewById(R.id.tvTotalValue)
        val tvUnitPrice: TextView = itemView.findViewById(R.id.tvUnitPrice)
        val tvObservation: TextView = itemView.findViewById(R.id.tvObservation)
        val btnDeleteFuel: ImageView = itemView.findViewById(R.id.btnDeleteFuel)

        private val displayDateFormat = SimpleDateFormat("dd/MM/yyyy - HH:mm", Locale.getDefault())
        private val apiDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }

        fun bind(fueling: Fueling) {
            try {
                val date = apiDateFormat.parse(fueling.dataAbastecimento)
                tvFuelDate.text = date?.let { displayDateFormat.format(it) } ?: "Data Inválida"
            } catch (e: Exception) {
                tvFuelDate.text = "Data Inválida"
                e.printStackTrace()
            }

            tvKmAtual.text = "Hodômetro: ${fueling.kmAtual} km"
            tvTotalValue.text = "Valor Total: R$ ${String.format(Locale.getDefault(), "%.2f", fueling.valorTotal)}"

            if (fueling.litrosAbastecidos > 0) {
                tvQuantity.text = "Litros: ${String.format(Locale.getDefault(), "%.2f", fueling.litrosAbastecidos)} L"
                tvUnitPrice.text = "Preço por Litro: R$ ${String.format(Locale.getDefault(), "%.2f", fueling.valorUnitario)}/L"
            }else {
                tvQuantity.text = "Quantidade: N/A"
                tvUnitPrice.text = "Preço Unitário: N/A"
            }

            if (!fueling.observacao.isNullOrBlank()) {
                tvObservation.text = "Obs: ${fueling.observacao}"
                tvObservation.visibility = View.VISIBLE
            } else {
                tvObservation.visibility = View.GONE
            }

            btnDeleteFuel.setOnClickListener { onDeleteClick(fueling) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FuelViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_fuel, parent, false)
        return FuelViewHolder(view)
    }

    override fun onBindViewHolder(holder: FuelViewHolder, position: Int) {
        holder.bind(fuelings[position])
    }

    override fun getItemCount(): Int = fuelings.size

    fun updateData(newFuelings: List<Fueling>) {
        fuelings.clear()
        fuelings.addAll(newFuelings)
        notifyDataSetChanged()
    }

    fun removeFueling(fueling: Fueling) {
        val index = fuelings.indexOf(fueling)
        if (index != -1) {
            fuelings.removeAt(index)
            notifyItemRemoved(index)
        }
    }

}