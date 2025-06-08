package com.example.myapplication.adapters

import API.RetrofitClient
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.FormVehicleActivity
import com.example.myapplication.R
import com.example.myapplication.models.VehicleList
import com.example.myapplication.models.Brand
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CustomVehicleAdapter(
    private var dataSet: List<VehicleList>,
    private val context: Context,
    private val onListUpdated: () -> Unit
) : RecyclerView.Adapter<CustomVehicleAdapter.ViewHolder>() {

    private val brandNameCache = mutableMapOf<Int, String>()
    private var brandsLoaded = false

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("vehicle_prefs", Context.MODE_PRIVATE)

    private val SELECTED_VEHICLE_KEY = "selected_vehicle_json"

    private var currentlySelectedVehicleId: Int? = null

    private val gson = Gson()

    private fun loadAllBrands() {
        if (brandsLoaded) {
            return
        }

        RetrofitClient.apiService.getAllBrands().enqueue(object : Callback<List<Brand>> {
            override fun onResponse(call: Call<List<Brand>>, response: Response<List<Brand>>) {
                if (response.isSuccessful) {
                    val brands = response.body()
                    brands?.forEach { brand ->
                        brandNameCache[brand.id] = brand.nome
                    }
                    brandsLoaded = true
                    notifyDataSetChanged()
                } else {
                    Log.e("API Error", "Erro ao carregar todas as marcas. Code: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<List<Brand>>, t: Throwable) {
                Log.e("API Failure", "Falha ao carregar todas as marcas", t)
            }
        })
    }

    private fun loadCurrentlySelectedVehicleIdFromPrefs() {
        val json = sharedPreferences.getString(SELECTED_VEHICLE_KEY, null)
        if (json != null) {
            try {
                val selectedVehicle = gson.fromJson(json, VehicleList::class.java)
                currentlySelectedVehicleId = selectedVehicle?.id
            } catch (e: Exception) {
                Log.e("SharedPreferences", "Erro ao deserializar veículo selecionado", e)
                currentlySelectedVehicleId = null
                clearSelectedVehicle()
            }
        } else {
            currentlySelectedVehicleId = null
        }
    }

    init {
        loadAllBrands()
        loadCurrentlySelectedVehicleIdFromPrefs()
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val modelo: TextView = view.findViewById(R.id.modeloVeiculo)
        val nomeMarca: TextView = view.findViewById(R.id.nomeMarcaVeiculo)
        val btnDelete: ImageView = view.findViewById(R.id.btnDeletar)
        val btnEditar: ImageView = view.findViewById(R.id.btnEditar)
        val switchSelectVehicle: Switch = view.findViewById(R.id.switchSelectVehicle)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.item_vehicle, viewGroup, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val veiculo = dataSet[position]

        viewHolder.modelo.text = veiculo.modelo

        val brandId = veiculo.marca
        val brandName = brandNameCache[brandId]

        if (brandName != null) {
            viewHolder.nomeMarca.text = "Marca: $brandName"
        } else {
            viewHolder.nomeMarca.text = "Marca: Carregando/Indisponível"
            if (!brandsLoaded) {
                loadAllBrands()
            }
        }

        val isSelected = (veiculo.id == currentlySelectedVehicleId)
        viewHolder.switchSelectVehicle.setOnCheckedChangeListener(null)
        viewHolder.switchSelectVehicle.isChecked = isSelected

        viewHolder.switchSelectVehicle.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                if (currentlySelectedVehicleId != null && currentlySelectedVehicleId != veiculo.id) {
                    val oldSelectedPosition = dataSet.indexOfFirst { it.id == currentlySelectedVehicleId }
                    if (oldSelectedPosition != -1) {
                        notifyItemChanged(oldSelectedPosition)
                    }
                }
                saveSelectedVehicle(veiculo)
                currentlySelectedVehicleId = veiculo.id
                Toast.makeText(context, "${veiculo.modelo} selecionado!", Toast.LENGTH_SHORT).show()
            } else {
                if (veiculo.id == currentlySelectedVehicleId) {
                    if (dataSet.size == 1) {
                        Toast.makeText(context, "Deve haver pelo menos um veículo selecionado.", Toast.LENGTH_SHORT).show()
                        buttonView.isChecked = true
                    } else {
                        Toast.makeText(context, "Selecione outro veículo para mudar o principal.", Toast.LENGTH_SHORT).show()
                        buttonView.isChecked = true
                    }
                } else {
                    buttonView.isChecked = false
                }
            }
        }

        viewHolder.btnDelete.setOnClickListener {
            AlertDialog.Builder(context)
                .setTitle("Confirmar Exclusão")
                .setMessage("Tem certeza que deseja excluir este veículo?")
                .setPositiveButton("Sim") { dialog, which ->
                    val deletedVehicleId = veiculo.id
                    val wasSelected = (deletedVehicleId == currentlySelectedVehicleId)

                    excluirVeiculo(deletedVehicleId) {
                        onListUpdated()

                        if (wasSelected) {
                            if (dataSet.isNotEmpty()) {
                                val firstVehicleAfterDeletion = dataSet.first()
                                saveSelectedVehicle(firstVehicleAfterDeletion)
                                currentlySelectedVehicleId = firstVehicleAfterDeletion.id
                            } else {
                                clearSelectedVehicle()
                                currentlySelectedVehicleId = null
                            }
                        }
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

    private fun saveSelectedVehicle(vehicle: VehicleList) {
        val editor = sharedPreferences.edit()
        val json = gson.toJson(vehicle)
        editor.putString(SELECTED_VEHICLE_KEY, json)
        editor.apply()
    }

    private fun clearSelectedVehicle() {
        val editor = sharedPreferences.edit()
        editor.remove(SELECTED_VEHICLE_KEY)
        editor.apply()
    }

    fun updateList(newList: List<VehicleList>) {
        dataSet = newList

        if (currentlySelectedVehicleId != null && newList.none { it.id == currentlySelectedVehicleId }) {
            clearSelectedVehicle()
            currentlySelectedVehicleId = null
        }

        if (currentlySelectedVehicleId == null && newList.isNotEmpty()) {
            val firstVehicle = newList.first()
            saveSelectedVehicle(firstVehicle)
            currentlySelectedVehicleId = firstVehicle.id
        }

        notifyDataSetChanged()
    }
}
