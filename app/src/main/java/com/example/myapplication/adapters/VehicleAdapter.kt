package com.example.myapplication.adapters

import API.RetrofitClient
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.FormVehicleActivity
import com.example.myapplication.R
import com.example.myapplication.models.vehicle.VehicleList
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CustomVehicleAdapter(private var dataSet: List<VehicleList>, private val context: Context, private val onListUpdated: () -> Unit) :
    RecyclerView.Adapter<CustomVehicleAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val modelo: TextView = view.findViewById(R.id.modeloVeiculo)
        val placa: TextView = view.findViewById(R.id.placaVeiculo)
        val btnDelete: ImageView = view.findViewById(R.id.btnDeletar)
        val btnEditar: ImageView = view.findViewById(R.id.btnEditar)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.item_vehicle, viewGroup, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val veiculo = dataSet[position]

        Log.d("Adapter", "onBindViewHolder() called for position $position: ${veiculo.modelo}, ${veiculo.marca}")
        viewHolder.modelo.text = veiculo.modelo
        viewHolder.placa.text = veiculo.marca.toString()

        viewHolder.btnDelete.setOnClickListener {
            AlertDialog.Builder(context)
                .setTitle("Confirmar Exclusão")
                .setMessage("Tem certeza que deseja excluir este veículo?")
                .setPositiveButton("Sim") { dialog, which ->
                    excluirVeiculo(veiculo.id) {
                        onListUpdated() // Notifica a HomeActivity para recarregar a lista
                    }
                }
                .setNegativeButton("Não", null)
                .show()
        }

        viewHolder.btnEditar.setOnClickListener {
            val intent = Intent(context, FormVehicleActivity::class.java)
            intent.putExtra("id", veiculo.id)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        Log.d("Adapter", "getItemCount() returned: ${dataSet.size}")
        return dataSet.size
    }

    private fun excluirVeiculo(vehicleId: Int?, onComplete: () -> Unit) {
        vehicleId?.let { id ->
            RetrofitClient.apiService.deleteVehicle(id).enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        Toast.makeText(context, "Veículo excluído com sucesso", Toast.LENGTH_SHORT).show()
                        onComplete()
                    } else {
                        Log.e("API Error", "Erro ao excluir veículo. Code: ${response.code()}")
                        Toast.makeText(context, "Erro ao excluir veículo", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Log.e("API Failure", "Falha ao excluir veículo", t)
                    Toast.makeText(context, "Falha ao excluir veículo", Toast.LENGTH_SHORT).show()
                }
            })
        } ?: run {
            Toast.makeText(context, "ID do veículo inválido", Toast.LENGTH_SHORT).show()
        }
    }

    fun updateList(newList: List<VehicleList>) {
        dataSet = newList
        notifyDataSetChanged()
    }
}